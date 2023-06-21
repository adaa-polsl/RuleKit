package adaa.analytics.rules.logic.actions.descriptors.singular;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleDescriptorBase;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.Rule;

public class ConditionCountDescriptor extends ActionRuleDescriptorBase<Double> {


    @Override
    protected Double innerDescriptor(Rule rule) {
        return new Double(rule.getPremise().getSubconditions().size());
    }

    @Override
    public String getName() {
        return "Condition count";
    }
}
