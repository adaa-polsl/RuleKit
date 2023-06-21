package adaa.analytics.rules.logic.actions.descriptors.ruleset;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import com.rapidminer.example.ExampleSet;

import java.util.Objects;
import java.util.stream.Collectors;

public class AverageSupportOfRightSubRules extends AverageQualityOfSubRuleBase {

    public AverageSupportOfRightSubRules(ExampleSet trainExamples) {
        super("Average support of right subrules",
                trainExamples,
                new ClassificationMeasure(ClassificationMeasure.Coverage));
    }

    @Override
    protected Double descriptor(ActionRuleSet ruleSet) {
        rulesToProcess = ruleSet.getRules()
                .stream()
                .map(ActionRule.class::cast)
                .map(ActionRule::getRightRule)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return super.descriptor(null);
    }
}
