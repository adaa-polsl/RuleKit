package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegressionActionFinder extends ActionFinder {

    private enum PruningDecision {
        PRUNE_SOURCE,
        PRUNE_TARGET,
        PRUNE,
        DONT_PRUNE
    }

    private RegressionActionInductionParameters params;

    public RegressionActionFinder(RegressionActionInductionParameters params) {
        super(params);
        this.params = params;
    }

    List<ElementaryCondition> getAllNominalConditions(Attribute attribute) {
        List<ElementaryCondition> ret = new ArrayList<>(attribute.getMapping().size());
        //consider all values
        for (int i = 0; i < attribute.getMapping().size(); i++) {

            ElementaryCondition proposal = new ElementaryCondition(attribute.getName(), new SingletonSet(i, attribute.getMapping().getValues()));
            ret.add(proposal);
        }
        return ret;
    }

    List<ElementaryCondition> getAllNumericalConditions(Attribute attribute, ExampleSet trainSet, Set<Integer> coveredByRule) {
        Map<Double, List<Integer>> values2ids = new TreeMap<>();

        // get all distinctive values of attribute
        for (int id : coveredByRule) {
            Example ex = trainSet.getExample(id);
            double val = ex.getValue(attribute);

            if (!values2ids.containsKey(val)) {
                values2ids.put(val, new ArrayList<>());
            }
            values2ids.get(val).add(id);
        }

        Double[] keys = values2ids.keySet().toArray(new Double[values2ids.size()]);
        ArrayList toBeChecked = new ArrayList<>(keys.length * 2);
        // check all possible midpoints
        for (int keyId = 0; keyId < keys.length - 1; ++keyId) {
            double key = keys[keyId];
            double next = keys[keyId + 1];
            double midpoint = (key + next) / 2;

            // evaluate left-side condition a < v
            ElementaryCondition candidate = new ElementaryCondition(attribute.getName(), Interval.create_le(midpoint));
            toBeChecked.add(candidate);

            // evaluate right-side condition v <= a
            candidate = new ElementaryCondition(attribute.getName(), Interval.create_geq(midpoint));
            toBeChecked.add(candidate);
        }

        return toBeChecked;
    }


    private boolean testOrderCorrectness(double source, double target) {
        RegressionActionInductionParameters parameters = (RegressionActionInductionParameters) this.params;
        switch (parameters.getRegressionOrder()) {
            case BETTER:
                return target > source;
            case WORSE:
                return source > target;
            case ANY:
                return Double.compare(source, target) != 0;
            default:
                return true;
        }
    }

    @Override
    protected ElementaryCondition induceCondition(Rule rule, ExampleSet trainSet, Set<Integer> uncoveredByRuleset, Set<Integer> coveredByRule, Set<Attribute> allowedAttributes, Object... extraParams) {

        if (allowedAttributes.size() == 0)
            return null;

        RegressionActionRule rRule = (RegressionActionRule) rule;

        if (rRule == null) throw new RuntimeException("RegressionFinder cannot extend non regression rule!");

        RegressionRule sourceRule = (RegressionRule) rRule.getLeftRule();
        RegressionRule targetRule = (RegressionRule) rRule.getRightRule();

        RegressionFinder regressionFinder = new RegressionFinder(this.params);
        ElementaryCondition bestForSource = regressionFinder.induceCondition(sourceRule, trainSet, uncoveredByRuleset, coveredByRule, allowedAttributes, extraParams);

        Attribute bestAttr = null;
        if (bestForSource != null) {
            bestAttr = trainSet.getAttributes().get(bestForSource.getAttribute());
            if (bestAttr.isNominal()) {
                allowedAttributes.remove(bestAttr);
            }
        }

        if (bestAttr == null) {
            return null;
        }

        sourceRule.getPremise().addSubcondition(bestForSource);

        Covering sourceRuleCovering = sourceRule.covers(trainSet);
        sourceRule.setCoveringInformation(sourceRuleCovering);
        Interval sourceInterval = createConsequenceInterval(sourceRuleCovering);

        //now let's find counter condition that assures best seperation!
        List<ElementaryCondition> toBeCheckedForTarget;
        Set<Integer> idsToConsider = new HashSet<>();
        Covering coveredByTargetRule = null;

        coveredByTargetRule = targetRule.covers(trainSet);

        if (bestAttr.isNominal()) {
            toBeCheckedForTarget = getAllNominalConditions(bestAttr);
        } else {
            if (rule.getPremise().getSubconditions().size() == 0) {
                toBeCheckedForTarget = getAllNumericalConditions(bestAttr, trainSet, IntStream.range(0, trainSet.size()).boxed().collect(Collectors.toSet()));
            } else {
                toBeCheckedForTarget = getAllNumericalConditions(bestAttr, trainSet, coveredByTargetRule.positives);
            }
        }


        double bestQ = Double.NEGATIVE_INFINITY;
        ElementaryCondition bestCounterCondition = null;

        final double minCov = params.getMinimumCovered();

        for (ElementaryCondition candidate : toBeCheckedForTarget) {
            CompoundCondition cc = new CompoundCondition();
            cc.getSubconditions().addAll(targetRule.getPremise().getSubconditions());
            RegressionRule testRule = new RegressionRule(cc, targetRule.getConsequence());

            //extend with proposed condition
            testRule.getPremise().addSubcondition(candidate);
            //test it
            Covering cov = testRule.covers(trainSet);
            testRule.setCoveringInformation(cov);

            double newlyCovered = SetHelper.intersectionSize(uncoveredByRuleset, cov.positives) + SetHelper.intersectionSize(uncoveredByRuleset, cov.negatives);

            if (newlyCovered <= minCov) {
                continue;
            }

            if (!params.getCanOverlapConsequences()) {
                Interval testRuleInterval = createConsequenceInterval(cov);

                if (testRuleInterval.intersects(sourceInterval)) {
                    continue;
                }
            }

            if (!testOrderCorrectness(sourceRule.getConsequenceValue(), targetRule.getConsequenceValue())) {
                continue;
            }

            ClassificationMeasure Q = (ClassificationMeasure) (this.params.getInductionMeasure());
            double q = Q.calculate(trainSet, cov);

            if (q > bestQ) {
                bestQ = q;
                bestCounterCondition = candidate;
            }

            /*
            //
            // test t studenta - musi byc maksymalizowana wartosc bezwzlgledna statystyki testowej
            // aby ja wyliczyc potrzebne info o licznosci przykladow po obu stronach
               double testStatisticValue = testRule.getConsequenceValue();
            TTest t = new TTestImpl();
            double[] targetValues = new double[cov.negatives.size()];

            l = cov.negatives.stream()
                    .map(x -> trainSet.getExample(x).getLabel())
                    .collect(Collectors.toList());

            for (int i = 0; i < l.size(); i++) {
                targetValues[i] = l.get(i);
            }

            if (targetValues.length < 2 || sourceValues.length < 2 ) continue;

            //test obustronny
            double testStatisticValue = t.t(sourceValues, targetValues);

            // MOŻNA zrobić testy jednostronne i wybierać czy wartość ma być większa czy mniejsza
            //wtedy np. walidujemy najpierw średnie z populacji
            //a alfa * 2... tak przynajmniej mówi manual do apache commons
               if (Math.abs(testStatisticValue) > Math.abs(prevTestStatisticValue)) {
                prevTestStatisticValue = testStatisticValue;
                bestCounterCondition = candidate;
            }
            */

        }
        //don't generate invalid rules if no new best counter condition have been discovered and we get into nasty situation that extending just source will cause the goals to overlap
        if (bestCounterCondition == null) {
            Interval targetInterval = createConsequenceInterval(coveredByTargetRule);
            if (sourceInterval.intersects(targetInterval))
                return null;
        }

        return new Action(bestForSource, bestCounterCondition);
    }


    @Override
    public void prune(Rule rule_, ExampleSet trainSet, Set<Integer> uncoveredPositives) {
        RegressionActionRule rar = (RegressionActionRule) rule_;

        if (rar == null)
            throw new IllegalArgumentException("Not an regression action rule in regression action rule pruning!");

        RegressionRule source = (RegressionRule) rar.getLeftRule();
        RegressionRule target = (RegressionRule) rar.getRightRule();

        ClassificationMeasure measure = (ClassificationMeasure) this.params.getPruningMeasure();
        boolean pruned = true;
        if (!params.getCanOverlapConsequences()) {
            //always driven by source rule...
            {
                Covering sourceCovering = source.covers(trainSet);
                Covering targetCovering = target.covers(trainSet);

                double initialQualitySource = measure.calculate(trainSet, sourceCovering);
                double initialQualityTarget = measure.calculate(trainSet, targetCovering);
                Interval unprunedSourceInterval = createConsequenceInterval(sourceCovering);
                Interval unprunedTargetInterval = createConsequenceInterval(targetCovering);
                double unprunedSourceValue = source.getConsequenceValue();
                double unprunedTargetValue = target.getConsequenceValue();

                while (pruned) {
                    ConditionBase toRemoveOrPrune = null;
                    PruningDecision decision = PruningDecision.DONT_PRUNE;

                    Interval bestPrunedSourceInterval = null, bestTargetPrunedInterval = null;
                    double bestPrunedSourceValue = Double.NEGATIVE_INFINITY, bestTargetPrunedValue = Double.NEGATIVE_INFINITY;
                    double bestPrunedSourceQ = Double.NEGATIVE_INFINITY, bestPrunedTargetQ = Double.NEGATIVE_INFINITY;

                    for (ConditionBase currentCondition : rar.getPremise().getSubconditions()) {
                        Action currentAction = (Action) currentCondition;


                        ConditionBase sourceCond = currentAction.getLeftCondition();
                        ConditionBase targetCond = currentAction.getRightCondition();

                        boolean targetPrunedBetter = false;
                        boolean sourcePrunedBetter = false;

                        Interval prunedTargetInterval = null;
                        Interval prunedSourceInterval = null;

                        double prunedTargetValue = Double.NEGATIVE_INFINITY;
                        double prunedSourceValue = Double.NEGATIVE_INFINITY;

                        double targetQ = Double.NEGATIVE_INFINITY;
                        double sourceQ = Double.NEGATIVE_INFINITY;


                        if (currentAction.getLeftValue() == null) {
                            sourcePrunedBetter = false;
                        } else {
                            ConditionBase testedCondition = source.getPremise().getSubconditions().stream().filter(x -> x.equals(sourceCond)).findFirst().get();
                            testedCondition.setDisabled(true);
                            Covering prunedSourceCovering = source.covers(trainSet);
                            sourceQ = measure.calculate(trainSet, prunedSourceCovering);
                            prunedSourceValue = source.getConsequenceValue();
                            prunedSourceInterval = createConsequenceInterval(prunedSourceCovering);
                            testedCondition.setDisabled(false);
                            sourcePrunedBetter = Double.compare(sourceQ, initialQualitySource) >= 0;
                        }

                        if (currentAction.getRightValue() == null) {
                            targetPrunedBetter = false;
                        } else {
                            ConditionBase testedCondition = target.getPremise().getSubconditions().stream().filter(x -> x.equals(targetCond)).findFirst().get();
                            testedCondition.setDisabled(true);
                            Covering prunedTargetCovering = target.covers(trainSet);
                            targetQ = measure.calculate(trainSet, prunedTargetCovering);
                            prunedTargetValue = target.getConsequenceValue();
                            prunedTargetInterval = createConsequenceInterval(prunedTargetCovering);
                            testedCondition.setDisabled(false);
                            targetPrunedBetter = Double.compare(targetQ, initialQualityTarget) >= 0;
                        }


                        if (sourcePrunedBetter && targetPrunedBetter && !prunedSourceInterval.intersects(prunedTargetInterval) && testOrderCorrectness(prunedSourceValue, prunedTargetValue)) {
                            toRemoveOrPrune = currentAction;
                            decision = PruningDecision.PRUNE;
                            bestPrunedSourceInterval = prunedSourceInterval;
                            bestTargetPrunedInterval = prunedTargetInterval;
                            bestPrunedSourceValue = prunedSourceValue;
                            bestTargetPrunedValue = prunedTargetValue;
                            bestPrunedSourceQ = sourceQ;
                            bestPrunedTargetQ = targetQ;
                        } else if (sourcePrunedBetter && !prunedSourceInterval.intersects(unprunedTargetInterval) && testOrderCorrectness(prunedSourceValue, unprunedTargetValue)) {
                            toRemoveOrPrune = currentAction;
                            decision = PruningDecision.PRUNE_SOURCE;
                            bestPrunedSourceInterval = prunedSourceInterval;
                            bestPrunedSourceValue = prunedSourceValue;
                            bestPrunedSourceQ = sourceQ;
                        } else if (targetPrunedBetter && !unprunedSourceInterval.intersects(prunedTargetInterval) && testOrderCorrectness(unprunedSourceValue, prunedTargetValue)) {
                            toRemoveOrPrune = currentAction;
                            decision = PruningDecision.PRUNE_TARGET;
                            bestTargetPrunedInterval = prunedTargetInterval;
                            bestTargetPrunedValue = prunedTargetValue;
                            bestPrunedTargetQ = targetQ;
                        }

                    }

                    if (decision != PruningDecision.DONT_PRUNE) {
                        Action removedAction = (Action) toRemoveOrPrune;
                        switch (decision) {
                            case PRUNE:
                                rar.getPremise().getSubconditions().removeIf(x -> x.equals(removedAction));
                                //rar.getPremise().removeSubcondition(toRemoveOrPrune);
                                unprunedSourceInterval = bestPrunedSourceInterval;
                                unprunedTargetInterval = bestTargetPrunedInterval;
                                unprunedSourceValue = bestPrunedSourceValue;
                                unprunedTargetValue = bestTargetPrunedValue;
                                initialQualitySource = bestPrunedSourceQ;
                                initialQualityTarget = bestPrunedTargetQ;
                                source = (RegressionRule) rar.getLeftRule();
                                target = (RegressionRule) rar.getRightRule();
                                break;
                            case PRUNE_SOURCE:
                                rar.getPremise().getSubconditions().removeIf(x -> x.equals(removedAction));
                                //rar.getPremise().removeSubcondition(toRemoveOrPrune);
                                if (removedAction.getRightValue() != null)
                                    rar.getPremise().addSubcondition(new Action(removedAction.getAttribute(), null, removedAction.getRightValue()));
                                unprunedSourceInterval = bestPrunedSourceInterval;
                                unprunedSourceValue = bestPrunedSourceValue;
                                initialQualitySource = bestPrunedSourceQ;
                                source = (RegressionRule) rar.getLeftRule();
                                break;
                            case PRUNE_TARGET:
                                rar.getPremise().getSubconditions().removeIf(x -> x.equals(removedAction));
                                //rar.getPremise().removeSubcondition(toRemoveOrPrune);
                                if (removedAction.getLeftValue() != null)
                                    rar.getPremise().addSubcondition(new Action(removedAction.getAttribute(), removedAction.getLeftValue(), null));
                                unprunedTargetInterval = bestTargetPrunedInterval;
                                unprunedTargetValue = bestTargetPrunedValue;
                                initialQualityTarget = bestPrunedTargetQ;
                                target = (RegressionRule) rar.getRightRule();
                                break;
                            case DONT_PRUNE:
                            default:
                                pruned = false;
                                continue;

                        }
                        pruned = rar.getPremise().getSubconditions().size() > 1;
                    } else {
                        pruned = false;
                    }

                }
            }
        } else {
            //in regression (and survival) case the pruning cannot be realized as below!!! because it will lead to violation of the BETTER/WORSE options
            //the removal of actions, its sources or targets have to be considered simultaneously to maintain the position of the rule
            RegressionFinder rf = new RegressionFinder(super.params);
            rf.prune(source, trainSet, uncoveredPositives);


            double initialQ = measure.calculate(trainSet, target.covers(trainSet));


            Covering sourcePrunedCovering = new Covering();
            source.covers(trainSet, sourcePrunedCovering, sourcePrunedCovering.positives, sourcePrunedCovering.negatives);

            IValueSet sourceRange = new Interval(sourcePrunedCovering.mean_y - sourcePrunedCovering.stddev_y, sourcePrunedCovering.mean_y + sourcePrunedCovering.stddev_y, false, false);

            while (pruned) {

                ConditionBase toRemove = null;
                double bestQ = Double.NEGATIVE_INFINITY;

                for (ConditionBase cnd : target.getPremise().getSubconditions()) {

                    if (!cnd.isPrunable()) continue;


                    cnd.setDisabled(true);

                    Covering covering = new Covering();
                    target.covers(trainSet, covering, covering.positives, covering.negatives);
                    cnd.setDisabled(false);

                    double q = measure.calculate(trainSet, covering);

                    if (q >= bestQ && testOrderCorrectness(source.getConsequenceValue(), target.getConsequenceValue())) {
                        toRemove = cnd;
                        bestQ = q;
                    }
                }

                if (bestQ >= initialQ) {
                    initialQ = bestQ;
                    target.getPremise().removeSubcondition(toRemove);
                    pruned = target.getPremise().getSubconditions().size() > 1;
                } else {
                    pruned = false;
                }
            }

            //reconstruct action rule

            //source -> target
            // or source -> nil
            RegressionRule finalTarget = target;
            List<ElementaryCondition> newPremise = source.getPremise().getSubconditions().stream()
                    .map(ElementaryCondition.class::cast)
                    .map(x -> {
                        List<ElementaryCondition> t = finalTarget.getPremise().getSubconditions().stream().map(ElementaryCondition.class::cast).filter(y -> y.getAttribute().equals(x.getAttribute())).collect(Collectors.toList());
                        ElementaryCondition right = null;
                        if (t.size() > 0) {
                            right = t.get(0);
                        }
                        return new Action(x, right);
                    })
                    .collect(Collectors.toList());

            // nil -> target
            RegressionRule finalSource = source;
            newPremise.addAll(target.getPremise().getSubconditions().stream()
                    .map(ElementaryCondition.class::cast)
                    .filter(x -> !finalSource.getPremise().getSubconditions().stream().map(ElementaryCondition.class::cast).anyMatch(y -> y.getAttribute().equals(x.getAttribute())))
                    .map(x -> new Action(null, x))
                    .collect(Collectors.toList())
            );

            CompoundCondition cc = new CompoundCondition();
            newPremise.forEach(cc::addSubcondition);
            rule_.setPremise(cc);

            ActionCovering covering = (ActionCovering) rule_.covers(trainSet);
            rule_.setCoveredNegatives(new IntegerBitSet(trainSet.size()));
            rule_.getCoveredNegatives().addAll(covering.negatives);
            rule_.setCoveredPositives(new IntegerBitSet(trainSet.size()));
            rule_.getCoveredPositives().addAll(covering.positives);
            rule_.setCoveringInformation(covering);
        }
    }

    private Interval createConsequenceInterval(Covering covering) {
        return new Interval(covering.mean_y - covering.stddev_y, covering.mean_y + covering.stddev_y, false, false);
    }
}
