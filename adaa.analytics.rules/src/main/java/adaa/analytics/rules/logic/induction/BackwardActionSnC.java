package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.model.ActionRuleSet;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.rm.example.IExampleSet;

public class BackwardActionSnC extends ActionSnC {

    public BackwardActionSnC(ActionFinder finder_, ActionInductionParameters params) {

        super(finder_, new ActionInductionParameters(params));
        ((ActionInductionParameters) this.params).reverseTransitions();
        // TODO Auto-generated constructor stub
    }

    @Override
    public RuleSetBase run(IExampleSet dataset) {

        ActionRuleSet ruleset = (ActionRuleSet)super.run(dataset);

        ActionRuleSet retRuleSet = (ActionRuleSet) factory.create(dataset);

        for (Rule r : ruleset.getRules()) {
            ActionRule reverted = new BackwardActionRuleAdapter((ActionRule)r).get();
            ActionCovering cov = (ActionCovering)reverted.covers(dataset);
            reverted.calculatePValue(dataset, (ClassificationMeasure)params.getInductionMeasure());
            reverted.setCoveredPositives(new IntegerBitSet(dataset.size()));
            reverted.getCoveredPositives().retainAll(cov.positives);

            reverted.setCoveredNegatives(new IntegerBitSet((dataset.size())));
            reverted.getCoveredNegatives().retainAll(cov.negatives);

            reverted.setCoveringInformation(cov);

            retRuleSet.addRule(reverted);
        }

        return retRuleSet;
    }
}
