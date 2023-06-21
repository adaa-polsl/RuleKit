package adaa.analytics.rules.logic.actions.descriptors.ruleset;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleSetDescriptorBase;
import adaa.analytics.rules.logic.actions.descriptors.ActionRuleDescriptorBase;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.Rule;

import java.util.ArrayList;
import java.util.List;

public class PerRuleDescriptor<RetType> extends ActionRuleSetDescriptorBase<List<RetType>> {

    protected ActionRuleDescriptorBase<RetType> innerDescriptor;

    public PerRuleDescriptor(ActionRuleDescriptorBase<RetType> inner){
        super("For each rule, " + inner.getName() + ":");
        innerDescriptor = inner;
    }

    @Override
    protected List<RetType> descriptor(ActionRuleSet ruleSet) {
        List<RetType> results = new ArrayList<RetType>();
        for (Rule r: ruleSet.getRules()) {
            results.add(innerDescriptor.descriptor(r));
        }
        return results;
    }
}
