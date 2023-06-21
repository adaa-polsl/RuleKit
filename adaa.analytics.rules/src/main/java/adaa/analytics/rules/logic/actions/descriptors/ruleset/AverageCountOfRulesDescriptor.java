package adaa.analytics.rules.logic.actions.descriptors.ruleset;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleSetDescriptorBase;
import adaa.analytics.rules.logic.representation.ActionRuleSet;

public class AverageCountOfRulesDescriptor extends ActionRuleSetDescriptorBase<Integer> {

    public AverageCountOfRulesDescriptor() {
        super("Average count of rules");
    }

    @Override
    protected Integer descriptor(ActionRuleSet ruleSet){
        return ruleSet.getRules().size();
    }
}
