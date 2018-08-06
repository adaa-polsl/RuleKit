package adaa.analytics.rules.operator;

import java.util.ArrayList;
import java.util.List;

import adaa.analytics.rules.logic.induction.AbstractSeparateAndConquer;
import adaa.analytics.rules.logic.induction.ClassificationFinder;
import adaa.analytics.rules.logic.induction.ClassificationSnC;
import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.induction.RegressionFinder;
import adaa.analytics.rules.logic.induction.RegressionSnC;
import adaa.analytics.rules.logic.induction.SurvivalClassificationSnC;
import adaa.analytics.rules.logic.induction.SurvivalLogRankFinder;
import adaa.analytics.rules.logic.induction.SurvivalLogRankSnC;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.logic.representation.SurvivalRuleSet;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.OrParameterCondition;
import com.rapidminer.parameter.conditions.ParameterCondition;

import adaa.analytics.rules.utils.OperatorI18N;;


public class RuleGenerator extends AbstractLearner implements OperatorI18N {

	protected ClassificationMetaCondition classificationMetaCondition = new ClassificationMetaCondition(this, false, this);
	protected RegressionMetaCondition regressionMetaCondition = new RegressionMetaCondition(this, false, this);
	protected SurvivalMetaCondition survivalMetaCondition = new SurvivalMetaCondition(this, true, this);
	
	public static final String PARAMETER_MIN_RULE_COVERED = "min_rule_covered";
	public static final String PARAMETER_MAX_UNCOVERED_FRACTION = "max_uncovered_fraction";
	public static final String PARAMETER_INDUCTION_MEASURE = "induction_measure";
	public static final String PARAMETER_PRUNING_ENABLED = "pruning_enabled";
	public static final String PARAMETER_PRUNING_MEASURE = "pruning_measure";
	public static final String PARAMETER_VOTING_MEASURE = "voting_measure";
	public static final String PARAMETER_USE_VOTING = "use_voting";
	public static final String PARAMETER_IGNORE_MISSING = "ignore_missing";
	public static final String PARAMETER_LOGRANK_SURVIVAL = "use_logrank";
	public static final String PARAMETER_MAX_GROWING = "max_growing";
	
	public static String[] QUALITY_MEASURE_NAMES;
	public static final int[] QUALITY_MEASURE_CLASSES = {
		ClassificationMeasure.Lift,
		ClassificationMeasure.Correlation,
		ClassificationMeasure.C2,
		ClassificationMeasure.RSS,
		ClassificationMeasure.Precision,
		ClassificationMeasure.SBayesian,
		ClassificationMeasure.BinaryEntropy
	};
	
	static {
		QUALITY_MEASURE_NAMES = new String[QUALITY_MEASURE_CLASSES.length];
		for (int i = 0; i < QUALITY_MEASURE_CLASSES.length; ++i) {
			QUALITY_MEASURE_NAMES[i] = ClassificationMeasure.getName(QUALITY_MEASURE_CLASSES[i]);
		}
	}
	
	protected PerformanceVector performances; 
	
	public RuleGenerator(OperatorDescription description) {
		super(description);	
	}
	
	@Override
	public Class<? extends PredictionModel> getModelClass() {
		return PredictionModel.class;
	}
	
	@Override
	public boolean canEstimatePerformance() {
		return true;
	};
	
	@Override
    public PerformanceVector getEstimatedPerformance() throws OperatorException {
        return performances;
    }

	@Override
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		Model model = null;
		
