package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class BackwardActionSnC extends ActionSnC {

    public BackwardActionSnC(ActionFinder finder_, ActionInductionParameters params) {

        super(finder_, new ActionInductionParameters(params));
        ((ActionInductionParameters) this.params).reverseTransitions();
        // TODO Auto-generated constructor stub
    }

    @Override
    public RuleSetBase run(ExampleSet dataset) {

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
