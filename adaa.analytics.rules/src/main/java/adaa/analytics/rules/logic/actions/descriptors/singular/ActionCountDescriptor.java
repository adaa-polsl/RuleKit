package adaa.analytics.rules.logic.actions.descriptors.singular;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleDescriptorBase;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.Rule;

public class ActionCountDescriptor extends ActionRuleDescriptorBase<Double> {

    @Override
    protected Double innerDescriptor(Rule rule) {
        return new Double(rule.getPremise().getSubconditions().stream()
                .map(Action.class::cast)
                .filter(x -> !x.getActionNil() && !x.isLeftEqualRight())
                .count());
    }

    @Override
    public String getName() {
        return "Action Count";
    }
}
