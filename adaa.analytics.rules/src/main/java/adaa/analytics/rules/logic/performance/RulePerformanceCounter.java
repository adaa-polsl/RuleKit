package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.logic.performance.binary.BinaryClassificationPerformance;
import adaa.analytics.rules.logic.performance.binary.ExtendedBinaryPerformance;
import adaa.analytics.rules.logic.performance.simple.*;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RulePerformanceCounter {

    private static final MeasuredPerformance[] MULTICLASS_CRITERIA_CLASSES = {
            new MultiClassificationPerformance(MultiClassificationPerformance.ACCURACY),
            new MultiClassificationPerformance(MultiClassificationPerformance.ERROR),
            new MultiClassificationPerformance(MultiClassificationPerformance.KAPPA),
            new ClassificationRulesPerformance(ClassificationRulesPerformance.BALANCED_ACCURACY),
            new ClassificationRulesPerformance(ClassificationRulesPerformance.RULES_PER_EXAMPLE),
            new ClassificationRulesPerformance(ClassificationRulesPerformance.VOTING_CONFLICTS),
            new ClassificationRulesPerformance( ClassificationRulesPerformance.NEGATIVE_VOTING_CONFLICTS),
            new CrossEntropy(),
            new Margin(),
            new SoftMarginLoss(),
            new LogisticLoss()
    };

    private static final MeasuredPerformance[] BINARY_CRITERIA_CLASSES = {
            new BinaryClassificationPerformance(BinaryClassificationPerformance.PRECISION),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.SENSITIVITY),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.SPECIFICITY),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.NEGATIVE_PREDICTIVE_VALUE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.FALLOUT),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.YOUDEN),
            new ExtendedBinaryPerformance(),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.PSEP),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.LIFT),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.F_MEASURE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.FALSE_POSITIVE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.FALSE_NEGATIVE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.TRUE_POSITIVE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.TRUE_NEGATIVE),
    };

    private static final MeasuredPerformance[] REGRESSION_CRITERIA_CLASSES = {
            new AbsoluteError(),
            new RelativeError(),
            new LenientRelativeError(),
            new StrictRelativeError(),
            new NormalizedAbsoluteError(),
            new SquaredError(),
            new RootMeanSquaredError(),
            new RootRelativeSquaredError(),
            new CorrelationCriterion(),
            new SquaredCorrelationCriterion()
    };

    private static final MeasuredPerformance[] SURVIVAL_CRITERIA_CLASSES = {
            new IntegratedBrierScore()
    };

    private List<MeasuredPerformance> choosedCriterion = new ArrayList<>();

    private ExampleSet testSet;

    public RulePerformanceCounter(ExampleSet testSet)
    {
        this.testSet = testSet;
        prepareCriteriaNames();
    }

    private void prepareCriteriaNames()
    {
        Attribute label = testSet.getAttributes().getLabel();

        if (testSet.getAnnotations().containsKey(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            throw new IllegalStateException("Operator does not evaluate contrast sets.");
        } else if (testSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
            choosedCriterion.addAll(Arrays.asList(SURVIVAL_CRITERIA_CLASSES));
        } else if (label.isNominal()) {
            if (label.getMapping().size() == 2) {
                choosedCriterion.addAll(Arrays.asList(MULTICLASS_CRITERIA_CLASSES));
                choosedCriterion.addAll(Arrays.asList(BINARY_CRITERIA_CLASSES));
            } else {
                choosedCriterion.addAll(Arrays.asList(MULTICLASS_CRITERIA_CLASSES));
            }

        } else {
            choosedCriterion.addAll(Arrays.asList(REGRESSION_CRITERIA_CLASSES));
        }


    }

    public void countValues() {
        Attribute weightAttribute = testSet.getAttributes().getWeight();
        if (weightAttribute != null) {
            if (!weightAttribute.isNumerical()) {
                throw new IllegalStateException("Error in weight value of example set - non numerical");
            }

            testSet.recalculateAttributeStatistics(weightAttribute);
            double minimum = testSet.getStatistics(weightAttribute, Statistics.MINIMUM);
            if (Double.isNaN(minimum) || minimum < 0.0d) {
                throw new IllegalStateException("Error in weight value of example set - nan or negative");
            }
        }

        // initialize all criteria
        for (MeasuredPerformance c : choosedCriterion) {

            // init all criteria
            c.startCounting(testSet, true);
        }

        Iterator<Example> exampleIterator = testSet.iterator();
        while (exampleIterator.hasNext()) {
            Example example = exampleIterator.next();

            if ((Double.isNaN(example.getLabel()) || Double.isNaN(example.getPredictedLabel()))) {
                continue;
            }

            for (MeasuredPerformance criterion: choosedCriterion) {
                     criterion.countExample(example);
            }
        }
    }

    public List<MeasuredPerformance> getResult()
    {
        return choosedCriterion;
    }

}