		try {
			InductionParameters params = new InductionParameters();
			params.setInductionMeasure(createMeasure(PARAMETER_INDUCTION_MEASURE, new ClassificationMeasure(ClassificationMeasure.Correlation)));
			params.setPruningMeasure(createMeasure(PARAMETER_INDUCTION_MEASURE, params.getInductionMeasure() )); 
			params.setVotingMeasure(createMeasure(PARAMETER_VOTING_MEASURE, params.getVotingMeasure()));
			
			params.setMaximumUncoveredFraction(getParameterAsDouble(PARAMETER_MAX_UNCOVERED_FRACTION));
			params.setMinimumCovered(getParameterAsDouble(PARAMETER_MIN_RULE_COVERED));
			params.setEnablePruning(getParameterAsBoolean(PARAMETER_PRUNING_ENABLED));
			params.setIgnoreMissing(getParameterAsBoolean(PARAMETER_IGNORE_MISSING));
			params.setMaxGrowingConditions(getParameterAsDouble(PARAMETER_MAX_GROWING));
			
			AbstractSeparateAndConquer snc; 
			
			if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
				// survival problem
				if (getParameterAsBoolean(PARAMETER_LOGRANK_SURVIVAL)) {
					params.setInductionMeasure(new LogRank());
					params.setPruningMeasure(new LogRank());
					params.setVotingMeasure(new LogRank());
					SurvivalLogRankFinder finder = new SurvivalLogRankFinder(params);
					snc = new SurvivalLogRankSnC(finder, params);
				} else {
					ClassificationFinder finder = new ClassificationFinder(params);
					snc = new SurvivalClassificationSnC(finder, params);
				}
			} else if (exampleSet.getAttributes().getLabel().isNumerical()) {
				// regression problem
				RegressionFinder finder = new RegressionFinder(params);
				snc = new RegressionSnC(finder, params);
			} else {
				// classification problem
				ClassificationFinder finder = new ClassificationFinder(params);
				snc = new ClassificationSnC(finder, params);
			}
			
