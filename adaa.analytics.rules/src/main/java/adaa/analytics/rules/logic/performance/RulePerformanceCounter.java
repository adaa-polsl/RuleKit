package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.logic.performance.binary.BinaryClassificationPerformance;
import adaa.analytics.rules.logic.performance.binary.ExtendedBinaryPerformance;
import adaa.analytics.rules.logic.performance.simple.*;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.model.ContrastRuleSet;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.IStatistics;

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

    private IExampleSet testSet;

    public RulePerformanceCounter(IExampleSet testSet)
    {
        this.testSet = testSet;
        prepareCriteriaNames();
    }

    private void prepareCriteriaNames()
    {
        IAttribute label = testSet.getAttributes().getLabel();

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
        IAttribute weightAttribute = testSet.getAttributes().getWeight();
        if (weightAttribute != null) {
            if (!weightAttribute.isNumerical()) {
                throw new IllegalStateException("Error in weight value of example set - non numerical");
            }

            testSet.recalculateAttributeStatistics(weightAttribute);
            double minimum = testSet.getStatistics(weightAttribute, IStatistics.MINIMUM);
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


    /**
     * Calculates rule model characteristics.
     *
     * @param rs Rule set to be investigated.
     * @return Performance vector with model characteristics.
     */
    public static List<MeasuredPerformance> recalculatePerformance(RuleSetBase rs) {
        List<MeasuredPerformance> ret = new ArrayList<>();
        ret.add(new RecountedPerformance("time_total_s", rs.getTotalTime()));

        ret.add(new RecountedPerformance("time_growing_s", rs.getGrowingTime()));
        ret.add(new RecountedPerformance("time_pruning_s", rs.getPruningTime()));
        ret.add(new RecountedPerformance("#rules", rs.getRules().size()));
        ret.add(new RecountedPerformance("#conditions_per_rule", rs.calculateConditionsCount()));
        ret.add(new RecountedPerformance("#induced_conditions_per_rule", rs.calculateInducedCondtionsCount()));



        if (rs instanceof ContrastRuleSet) {
            ContrastRuleSet crs = (ContrastRuleSet) rs;

            ContrastIndicators indicators = crs.calculateAvgContrastIndicators();

            for (String k : indicators.values.keySet()) {
                ret.add(new RecountedPerformance(k, indicators.get(k)));
            }

            double[] stats = crs.calculateAttributeStats();
            ret.add(new RecountedPerformance("attribute_occurence", stats[0]));
            ret.add(new RecountedPerformance("redundancy", stats[1]));
            ret.add(new RecountedPerformance("total_duplicates", crs.getTotalDuplicates()));
        } else {
            ret.add(new RecountedPerformance("avg_rule_coverage", rs.calculateAvgRuleCoverage()));
            ret.add(new RecountedPerformance("avg_rule_precision", rs.calculateAvgRulePrecision()));
            ret.add(new RecountedPerformance("avg_rule_quality", rs.calculateAvgRuleQuality()));


            ret.add(new RecountedPerformance("avg_pvalue", rs.calculateSignificance(0.05).p));
            ret.add(new RecountedPerformance("avg_FDR_pvalue", rs.calculateSignificanceFDR(0.05).p));
            ret.add(new RecountedPerformance("avg_FWER_pvalue", rs.calculateSignificanceFWER(0.05).p));

            ret.add(new RecountedPerformance("fraction_0.05_significant", rs.calculateSignificance(0.05).fraction));
            ret.add(new RecountedPerformance("fraction_0.05_FDR_significant", rs.calculateSignificanceFDR(0.05).fraction));
            ret.add(new RecountedPerformance("fraction_0.05_FWER_significant", rs.calculateSignificanceFWER(0.05).fraction));
        }
        return ret;
    }

}
