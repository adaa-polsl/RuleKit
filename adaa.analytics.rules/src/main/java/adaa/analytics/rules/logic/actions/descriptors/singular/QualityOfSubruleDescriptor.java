package adaa.analytics.rules.logic.actions.descriptors.singular;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleDescriptorBase;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ClassificationRule;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.rm.example.IExampleSet;

public class QualityOfSubruleDescriptor extends ActionRuleDescriptorBase<Double> {

    public enum RuleSide {
        LEFT,
        RIGHT
    }

    protected RuleSide ruleSide;
    protected ClassificationMeasure qualityFunc;
    protected IExampleSet exampleSet;

    public QualityOfSubruleDescriptor(RuleSide sideOfRule, ClassificationMeasure measure, IExampleSet examples){
        ruleSide = sideOfRule;
        qualityFunc = measure;
        exampleSet = examples;
    }

    @Override
    protected Double innerDescriptor(Rule rule) {
        Rule subRule;
        if (rule instanceof ActionRule) {
            ActionRule aRule = (ActionRule)rule;
            subRule = ruleSide.equals(RuleSide.LEFT) ? aRule.getLeftRule() : aRule.getRightRule();
        } else if (rule instanceof ClassificationRule) {
            subRule = rule;
        } else throw new RuntimeException("Not supported");

        Covering cov = new Covering();
        subRule.covers(exampleSet, cov);
        return qualityFunc.calculate(exampleSet, cov);
    }

    @Override
    public String getName() {
        return qualityFunc.getName() + " of " + ruleSide.name() + " subrule";
    }
}