			RuleSetBase rs = snc.run(exampleSet);
			performances = recalculatePerformance(rs);
			model = rs;
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}

	@Override
	public boolean supportsCapability(OperatorCapability capability) {
		switch (capability) {
		case NUMERICAL_ATTRIBUTES: return true;
		case BINOMINAL_ATTRIBUTES: return true;
		case POLYNOMINAL_ATTRIBUTES: return true;
		case POLYNOMINAL_LABEL: return true;
		case BINOMINAL_LABEL: return true;
		case NUMERICAL_LABEL: return true;
		case WEIGHTED_EXAMPLES: return true;
		case NO_LABEL: return true;
		case MISSING_VALUES: return true;
		
		default:
			break;
		}

		return false;
	}
	
	@Override
    public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = new ArrayList<ParameterType>();
		ParameterType tmp;	
		
		// those parameters are the same for regression, classification, and survival
		types.add(new ParameterTypeDouble(
				PARAMETER_MIN_RULE_COVERED, getParameterDescription(PARAMETER_MIN_RULE_COVERED), 0, Double.MAX_VALUE, 5));
		types.add(new ParameterTypeDouble(
				PARAMETER_MAX_UNCOVERED_FRACTION, getParameterDescription(PARAMETER_MAX_UNCOVERED_FRACTION), 0, Double.MAX_VALUE, 0));
		types.add(new ParameterTypeBoolean(
				PARAMETER_IGNORE_MISSING, getParameterDescription(PARAMETER_IGNORE_MISSING), false));
		types.add(new ParameterTypeDouble(
				PARAMETER_MAX_GROWING, getParameterDescription(PARAMETER_MAX_GROWING), 0, Double.MAX_VALUE, 0));
		
		
		// get log rank flag only in survival mode
		tmp = new ParameterTypeBoolean(
				PARAMETER_LOGRANK_SURVIVAL, getParameterDescription(PARAMETER_LOGRANK_SURVIVAL), true);
		tmp.registerDependencyCondition(survivalMetaCondition);
		tmp.setHidden(true);
		types.add(tmp);
		
		// add measures only when log rank flag is not set
		ParameterCondition measuresCondition = new OrParameterCondition(this, false,
				classificationMetaCondition,
				regressionMetaCondition,
				new BooleanParameterCondition(this, PARAMETER_LOGRANK_SURVIVAL, false, false));
		tmp = new ParameterTypeStringCategory(
				PARAMETER_INDUCTION_MEASURE, getParameterDescription(PARAMETER_INDUCTION_MEASURE), 
				QUALITY_MEASURE_NAMES, QUALITY_MEASURE_NAMES[0], false);
		tmp.registerDependencyCondition(measuresCondition);
		types.add(tmp);
		
		tmp = new ParameterTypeBoolean(
				PARAMETER_PRUNING_ENABLED, getParameterDescription(PARAMETER_PRUNING_ENABLED), true);
		types.add(tmp);
		tmp.registerDependencyCondition(measuresCondition);
		
		tmp = new ParameterTypeStringCategory(
				PARAMETER_PRUNING_MEASURE, getParameterDescription(PARAMETER_PRUNING_MEASURE), 
				QUALITY_MEASURE_NAMES, "", false);
		tmp.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_PRUNING_ENABLED, true, true));
		
		tmp = new ParameterTypeStringCategory(
				PARAMETER_VOTING_MEASURE, getParameterDescription(PARAMETER_VOTING_MEASURE), 
				QUALITY_MEASURE_NAMES, "", false);
		tmp.registerDependencyCondition(measuresCondition);
	
		types.add(tmp);
		
		return types;
    }
	 
	 
	 protected IQualityMeasure createMeasure(String measureParameter, IQualityMeasure defaultMeasure) throws UndefinedParameterError, IllegalAccessException {
		String measureName = getParameterAsString(measureParameter);
		int variant = -1;
		for (int i = 0; i < QUALITY_MEASURE_NAMES.length; i++) {
			if (QUALITY_MEASURE_NAMES[i].equals(measureName)) {
				variant = QUALITY_MEASURE_CLASSES[i];
			}
		}

		if (variant != -1) {	
			return new ClassificationMeasure(variant);
		} else {
			log("No quality measure defined, using default (" + defaultMeasure.getName() + ")");
			return defaultMeasure;
		}
	}
	 
	 @Override
	 public String toString() {
		
		 String msg = null;
		 
		 try {
			msg = "RuleGenerator: " + 
				PARAMETER_MIN_RULE_COVERED + "=" + getParameterAsDouble(PARAMETER_MIN_RULE_COVERED) + "; " +
				PARAMETER_INDUCTION_MEASURE + "=" + getParameter(PARAMETER_INDUCTION_MEASURE) + "; " +
				PARAMETER_PRUNING_ENABLED + "=" + getParameterAsBoolean(PARAMETER_PRUNING_ENABLED) + "; " +
				PARAMETER_PRUNING_MEASURE + "=" + getParameter(PARAMETER_PRUNING_MEASURE);
		
		} catch (UndefinedParameterError e) {
			e.printStackTrace();
		}
		
		return msg;
	 }
	 
	 public static PerformanceVector recalculatePerformance(RuleSetBase rs) {
		PerformanceVector pv = new PerformanceVector();
		pv.addCriterion(new EstimatedPerformance("#rules", rs.getRules().size(), 1, false));
		pv.addCriterion(new EstimatedPerformance("#conditions_per_rule", rs.calculateConditionsCount(), 1, false));
		pv.addCriterion(new EstimatedPerformance("#induced_conditions_per_rule", rs.calculateInducedCondtionsCount(), 1, false));
		
		pv.addCriterion(new EstimatedPerformance("avg_rule_coverage", rs.calculateAvgRuleCoverage(), 1, false));
		pv.addCriterion(new EstimatedPerformance("avg_rule_precision", rs.calculateAvgRulePrecision(), 1, false));
		pv.addCriterion(new EstimatedPerformance("avg_rule_quality", rs.calculateAvgRuleQuality(), 1, false));
		
		pv.addCriterion(new EstimatedPerformance("avg_pvalue", rs.calculateSignificance(0.05).p , 1, false));
		pv.addCriterion(new EstimatedPerformance("avg_FDR_pvalue", rs.calculateSignificanceFDR(0.05).p, 1, false));
		pv.addCriterion(new EstimatedPerformance("avg_FWER_pvalue", rs.calculateSignificanceFWER(0.05).p, 1, false));
		
		pv.addCriterion(new EstimatedPerformance("fraction_0.05_significant", rs.calculateSignificance(0.05).fraction, 1, false));
		pv.addCriterion(new EstimatedPerformance("fraction_0.05_FDR_significant", rs.calculateSignificanceFDR(0.05).fraction, 1, false));
		pv.addCriterion(new EstimatedPerformance("fraction_0.05_FWER_significant", rs.calculateSignificanceFWER(0.05).fraction, 1, false));
			
		return pv;
	 }
}
