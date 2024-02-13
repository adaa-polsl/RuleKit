package adaa.analytics.rules.logic.actions.descriptors.ruleset;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleSetDescriptorBase;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.model.ActionRuleSet;

public class AverageCountOfEmptyActions extends ActionRuleSetDescriptorBase<Double> {

    public AverageCountOfEmptyActions(){
        super("Average count of empty actions");
    }

    @Override
    protected Double descriptor(ActionRuleSet ruleSet) {
        int result = 0;

        for (Rule r : ruleSet.getRules()) {
            ActionRule actionRule = (ActionRule)r;

            for (ConditionBase cb : actionRule.getPremise().getSubconditions()){
                Action action = (Action)cb;

                if (action.getActionNil() || action.isLeftEqualRight() || action.getRightCondition() == null){
                    result++;
                }
            }
        }
        return (double)result / (double)ruleSet.getRules().size();
    }
}
