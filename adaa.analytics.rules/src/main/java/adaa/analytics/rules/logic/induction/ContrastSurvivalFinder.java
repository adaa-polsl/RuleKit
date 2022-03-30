package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.quality.NegativeControlledMeasure;
import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.container.Pair;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
            Covering cov = (Covering)ct;
            Set<Integer> examples = new HashSet<>();
            examples.addAll(cov.positives);
            KaplanMeierEstimator positiveEstimator = new KaplanMeierEstimator(dataset, examples);

            examples.addAll(cov.negatives);
            KaplanMeierEstimator entireEstimator = new KaplanMeierEstimator(dataset, examples);

            // compare estimators of:
            // - all covered examples (entire contrast set)
            // - covered positives
            Pair<Double,Double> statsAndPValue = super.compareEstimators(positiveEstimator, entireEstimator);

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
        observers.clear();
        observers.add(penalty);

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

        for (IFinderObserver o: observers) {
            o.ruleReady(rule);
        }
    }

    @Override
    boolean checkCoverage(double p, double n, double new_p, double new_n, double P, double N) {
               return ((new_p) >= params.getAbsoluteMinimumCovered(P)) &&
                ((p) >= params.getAbsoluteMinimumCoveredAll(P));
    }
}
