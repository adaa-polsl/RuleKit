package adaa.analytics.rules.logic.actions.descriptors;

import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.Rule;

public abstract class ActionRuleDescriptorBase<RetType>  {


    public RetType descriptor(Rule rule) {
        //ActionRule actionRule = (ActionRule)rule;
        //if (actionRule == null) throw new RuntimeException("Cannot apply action rule descriptor to non-action rule");

        return innerDescriptor(rule);
    }



    protected abstract RetType innerDescriptor(Rule rule);

    public abstract String getName();
}
