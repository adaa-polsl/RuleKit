package adaa.analytics.rules.logic.actions.descriptors.ruleset;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleSetDescriptorBase;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.rm.example.IExampleSet;
import sun.tools.asm.Cover;

import java.util.List;

public abstract class AverageQualityOfSubRuleBase extends ActionRuleSetDescriptorBase<Double> {

    protected IExampleSet exampleSet;
    protected ClassificationMeasure qualityFunction;
    protected List<Rule> rulesToProcess;

    protected AverageQualityOfSubRuleBase(String name, IExampleSet trainExamples, ClassificationMeasure measure){
        super(name);
        exampleSet = trainExamples;
        qualityFunction = measure;
    }

    @Override
    protected Double descriptor(ActionRuleSet ruleSet) {
        double overallQuality = 0.0;

        for(Rule rule : rulesToProcess) {
            Covering cov = new Covering();
            rule.covers(exampleSet, cov);
            overallQuality += qualityFunction.calculate(exampleSet, cov);
        }
        return overallQuality / (double)ruleSet.getRules().size();
    }
}
