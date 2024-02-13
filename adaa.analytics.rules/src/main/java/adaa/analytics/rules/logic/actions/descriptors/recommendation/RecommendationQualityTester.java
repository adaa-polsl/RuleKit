package adaa.analytics.rules.logic.actions.descriptors.recommendation;

import adaa.analytics.rules.logic.actions.descriptors.ActionRuleDescriptorBase;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationQualityTester {

    public class RecommendationTestResult {
        public boolean sucessful;
        Map<String, Object> results;
        public ActionRuleDescriptorBase usedDescriptor;

        public String printResults() {
            StringBuilder builder = new StringBuilder();
            builder.append("Results (")
                    .append(usedDescriptor.getName())
                    .append("):")
                    .append(results.toString());
            return builder.toString();
        }
    }


    private ActionRuleDescriptorBase descriptor;
    private IExampleSet examples;

    public RecommendationQualityTester(IExampleSet testExamples, ActionRuleDescriptorBase measurement) {
        examples = testExamples;
        descriptor = measurement;
    }

    public RecommendationTestResult runTest(ActionRule _recommendation) {
        Map<String, Object> results = new HashMap<>();
        IAttribute label = examples.getAttributes().getLabel();
        List<String> classes = new ArrayList<>(label.getMapping().getValues());
        //label.getMapping().

        CompoundCondition premise = _recommendation.getRightRule().getPremise();
        Double classIdInMapping = ((SingletonSet)(_recommendation.getRightRule().getConsequence().getValueSet())).getValue();
        String originalTarget = label.getMapping().mapIndex(classIdInMapping.intValue());
        classes.remove(originalTarget);

        Object originalStats = descriptor.descriptor(_recommendation.getRightRule());
        for (String klass : classes){

            ClassificationRule testRule = new ClassificationRule(premise,
                    new ElementaryCondition(label.getName(), new SingletonSet(label.getMapping().getIndex(klass), classes)));

            results.put(klass, descriptor.descriptor(testRule));
        }
        RecommendationTestResult res = new RecommendationTestResult();
        res.results = results;
        res.results.put(originalTarget, originalStats);

        String maximumKey = res.results.entrySet().stream().max((entry1, entry2) -> (Double) entry1.getValue() > (Double) entry2.getValue() ? 1 : -1).get().getKey();
        res.sucessful = maximumKey.equalsIgnoreCase(originalTarget);

        res.usedDescriptor = descriptor;
        return res;
    }
}
