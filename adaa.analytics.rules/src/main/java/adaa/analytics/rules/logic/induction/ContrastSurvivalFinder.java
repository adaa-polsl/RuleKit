package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.quality.NegativeControlledMeasure;
import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class ContrastSurvivalFinder extends SurvivalLogRankFinder implements IPenalizedFinder {

    /// Quality measure used for regression contrast sets
    static class Measure extends LogRank implements Serializable {

        private static final long serialVersionUID = 168994542094649823L;

        @Override
        public String getName() {
            return "Survival contrast set quality measure";
        }

        @Override
        public double calculate(ExampleSet dataset, ContingencyTable ct) {

            ContrastSurvivalExampleSet ces = (dataset instanceof ContrastSurvivalExampleSet) ? (ContrastSurvivalExampleSet)dataset : null;
            if (ces == null) {
                throw new InvalidParameterException("ContrastSurvivalRuleSet supports only ContrastSurvivalExampleSet instances");
            }

            Covering cov = (Covering)ct;
            Set<Integer> examples = new IntegerBitSet(dataset.size());
            examples.addAll(cov.positives);
            examples.addAll(cov.negatives);
            KaplanMeierEstimator entireEstimator = new KaplanMeierEstimator(dataset, examples);

            // compare estimators of:
            // - all covered examples (entire contrast set)
            // - entire group
            KaplanMeierEstimator groupEstimator = ces.getGroupEstimators().get((int)ct.targetLabel);
            Pair<Double,Double> statsAndPValue = super.compareEstimators(groupEstimator, entireEstimator);

            // smaller test statistics -> smaller difference -> better contrast set
            return -statsAndPValue.getFirst();
        }

        @Override
        public double calculate(double p, double n, double P, double N) {
            assert false: "ContrastSurvivalFinder.Measure: unable to calculate quality from contingency matrix only";
            return 0;
        }
    }

    private AttributePenaltyCollection penalties;

    @Override
    public AttributePenaltyCollection getAttributePenalties() {
        return penalties;
    }

    public ContrastSurvivalFinder(InductionParameters params) {
        super(params);
        penalties = new AttributePenaltyCollection(params);
        IQualityMeasure m = new ContrastSurvivalFinder.Measure();
        params.setInductionMeasure(m);
        params.setPruningMeasure(new NegativeControlledMeasure(m, params.getMaxcovNegative()));
        params.setVotingMeasure(m);
    }

    public ExampleSet preprocess(ExampleSet trainSet) {
        super.preprocess(trainSet);
        return trainSet; // return original one
    }

    /**
     * Invokes grow method from the super class and verifies negative coverage requirement.
     *
     * @param rule Rule to be grown.
     * @param dataset Training set.
     * @param uncovered Set of positive examples yet uncovered by the model.
     * @return Number of conditions added.
     */
    public int grow(
            final Rule rule,
            final ExampleSet dataset,
            final Set<Integer> uncovered) {

        int consequence = (int)(((SingletonSet)rule.getConsequence().getValueSet()).getValue());

        AttributePenalty penalty = penalties.get(consequence);
        modifier = penalty;
        clearObservers();
        addObserver(penalty);

        int cnds = super.grow(rule, dataset, uncovered);
        NegativeControlledMeasure ncm = (NegativeControlledMeasure)params.getPruningMeasure();
        if (ncm.verifyNegativeCoverage(rule.getCoveringInformation()) == false) {
            cnds = 0;
        }
        return cnds;
    }

    /**
     * Postprocess a rule.
     *
     * @param rule Rule to be postprocessed.
     * @param dataset Training set.
     *
     */
    @Override
    public void postprocess(
            final Rule rule,
            final ExampleSet dataset) {

        IntegerBitSet covered = new IntegerBitSet(dataset.size());
        IntegerBitSet  negatives = new IntegerBitSet(dataset.size());
        ContingencyTable ct = new ContingencyTable();
        rule.covers(dataset, ct, covered, negatives);
        covered.addAll(negatives);

        KaplanMeierEstimator kme = new KaplanMeierEstimator(dataset, covered);
        ((ContrastSurvivalRule)rule).setEstimator(kme);

        notifyRuleReady(rule);
    }

    @Override
    protected boolean checkCandidate(
            ExampleSet dataset,
            Rule rule,
            ConditionBase candidate,
            Set<Integer> uncovered,
            Set<Integer> covered,
            ConditionEvaluation currentBest) {

        try {

            CompoundCondition newPremise = new CompoundCondition();
            newPremise.getSubconditions().addAll(rule.getPremise().getSubconditions());
            newPremise.addSubcondition(candidate);

            Rule newRule = (Rule) rule.clone();
            newRule.setPremise(newPremise);

            Covering cov = new Covering();
            newRule.covers(dataset, cov, cov.positives, cov.negatives);

            double new_p = 0, new_n = 0;

            if (dataset.getAttributes().getWeight() == null) {
                // unweighted examples
                new_p = SetHelper.intersectionSize(uncovered, cov.positives);
                new_n =	SetHelper.intersectionSize(uncovered, cov.negatives);
            } else {
                // calculate weights of newly covered examples
                for (int id : cov.positives) {
                    new_p += uncovered.contains(id) ? dataset.getExample(id).getWeight() : 0;
                }
                for (int id : cov.negatives) {
                    new_n += uncovered.contains(id) ? dataset.getExample(id).getWeight() : 0;
                }
            }

            if (checkCoverage(cov.weighted_p, cov.weighted_n, new_p, new_n, cov.weighted_P, cov.weighted_N, uncovered.size(), rule.getRuleOrderNum())) {

                double quality = params.getInductionMeasure().calculate(dataset, cov);

                if (candidate instanceof ElementaryCondition) {
                    ElementaryCondition ec = (ElementaryCondition) candidate;
                    quality = modifier.modifyQuality(quality, ec.getAttribute(), cov.weighted_p, new_p);
                }

                if (quality > currentBest.quality ||
                        (quality == currentBest.quality && (new_p > currentBest.covered || currentBest.opposite))) {

                    /*
                    Logger.log("\t\tCurrent best: " + candidate +
                            " (p=" + cov.weighted_p + ", new_p=" + (double) new_p +
                            ", n=" + cov.weighted_n + ", new_n=" + (double) new_n +
                            ", P=" + cov.weighted_P +
                            ", mean_y=" + cov.mean_y + ", mean_y2=" + cov.mean_y2 + ", stddev_y=" + cov.stddev_y +
                            ", quality=" + quality + "\n", Level.FINEST);
                    */
                    currentBest.quality = quality;
                    currentBest.condition = candidate;
                    currentBest.covered = new_p;
                    currentBest.covering = cov;
                    currentBest.opposite = (candidate instanceof ElementaryCondition) &&
                            (((ElementaryCondition) candidate).getValueSet() instanceof SingletonSetComplement);

                    //rule.setWeight(quality);
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    boolean checkCoverage(double p, double n, double new_p, double new_n, double P, double N,double uncoveredSize, int ruleOrderNum) {
               return ((new_p) >= params.getAbsoluteMinimumCovered(P)) &&
                ((p) >= params.getAbsoluteMinimumCoveredAll(P));
    }
}
