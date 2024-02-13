package adaa.analytics.rules.logic.actions.descriptors;

import adaa.analytics.rules.logic.representation.model.ActionRuleSet;

import java.util.ArrayList;
import java.util.List;

public class ActionRuleSetDescriptors {

    protected List<ActionRuleSetDescriptorBase<?>> stats = new ArrayList<ActionRuleSetDescriptorBase<?>>();

    public void add(ActionRuleSetDescriptorBase<?> stat) {
        stats.add(stat);
    }

    public String generateReport(ActionRuleSet ruleSet) {

        StringBuilder builder = new StringBuilder();

        for (ActionRuleSetDescriptorBase<?> stat : stats) {
            builder.append(stat.getName())
                    .append(':')
                    .append(stat.apply(ruleSet))
                    .append('\n');
        }

        return builder.toString();
    }

}
