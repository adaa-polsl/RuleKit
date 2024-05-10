package adaa.analytics.rules.logic.rulegenerator;

import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.IUserMeasure;
import adaa.analytics.rules.utils.Logger;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RuleGeneratorParams {


    /**
     * Auxiliary enumeration type describing possible destinations of quality measures.
     */
    protected enum MeasureDestination {
        INDUCTION,
        PRUNING,
        VOTING
    };



    /**
     * Boolean indicating whether user's knowledge should be used.
     */
    public static final String PARAMETER_USE_EXPERT = "use_expert";


    public static final String PARAMETER_EXPERT_CONFIG = "expert_config";

    /**
     * Set of initial (expert's) rules.
     */
    public static final String PARAMETER_EXPERT_RULES = "expert_rules";

    /**
     * Multiset of preferred conditions (used also for specifying preferred attributes by using special value "Any").
     */
    public static final String PARAMETER_EXPERT_PREFERRED_CONDITIONS = "expert_preferred_conditions";

    /**
     * Set of forbidden conditions (used also for specifying forbidden attributes by using special value Any).
     */
    public static final String PARAMETER_EXPERT_FORBIDDEN_CONDITIONS = "expert_forbidden_conditions";

    /**
     * Auxiliary parameter for specifying sets/multisets of expert rules and preferred/forbidden conditions/attributes.
     */
    public static final String PARAMETER_EXPORT_KEY = "export_key";

    /**
     * Auxiliary parameter for specifying sets/multisets of expert rules and preferred/forbidden conditions/attributes.
     */
    public static final String PARAMETER_EXPORT_VALUE = "export_value";

    /**
     * Boolean indicating whether initial rules should be extended with a use of preferred conditions and attributes.
     */
    public static final String PARAMETER_EXTEND_USING_PREFERRED = "extend_using_preferred";

    /**
     * Boolean indicating whether initial rules should be extended with a use of automatic conditions.
     */
    public static final String PARAMETER_EXTEND_USING_AUTOMATIC = "extend_using_automatic";

    /**
     * Boolean indicating whether new rules should be induced with a use of preferred conditions and attributes.
     */
    public static final String PARAMETER_INDUCE_USING_PREFERRED = "induce_using_preferred";

    /**
     * Boolean indicating whether new rules should be induced with a use of automatic conditions.
     */
    public static final String PARAMETER_INDUCE_USING_AUTOMATIC = "induce_using_automatic";

    /**
     * Boolean indicating whether automatic induction should be performed for classes for which
     * no user's knowledge has been defined (classification only).
     */
    public static final String PARAMETER_CONSIDER_OTHER_CLASSES = "consider_other_classes";

    /**
     * Maximum number of preferred conditions per rule.
     */
    public static final String PARAMETER_PREFERRED_CONDITIONS_PER_RULE = "preferred_conditions_per_rule";

    /**
     * Maximum number of preferred attributes per rule.
     */
    public static final String PARAMETER_PREFERRED_ATTRIBUTES_PER_RULE = "preferred_attributes_per_rule";


    /**
     * Number/fraction of previously uncovered examples to be covered by a new rule.
     * (positive examples for classification problems).
     */
    public static final String PARAMETER_MINCOV_NEW = "minsupp_new";

    /**
     * Max number of rules to generate - overrides min_cov setting
     * */
    public static final String PARAMETER_MAX_RULE_COUNT = "max_rule_count";


    /**
     * Number/fraction examples to be covered by a new rule.
     * (positive examples for classification problems).
     */
    public static final String PARAMETER_MINCOV_ALL = "minsupp_all";

    /**
     * Fraction of examples that may remain uncovered by the rule set.
     */
    public static final String PARAMETER_MAX_UNCOVERED_FRACTION = "max_uncovered_fraction";

    /**
     * Non-negative integer representing maximum number of conditions which can be added to the rule in the growing phase
     * (use this parameter for large datasets if execution time is prohibitive); 0 indicates no limit.
     */
    public static final String PARAMETER_MAX_GROWING = "max_growing";

    /**
     *  Flag determining if best candidate should be selected from growing phase."
     */
    public static final String PARAMETER_SELECT_BEST_CANDIDATE = "select_best_candidate";

    /**
     * Name of the rule quality measure used during growing (ignored in the survival analysis where log-rank statistics is used).
     */
    public static final String PARAMETER_INDUCTION_MEASURE = "induction_measure";

    /**
     * Binary parameter indicating whether pruning should be enabled.
     */
    public static final String PARAMETER_ENABLE_PRUNING = "enable_pruning";

    /**
     * Name of the rule quality measure used during pruning.
     */
    public static final String PARAMETER_PRUNING_MEASURE = "pruning_measure";
    /**
     * Name of the rule quality measure used during growing.
     */
    public static final String PARAMETER_VOTING_MEASURE = "voting_measure";

    /**
     * Class name of user-defined induction measure; applies only when the corresponding measure parameter has value UserDefined;
     * the equation must be a mathematical expression with p, n, P, N literals (elements of confusion matrix), operators,
     * numbers, and library functions (sin, log, etc.).
     */
    public static final String PARAMETER_USER_INDUCTION_CLASS = "user_induction_class";

    /**
     * Class name of user-defined pruning measure.
     */
    public static final String PARAMETER_USER_PRUNING_CLASS = "user_pruning_class";

    /**
     * Class name of user-defined voting measure.
     */
    public static final String PARAMETER_USER_VOTING_CLASS = "user_voting_class";

    /**
     * Boolean telling whether missing values should be ignored (by default, a missing value of given attribute is always
     *  considered as not fulfilling the condition build upon that attribute)
     */
    public static final String PARAMETER_IGNORE_MISSING = "ignore_missing";

    /**
     *
     */
    public static final String PARAMETER_MAXCOV_NEGATIVE = "max_neg2pos";

    public static final String PARAMETER_PENALTY_STRENGTH = "penalty_strength";

    public static final String PARAMETER_PENALTY_SATURATION = "penalty_saturation";

    public static final String PARAMETER_MAX_PASSES_COUNT = "max_passes_count";

    public static final String PARAMETER_INCLUDE_BINARY_CONTRAST = "include_binary_contrast";

    public static final String PARAMETER_COMPLEMENTARY_CONDITIONS = "complementary_conditions";

    public static final String PARAMETER_MEAN_BASED_REGRESSION = "mean_based_regression";

    public static final String PARAMETER_CONTROL_APRORI_PRECISION = "control_apriori_precision";

    public static final String PARAMETER_APPROXIMATE_INDUCTION = "approximate_induction";

    public static final String PARAMETER_APPROXIMATE_BINS_COUNT = "approximate_bins_count";
    private final Map<String,Object> parameterValues = new HashMap<>();
    /**
    * Instead of parameter in settings (user_induction_class) one can set object with IUserMeasure
    * */
    public IUserMeasure userMeasureInductionObject;

    /**
     * Instead of parameter in settings (user_purning_class) one can set object with IUserMeasure
     * */
    public IUserMeasure userMeasurePurningObject;

    /**
     * Instead of parameter in settings (user_voting_class) one can set object with IUserMeasure
     * */
    public IUserMeasure userMeasureVotingObject;


    public RuleGeneratorParams() {
        InductionParameters defaultParams = new InductionParameters();
        parameterValues.put(PARAMETER_MINCOV_NEW, defaultParams.getMinimumCovered());
        parameterValues.put(PARAMETER_MINCOV_ALL, "");
        parameterValues.put(PARAMETER_MAX_RULE_COUNT,defaultParams.getMaxRuleCount());
        parameterValues.put(PARAMETER_MAX_UNCOVERED_FRACTION, defaultParams.getMaximumUncoveredFraction());
        parameterValues.put(PARAMETER_MAX_GROWING,  defaultParams.getMaxGrowingConditions());
        parameterValues.put(PARAMETER_SELECT_BEST_CANDIDATE, defaultParams.getSelectBestCandidate());
        parameterValues.put(PARAMETER_COMPLEMENTARY_CONDITIONS,defaultParams.isConditionComplementEnabled());
        parameterValues.put(PARAMETER_INDUCTION_MEASURE, defaultParams.getInductionMeasure().getName());
        parameterValues.put(PARAMETER_USER_INDUCTION_CLASS, null);
        parameterValues.put(PARAMETER_ENABLE_PRUNING,defaultParams.isPruningEnabled());
        parameterValues.put(PARAMETER_PRUNING_MEASURE, defaultParams.getPruningMeasure().getName());
        parameterValues.put(PARAMETER_USER_PRUNING_CLASS, null);
        parameterValues.put(PARAMETER_VOTING_MEASURE, defaultParams.getVotingMeasure().getName());
        parameterValues.put(PARAMETER_USER_VOTING_CLASS,null);
        parameterValues.put(PARAMETER_IGNORE_MISSING,defaultParams.isIgnoreMissing());
        parameterValues.put(PARAMETER_MAXCOV_NEGATIVE, defaultParams.getMaxcovNegative());
        parameterValues.put(PARAMETER_PENALTY_STRENGTH, defaultParams.getPenaltyStrength());
        parameterValues.put(PARAMETER_PENALTY_SATURATION, defaultParams.getPenaltySaturation());
        parameterValues.put(PARAMETER_MAX_PASSES_COUNT, defaultParams.getMaxPassesCount());
        parameterValues.put(PARAMETER_MEAN_BASED_REGRESSION, defaultParams.isMeanBasedRegression());
        parameterValues.put(PARAMETER_CONTROL_APRORI_PRECISION, defaultParams.isControlAprioriPrecision());
        parameterValues.put(PARAMETER_APPROXIMATE_INDUCTION, defaultParams.isApproximateInduction());
        parameterValues.put(PARAMETER_APPROXIMATE_BINS_COUNT, defaultParams.getApproximateBinsCount());

        parameterValues.put(PARAMETER_USE_EXPERT,  false);
        parameterValues.put(PARAMETER_EXTEND_USING_PREFERRED, false);
        parameterValues.put(PARAMETER_EXTEND_USING_AUTOMATIC, false);
        parameterValues.put(PARAMETER_INDUCE_USING_PREFERRED, false);
        parameterValues.put(PARAMETER_INDUCE_USING_AUTOMATIC, false);
        parameterValues.put(PARAMETER_CONSIDER_OTHER_CLASSES, false);
        parameterValues.put(PARAMETER_PREFERRED_CONDITIONS_PER_RULE,  Integer.MAX_VALUE);
        parameterValues.put(PARAMETER_PREFERRED_ATTRIBUTES_PER_RULE, Integer.MAX_VALUE);


        parameterValues.put(PARAMETER_EXPERT_CONFIG, null);
        parameterValues.put(PARAMETER_EXPERT_RULES, null);
        parameterValues.put(PARAMETER_EXPERT_PREFERRED_CONDITIONS, null);
        parameterValues.put(PARAMETER_EXPERT_FORBIDDEN_CONDITIONS, null);
    }

    public boolean contains(String key) {
        return parameterValues.containsKey(key);
    }
    public Map<String,Object> getParameters() {
        return parameterValues;
    }

    public void setParameter(String key, String o) {
        parameterValues.put(key,o);
    }

    public void setListParameter(String key, List<String[]> o) {
        parameterValues.put(key,o);
    }

    public boolean getParameterAsBoolean(String key) {
        if (!parameterValues.containsKey(key))
            return false;
        return Boolean.valueOf(parameterValues.get(key).toString());
    }

    public void setUserMeasureInductionObject(IUserMeasure userMeasureInductionObject) {
        this.userMeasureInductionObject = userMeasureInductionObject;
    }


    public void setUserMeasurePurningObject(IUserMeasure userMeasurePurningObject) {
        this.userMeasurePurningObject = userMeasurePurningObject;
    }

    public void setUserMeasureVotingObject(IUserMeasure userMeasureVotingObject) {
        this.userMeasureVotingObject = userMeasureVotingObject;
    }

    public int getParameterAsInt(String key) {
        return Integer.valueOf(parameterValues.get(key).toString());
    }

    public double getParameterAsDouble(String key) {
        return Double.valueOf(parameterValues.get(key).toString());
    }

    public String getParameterAsString(String key) {
        return (String)parameterValues.get(key);
    }

    public String getParameter(String key) {
        return (String)parameterValues.get(key);
    }

    public List<String[]> getParameterList(String key)
    {
        if (!parameterValues.containsKey(key))
            return new ArrayList<>();
        else
            return (List<String[]>)parameterValues.get(key);
    }


    /**
     * Creates instance of a quality measure object.
     * @param destination Destination of measure (growing/pruning/voting).
     * @param defaultMeasure Default measure.
     * @return New quality measure.
     * @throws IllegalAccessException
     */
    private IQualityMeasure generateQualityMeasure(MeasureDestination destination, IQualityMeasure defaultMeasure)  {
        String measureName;
        if (destination == MeasureDestination.INDUCTION) {
            measureName = getParameterAsString(PARAMETER_INDUCTION_MEASURE);
        } else if (destination == MeasureDestination.PRUNING) {
            measureName = getParameterAsString(PARAMETER_PRUNING_MEASURE);
        } else {
            measureName = getParameterAsString(PARAMETER_VOTING_MEASURE);
        }

        int variant = -1;
        for (int i = 0; i < ClassificationMeasure.NAMES.length; i++) {
            if (ClassificationMeasure.getName(i).equals(measureName)) {
                variant = i;
            }
        }
        if (variant != -1) {
            ClassificationMeasure classificationMeasure = new ClassificationMeasure(variant);
            if (measureName.equals(ClassificationMeasure.getName(ClassificationMeasure.UserDefined))) {
                classificationMeasure.setUserMeasure(resolveUserMeasure(destination));
            }
            return classificationMeasure;
        } else {
            Logger.log("No quality measure defined, using default (" + defaultMeasure.getName() + ")", Level.INFO);
            return defaultMeasure;
        }
    }

    private IUserMeasure resolveUserMeasure(MeasureDestination destination)
    {
        IUserMeasure userMeasureObj = null;
        String className;
        if (destination == MeasureDestination.INDUCTION) {
            className = getParameter(PARAMETER_USER_INDUCTION_CLASS);
            userMeasureObj = this.userMeasureInductionObject;
        } else if (destination == MeasureDestination.PRUNING) {
            className = getParameter(PARAMETER_USER_PRUNING_CLASS);
            userMeasureObj = this.userMeasurePurningObject;
        } else {
            className = getParameter(PARAMETER_USER_VOTING_CLASS);
            userMeasureObj = this.userMeasureVotingObject;
        }

        if (userMeasureObj!=null)
            return userMeasureObj;

        if (className==null || className.isEmpty())
        {
            throw new IllegalStateException("No user measure defined for "+destination);
        }

        try {
            Class aClass =  Class.forName(className);
            return (IUserMeasure) aClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Error while running UserMeasure class. " + e.getMessage());
        }
    }

    InductionParameters generateInductionParameters()  {
        InductionParameters params = new InductionParameters();
        params.setInductionMeasure(generateQualityMeasure(MeasureDestination.INDUCTION, new ClassificationMeasure(ClassificationMeasure.Correlation)));
        params.setPruningMeasure(generateQualityMeasure(MeasureDestination.PRUNING, params.getInductionMeasure()));
        params.setVotingMeasure(generateQualityMeasure(MeasureDestination.VOTING, params.getInductionMeasure()));

        params.setMaximumUncoveredFraction(getParameterAsDouble(PARAMETER_MAX_UNCOVERED_FRACTION));

        params.setMinimumCovered(getParameterAsDouble(PARAMETER_MINCOV_NEW));
        params.setMaxcovNegative(getParameterAsDouble(PARAMETER_MAXCOV_NEGATIVE));
        params.setMaxRuleCount(getParameterAsInt(PARAMETER_MAX_RULE_COUNT));

        params.setEnablePruning(getParameterAsBoolean(PARAMETER_ENABLE_PRUNING));
        params.setIgnoreMissing(getParameterAsBoolean(PARAMETER_IGNORE_MISSING));
        params.setMaxGrowingConditions(getParameterAsDouble(PARAMETER_MAX_GROWING));
        params.setSelectBestCandidate(getParameterAsBoolean(PARAMETER_SELECT_BEST_CANDIDATE));
        params.setConditionComplementEnabled(getParameterAsBoolean(PARAMETER_COMPLEMENTARY_CONDITIONS));

        params.setPenaltyStrength(getParameterAsDouble(PARAMETER_PENALTY_STRENGTH));
        params.setPenaltySaturation(getParameterAsDouble(PARAMETER_PENALTY_SATURATION));
        params.setMaxPassesCount(getParameterAsInt(PARAMETER_MAX_PASSES_COUNT));
        params.setBinaryContrastIncluded(getParameterAsBoolean(PARAMETER_INCLUDE_BINARY_CONTRAST));
        params.setMeanBasedRegression(getParameterAsBoolean(PARAMETER_MEAN_BASED_REGRESSION));
        params.setControlAprioriPrecision(getParameterAsBoolean(PARAMETER_CONTROL_APRORI_PRECISION));
        params.setApproximateInduction(getParameterAsBoolean(PARAMETER_APPROXIMATE_INDUCTION));
        params.setApproximateBinsCount(getParameterAsInt(PARAMETER_APPROXIMATE_BINS_COUNT));

        String tmp = getParameterAsString(PARAMETER_MINCOV_ALL);
        if (tmp.length() > 0) {
            List<Double> mincovs = Arrays.stream(tmp.split(" +")).map(Double::parseDouble).collect(Collectors.toList());

            if (mincovs.size() == 1) {
                params.setMinimumCoveredAll(mincovs.get(0));
            } else {
                params.setMinimumCoveredAll_list(mincovs);
            }
        }

        return params;
    }
}
