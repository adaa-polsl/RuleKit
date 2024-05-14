package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.data.DataColumnDoubleAdapter;
import adaa.analytics.rules.data.IDataColumnAdapter;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.NegativeControlledMeasure;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.logic.representation.exampleset.ContrastExampleSet;
import adaa.analytics.rules.logic.representation.exampleset.ContrastRegressionExampleSet;
import adaa.analytics.rules.logic.representation.rule.ContrastRegressionRule;
import adaa.analytics.rules.logic.representation.rule.Rule;
import adaa.analytics.rules.logic.representation.valueset.SingletonSet;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Set;

public class ContrastRegressionFinder extends RegressionFinder implements IPenalizedFinder {

    /// Quality measure used for regression contrast sets
    static class Measure implements IQualityMeasure, Serializable {

        private static final long serialVersionUID = 1116710036994194548L;

        @Override
        public String getName() {
            return "Regression contrast set quality measure";
        }

        @Override
        public double calculate(IExampleSet dataset, ContingencyTable ct) {

            ContrastRegressionExampleSet cer = (dataset instanceof ContrastExampleSet) ? (ContrastRegressionExampleSet)dataset : null;
            if (cer == null) {
                throw new InvalidParameterException("ContrastSurvivalRuleSet supports only ContrastRegressionExampleSet instances");
            }

            Covering cov = (Covering)ct;
            double sum = 0;

            int i = 0;
            IDataColumnAdapter labelDataColumnDoubleAdapter = dataset.getDataColumnDoubleAdapter(dataset.getAttributes().getLabel(), Double.NaN);

            for (int e : cov.positives) {
                sum += labelDataColumnDoubleAdapter.getDoubleValue(e);
            }
            for (int e : cov.negatives) {
                sum += labelDataColumnDoubleAdapter.getDoubleValue(e);
            }

            // the smaller the difference in means, the better the contrast set
            double groupEstimator = cer.getGroupEstimators().get((int)ct.targetLabel);
            double diff = Math.abs(sum / (cov.weighted_p + cov.weighted_n) - groupEstimator);
            return -diff;
        }

        @Override
        public double calculate(double p, double n, double P, double N) {
            assert false: "ContrastRegressionFinder.Measure: unable to calculate quality from contingency matrix only";
            return 0;
        }
    }

    private AttributePenaltyCollection penalties;

    @Override
    public AttributePenaltyCollection getAttributePenalties() {
        return penalties;
    }

    public ContrastRegressionFinder(InductionParameters params) {
        super(params);
        penalties = new AttributePenaltyCollection(params);
        IQualityMeasure m = new Measure();
        params.setInductionMeasure(m);
        params.setPruningMeasure(new NegativeControlledMeasure(m, params.getMaxcovNegative()));
        params.setVotingMeasure(m);
        params.setMeanBasedRegression(false);
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
            final IExampleSet dataset,
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
            final IExampleSet dataset) {

        IntegerBitSet covered = new IntegerBitSet(dataset.size());
        IntegerBitSet  negatives = new IntegerBitSet(dataset.size());
        ContingencyTable ct = new ContingencyTable();
        rule.covers(dataset, ct, covered, negatives);
        covered.addAll(negatives);
        IDataColumnAdapter labelDataColumnDoubleAdapter = dataset.getDataColumnDoubleAdapter(dataset.getAttributes().getLabel(), Double.NaN);

        double sum = 0;
        for (int e : covered) {
            sum += labelDataColumnDoubleAdapter.getDoubleValue(e);
        }
        ((ContrastRegressionRule)rule).setMeanLabel(sum / covered.size());

        notifyRuleReady(rule);
    }


    boolean checkCoverage(double p, double n, double new_p, double new_n, double P, double N) {
        return ((new_p) >= params.getAbsoluteMinimumCovered(P)) &&
                ((p) >= params.getAbsoluteMinimumCoveredAll(P));
    }
}
