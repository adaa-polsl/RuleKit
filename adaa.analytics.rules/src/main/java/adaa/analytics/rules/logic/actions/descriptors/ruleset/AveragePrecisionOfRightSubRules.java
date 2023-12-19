package adaa.analytics.rules.logic.actions.descriptors.ruleset;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import com.rapidminer.example.ExampleSet;

import java.util.stream.Collectors;

public class AveragePrecisionOfRightSubRules extends AverageQualityOfSubRuleBase {

    public AveragePrecisionOfRightSubRules(ExampleSet trainExamples) {
        super("Average support of left subrules",
                trainExamples,
                new ClassificationMeasure(ClassificationMeasure.Precision));
    }

    @Override
    protected Double descriptor(ActionRuleSet ruleSet) {
        rulesToProcess = ruleSet.getRules()
                .stream()
                .map(ActionRule.class::cast)
                .map(ActionRule::getRightRule)
                .collect(Collectors.toList());
        return super.descriptor(null);
    }
}