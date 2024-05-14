package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.data.metadata.EStatisticType;
import adaa.analytics.rules.logic.representation.ContrastIndicators;
import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import adaa.analytics.rules.logic.representation.rule.SurvivalRule;
import adaa.analytics.rules.logic.representation.ruleset.ContrastRuleSet;
import adaa.analytics.rules.logic.representation.ruleset.RuleSetBase;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RulePerformanceCounter {

    private static final AbstractPerformanceCounter[] MULTICLASS_CRITERIA_CLASSES = {
            new MultiClassificationPerformance(MultiClassificationPerformance.ACCURACY),
            new MultiClassificationPerformance(MultiClassificationPerformance.ERROR),
            new MultiClassificationPerformance(MultiClassificationPerformance.KAPPA),
            new BalancedAccuracyPerformance(),
            new ClassificationRulesPerformance(ClassificationRulesPerformance.RULES_PER_EXAMPLE),
            new ClassificationRulesPerformance(ClassificationRulesPerformance.VOTING_CONFLICTS),
            new NegativeVotingConflictsPerformance()
    };

    private static final AbstractPerformanceCounter[] BINARY_CRITERIA_CLASSES = {
            new BinaryClassificationPerformance(BinaryClassificationPerformance.PRECISION),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.SENSITIVITY),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.SPECIFICITY),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.NEGATIVE_PREDICTIVE_VALUE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.FALLOUT),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.YOUDEN),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.GEOMETRIC_MEAN),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.PSEP),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.LIFT),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.F_MEASURE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.FALSE_POSITIVE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.FALSE_NEGATIVE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.TRUE_POSITIVE),
            new BinaryClassificationPerformance(BinaryClassificationPerformance.TRUE_NEGATIVE),
    };

    private static final AbstractPerformanceCounter[] REGRESSION_CRITERIA_CLASSES = {
            new SimpleCriterion(SimpleCriterion.ABSOLUTE_ERROR),
            new SimpleCriterion(SimpleCriterion.RELATIVE_ERROR),
            new SimpleCriterion(SimpleCriterion.LENIENT_RELATIVE_ERROR),
            new SimpleCriterion(SimpleCriterion.STRICT_RELATIVE_ERROR),
            new NormalizedAbsoluteError(),
            new SimpleCriterion(SimpleCriterion.SQUARED_ERROR),
            new SimpleCriterion(SimpleCriterion.ROOT_MEAN_SQUARED_ERROR),
            new RootRelativeSquaredError(),
            new CorrelationCriterion(),
            new SquaredCorrelationCriterion()
    };

    private static final AbstractPerformanceCounter[] SURVIVAL_CRITERIA_CLASSES = {
            new IntegratedBrierScore()
    };

    private List<AbstractPerformanceCounter> choosedCriterion = new ArrayList<>();


    private List<PerformanceResult> performanceResults = new ArrayList<>();

    private IExampleSet testSet;

    public RulePerformanceCounter(IExampleSet testSet) {
        this.testSet = testSet;
        prepareCriteriaNames();
    }

    private void prepareCriteriaNames() {
        IAttribute label = testSet.getAttributes().getLabel();

        if (testSet.getAnnotations().containsKey(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            throw new IllegalStateException("Operator does not evaluate contrast sets.");
        } else if (testSet.getAttributes().getColumnByRole(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
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
            weightAttribute.recalculateStatistics();
            double minimum = weightAttribute.getStatistic(EStatisticType.MINIMUM);
            if (Double.isNaN(minimum) || minimum < 0.0d) {
                throw new IllegalStateException("Error in weight value of example set - nan or negative");
            }
        }

        // initialize all criteria
        for (AbstractPerformanceCounter criterion : choosedCriterion) {

            // init all criteria
            PerformanceResult pr = criterion.countExample(testSet);
            performanceResults.add(pr);
        }
    }

    public List<PerformanceResult> getResult() {
        return performanceResults;
    }


    /**
     * Calculates rule model characteristics.
     *
     * @param rs Rule set to be investigated.
     * @return Performance vector with model characteristics.
     */
    public static List<PerformanceResult> recalculatePerformance(RuleSetBase rs) {
        List<PerformanceResult> ret = new ArrayList<>();
        ret.add(new PerformanceResult("time_total_s", rs.getTotalTime()));

        ret.add(new PerformanceResult("time_growing_s", rs.getGrowingTime()));
        ret.add(new PerformanceResult("time_pruning_s", rs.getPruningTime()));
        ret.add(new PerformanceResult("#rules", rs.getRules().size()));
        ret.add(new PerformanceResult("#conditions_per_rule", rs.calculateConditionsCount()));
        ret.add(new PerformanceResult("#induced_conditions_per_rule", rs.calculateInducedCondtionsCount()));


        if (rs instanceof ContrastRuleSet) {
            ContrastRuleSet crs = (ContrastRuleSet) rs;

            ContrastIndicators indicators = crs.calculateAvgContrastIndicators();

            for (String k : indicators.values.keySet()) {
                ret.add(new PerformanceResult(k, indicators.get(k)));
            }

            double[] stats = crs.calculateAttributeStats();
            ret.add(new PerformanceResult("attribute_occurence", stats[0]));
            ret.add(new PerformanceResult("redundancy", stats[1]));
            ret.add(new PerformanceResult("total_duplicates", crs.getTotalDuplicates()));
        } else {
            ret.add(new PerformanceResult("avg_rule_coverage", rs.calculateAvgRuleCoverage()));
            ret.add(new PerformanceResult("avg_rule_precision", rs.calculateAvgRulePrecision()));
            ret.add(new PerformanceResult("avg_rule_quality", rs.calculateAvgRuleQuality()));


            ret.add(new PerformanceResult("avg_pvalue", rs.calculateSignificance(0.05).p));
            ret.add(new PerformanceResult("avg_FDR_pvalue", rs.calculateSignificanceFDR(0.05).p));
            ret.add(new PerformanceResult("avg_FWER_pvalue", rs.calculateSignificanceFWER(0.05).p));

            ret.add(new PerformanceResult("fraction_0.05_significant", rs.calculateSignificance(0.05).fraction));
            ret.add(new PerformanceResult("fraction_0.05_FDR_significant", rs.calculateSignificanceFDR(0.05).fraction));
            ret.add(new PerformanceResult("fraction_0.05_FWER_significant", rs.calculateSignificanceFWER(0.05).fraction));
        }
        return ret;
    }

}
