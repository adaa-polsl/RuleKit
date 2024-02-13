package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.NegativeControlledMeasure;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Set;

public class ContrastClassificationFinder extends ClassificationFinder implements IPenalizedFinder {

    private AttributePenaltyCollection penalties;

    @Override
    public AttributePenaltyCollection getAttributePenalties() {
        return penalties;
    }

    /**
     * Initializes induction parameters.
     *
     * @param params Induction parameters.
     */
    public ContrastClassificationFinder(InductionParameters params) {
        super(params);
        penalties = new AttributePenaltyCollection(params);
        params.setPruningMeasure(new NegativeControlledMeasure(params.getPruningMeasure(), params.getMaxcovNegative()));
    }


    /**
     * Invokes grow method from the super class and verifies negative coverage requirement.
     *
     * @param rule Rule to be grown.
     * @param dataset Training set.
     * @param uncovered Set of positive examples yet uncovered by the model.
     * @return Number of conditions added.
     */
    @Override
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
}
