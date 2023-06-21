package adaa.analytics.rules.logic.actions.descriptors;

import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActionRuleDescriptors {

    List<ActionRuleDescriptorBase> descriptors = new ArrayList<>();

    public void add(ActionRuleDescriptorBase descriptor) {
        descriptors.add(descriptor);
    }

    public String generateReport(ActionRule rule) {

        StringBuilder builder = new StringBuilder();

        for (ActionRuleDescriptorBase<?> stat : descriptors) {
            builder.append(stat.getName())
                    .append(':')
                    .append(stat.descriptor(rule))
                    .append('\n');
        }

        return builder.toString();
    }

    public Map<String, Double> getValues(Rule rule) {

        return descriptors.stream()
                .collect(Collectors.toMap(x -> x.getName(), y -> (Double)y.descriptor(rule)));


    }
}
