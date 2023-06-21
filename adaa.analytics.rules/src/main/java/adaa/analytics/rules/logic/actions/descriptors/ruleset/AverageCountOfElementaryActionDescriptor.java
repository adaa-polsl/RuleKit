package adaa.analytics.rules.logic.actions.descriptors.ruleset;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleSetDescriptorBase;
import adaa.analytics.rules.logic.representation.*;

public class AverageCountOfElementaryActionDescriptor extends ActionRuleSetDescriptorBase<Double> {

    public AverageCountOfElementaryActionDescriptor() {
        super("Average count of elementary action per rule");
    }

    @Override
    protected Double descriptor(ActionRuleSet ruleSet){
        double ruleSetSize = ruleSet.getRules().size();
        int amountOfElementaryActions = 0;

        for (Rule rule : ruleSet.getRules()){
            ActionRule actionRule = (ActionRule)rule;

            for (ConditionBase ec : rule.getPremise().getSubconditions()){
                Action action = (Action)ec;

                amountOfElementaryActions += (action.getActionNil() || action.isLeftEqualRight() ? 0 : 1);
            }
        }
        return (double)amountOfElementaryActions / ruleSetSize;
    }
}
