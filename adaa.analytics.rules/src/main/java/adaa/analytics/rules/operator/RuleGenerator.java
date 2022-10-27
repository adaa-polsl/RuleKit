/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.operator;

import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.utils.OperatorI18N;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.*;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.EqualStringCondition;
import com.rapidminer.parameter.conditions.OrParameterCondition;
import com.rapidminer.parameter.conditions.ParameterCondition;
import org.codehaus.groovy.reflection.ParameterTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The basic RuleKit learner operator. It enables inducing classification, regression,
 * and survival rules - the problem type is established automatically using metadata of 
 * the training set.
 * 
 * @author Adam Gudys
 *
 */
public class RuleGenerator extends AbstractLearner implements OperatorI18N {

	/**
	 * Auxiliary enumeration type describing possible destinations of quality measures.
	 */
	protected enum MeasureDestination {
		INDUCTION,
		PRUNING,
		VOTING
	};
	
	/**
	 * Parameter condition fulfilled for classification problems.
	 */
	protected ClassificationMetaCondition classificationMetaCondition = new ClassificationMetaCondition(this);
	
	/**
	 * Parameter condition fulfilled for regression problems.
	 */
	protected RegressionMetaCondition regressionMetaCondition = new RegressionMetaCondition(this);
	
	/**
	 * Parameter condition fulfilled for survival problems.
	 */
	protected SurvivalMetaCondition survivalMetaCondition = new SurvivalMetaCondition(this);
	
	/**
	 * Performance vector for storing model characteristics. Updated every time the operator is run.
	 */
	protected PerformanceVector performances; 
	
	/**
	 * Number/fraction of previously uncovered examples to be covered by a new rule.
	 * (positive examples for classification problems).
	 */
	public static final String PARAMETER_MINCOV_NEW = "mincov_new";

	/**
	 * Number/fraction examples to be covered by a new rule.
	 * (positive examples for classification problems).
	 */
	public static final String PARAMETER_MINCOV_ALL = "mincov_all";

	/**
	 */
	public static final String PARAMETER_MINCOV_ALL_LIST = "mincov_all_list";

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
	 * Equation of user-defined induction measure; applies only when the corresponding measure parameter has value UserDefined; 
	 * the equation must be a mathematical expression with p, n, P, N literals (elements of confusion matrix), operators, 
	 * numbers, and library functions (sin, log, etc.).
	 */
	public static final String PARAMETER_USER_INDUCTION_EQUATION = "user_induction_equation";
	
	/**
	 * Equation of user-defined pruning measure.
	 */
	public static final String PARAMETER_USER_PRUNING_EQUATION = "user_pruning_equation";
	
	/**
	 * Equation of user-defined voting measure.
	 */
	public static final String PARAMETER_USER_VOTING_EQUATION = "user_voting_equation"; 
	
	/**
	 * Boolean telling whether missing values should be ignored (by default, a missing value of given attribute is always
	 *  considered as not fulfilling the condition build upon that attribute)
	 */
	public static final String PARAMETER_IGNORE_MISSING = "ignore_missing";

	/**
	 *
	 */
	public static final String PARAMETER_MAXCOV_NEGATIVE = "maxcov_negative";

	public static final String PARAMETER_PENALTY_STRENGTH = "penalty_strength";

	public static final String PARAMETER_PENALTY_SATURATION = "penalty_saturation";

	public static final String PARAMETER_MAX_PASSES_COUNT = "max_passes_count";

	public static final String PARAMETER_INCLUDE_BINARY_CONTRAST = "include_binary_contrast";

	public static final String PARAMETER_COMPLEMENTARY_CONDITIONS = "complementary_conditions";

	/**
	 * Invokes base class constructor.
	 * @param description Operator description.
	 */
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
			params.setInductionMeasure(createMeasure(MeasureDestination.INDUCTION, new ClassificationMeasure(ClassificationMeasure.Correlation)));
			params.setPruningMeasure(createMeasure(MeasureDestination.PRUNING, params.getInductionMeasure())); 
			params.setVotingMeasure(createMeasure(MeasureDestination.VOTING, params.getInductionMeasure()));
			
			params.setMaximumUncoveredFraction(getParameterAsDouble(PARAMETER_MAX_UNCOVERED_FRACTION));

			params.setMinimumCovered(getParameterAsDouble(PARAMETER_MINCOV_NEW));
			params.setMinimumCoveredAll(getParameterAsDouble(PARAMETER_MINCOV_ALL));
			params.setMaxcovNegative(getParameterAsDouble(PARAMETER_MAXCOV_NEGATIVE));

			params.setEnablePruning(getParameterAsBoolean(PARAMETER_ENABLE_PRUNING));
			params.setIgnoreMissing(getParameterAsBoolean(PARAMETER_IGNORE_MISSING));
			params.setMaxGrowingConditions(getParameterAsDouble(PARAMETER_MAX_GROWING));
			params.setSelectBestCandidate(getParameterAsBoolean(PARAMETER_SELECT_BEST_CANDIDATE));
			params.setConditionComplementEnabled(getParameterAsBoolean(PARAMETER_COMPLEMENTARY_CONDITIONS));

			params.setPenaltyStrength(getParameterAsDouble(PARAMETER_PENALTY_STRENGTH));
			params.setPenaltySaturation(getParameterAsDouble(PARAMETER_PENALTY_SATURATION));
			params.setMaxPassesCount(getParameterAsInt(PARAMETER_MAX_PASSES_COUNT));
			params.setBinaryContrastIncluded(getParameterAsBoolean(PARAMETER_INCLUDE_BINARY_CONTRAST));

			String tmp = getParameterAsString(PARAMETER_MINCOV_ALL_LIST);
			if (tmp.length() > 0) {
				List<Double> mincovs = Arrays.stream(tmp.split(" +")).map(Double::parseDouble).collect(Collectors.toList());
				params.setMinimumCoveredAll_list(mincovs);
			}

			AbstractSeparateAndConquer snc;
			AbstractFinder finder;

			Attribute contrastAttr = null;

			if (exampleSet.getAnnotations().containsKey(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
				contrastAttr = exampleSet.getAttributes().get(exampleSet.getAnnotations().get(ContrastRule.CONTRAST_ATTRIBUTE_ROLE));
			}

			// set role only when not null and different than label attribute
			if (contrastAttr != null && contrastAttr != exampleSet.getAttributes().getLabel()) {
				exampleSet.getAttributes().setSpecialAttribute(contrastAttr, ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
			}

			if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
				// survival problem
			//	if (getParameterAsBoolean(PARAMETER_LOGRANK_SURVIVAL)) {
					params.setInductionMeasure(new LogRank());
					params.setPruningMeasure(new LogRank());
					params.setVotingMeasure(new LogRank());
					finder = contrastAttr != null
							? new ContrastSurvivalFinder(params)
							: new SurvivalLogRankFinder(params);
					snc = new SurvivalLogRankSnC((SurvivalLogRankFinder)finder, params);
			//	} else {
			//		ClassificationFinder finder = new ClassificationFinder(params);
			//		snc = new SurvivalClassificationSnC(finder, params);
			//	}
			} else if (exampleSet.getAttributes().getLabel().isNumerical()) {
				// regression problem
				finder = contrastAttr != null
						? new ContrastRegressionFinder(params)
						: new RegressionFinder(params);

				snc = new RegressionSnC((RegressionFinder) finder, params);
			} else {
				// classification problem
				finder = contrastAttr != null
						? new ContrastClassificationFinder(params)
						: new ClassificationFinder(params);
				snc = new ClassificationSnC((ClassificationFinder) finder, params);
			}

			// overwrite snc for contrast sets
			if (contrastAttr != null) {
				params.setConditionComplementEnabled(true);
				params.setSelectBestCandidate(true);
				snc = new ContrastSnC(finder, params);
			}

			double beginTime = System.nanoTime();
			RuleSetBase rs = snc.run(exampleSet);
			rs.setTotalTime((System.nanoTime() - beginTime) / 1e9);

			performances = recalculatePerformance(rs);
			model = rs;
			
			finder.close();
			
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

		InductionParameters defaultParams = new InductionParameters();


		// those parameters are the same for regression, classification, and survival
		types.add(new ParameterTypeDouble(PARAMETER_MINCOV_NEW, getParameterDescription(PARAMETER_MINCOV_NEW),
				0, Double.MAX_VALUE, defaultParams.getMinimumCovered()));

		types.add(new ParameterTypeDouble(PARAMETER_MINCOV_ALL, getParameterDescription(PARAMETER_MINCOV_ALL),
				0, Double.MAX_VALUE, defaultParams.getMinimumCoveredAll()));

		types.add(new ParameterTypeDouble(PARAMETER_MAX_UNCOVERED_FRACTION, getParameterDescription(PARAMETER_MAX_UNCOVERED_FRACTION),
				0, Double.MAX_VALUE, defaultParams.getMaximumUncoveredFraction()));

		types.add(new ParameterTypeDouble(PARAMETER_MAX_GROWING, getParameterDescription(PARAMETER_MAX_GROWING),
				0, Double.MAX_VALUE, defaultParams.getMaxGrowingConditions()));

		types.add(new ParameterTypeBoolean(PARAMETER_SELECT_BEST_CANDIDATE, getParameterDescription(PARAMETER_SELECT_BEST_CANDIDATE),
				defaultParams.getSelectBestCandidate()));

		types.add(new ParameterTypeBoolean(PARAMETER_COMPLEMENTARY_CONDITIONS, getParameterDescription(PARAMETER_COMPLEMENTARY_CONDITIONS),
				defaultParams.isConditionComplementEnabled()));


		// add measures only when log rank flag is not set
		ParameterCondition measuresCondition = new OrParameterCondition(this, false,
				classificationMetaCondition,
				regressionMetaCondition);
//				new BooleanParameterCondition(this, PARAMETER_LOGRANK_SURVIVAL, false, false)
		
		tmp = new ParameterTypeStringCategory(
				PARAMETER_INDUCTION_MEASURE, getParameterDescription(PARAMETER_INDUCTION_MEASURE),
				ClassificationMeasure.NAMES, defaultParams.getInductionMeasure().getName(), false);
		tmp.registerDependencyCondition(measuresCondition);
		types.add(tmp);

		//tmp = new ParameterTypeBoolean(PARAMETER_USER_EQUATION,getParameterDescription(PARAMETER_USER_EQUATION),true);
		//tmp = new ParameterTypeConfiguration(RuleGeneratorWizard.class, this);
		//tmp.setKey(PARAMETER_USER_EQUATION);
		tmp = new ParameterTypeString(PARAMETER_USER_INDUCTION_EQUATION, getParameterDescription(PARAMETER_USER_INDUCTION_EQUATION),true);
		tmp.registerDependencyCondition(new EqualStringCondition(this,PARAMETER_INDUCTION_MEASURE,true,"UserDefined"));
		types.add(tmp);

		tmp = new ParameterTypeBoolean(
				PARAMETER_ENABLE_PRUNING, getParameterDescription(PARAMETER_ENABLE_PRUNING),
				defaultParams.isPruningEnabled());
		types.add(tmp);
		tmp.registerDependencyCondition(measuresCondition);
		
		tmp = new ParameterTypeStringCategory(
				PARAMETER_PRUNING_MEASURE, getParameterDescription(PARAMETER_PRUNING_MEASURE),
				ClassificationMeasure.NAMES, defaultParams.getPruningMeasure().getName(), false);
		tmp.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_ENABLE_PRUNING, true, true));
		types.add(tmp);
		tmp = new ParameterTypeString(PARAMETER_USER_PRUNING_EQUATION, getParameterDescription(PARAMETER_USER_PRUNING_EQUATION),true);
		tmp.registerDependencyCondition(new EqualStringCondition(this,PARAMETER_PRUNING_MEASURE,true,"UserDefined"));
		types.add(tmp);

		tmp = new ParameterTypeStringCategory(
				PARAMETER_VOTING_MEASURE, getParameterDescription(PARAMETER_VOTING_MEASURE),
				ClassificationMeasure.NAMES, defaultParams.getVotingMeasure().getName(), false);
		tmp.registerDependencyCondition(measuresCondition);
		types.add(tmp);
		tmp = new ParameterTypeString(PARAMETER_USER_VOTING_EQUATION, getParameterDescription(PARAMETER_USER_VOTING_EQUATION),true);
		tmp.registerDependencyCondition(new EqualStringCondition(this,PARAMETER_VOTING_MEASURE,true,"UserDefined"));
		types.add(tmp);
		
		types.add(new ParameterTypeBoolean(PARAMETER_IGNORE_MISSING, getParameterDescription(PARAMETER_IGNORE_MISSING),
				defaultParams.isIgnoreMissing()));

		tmp = new ParameterTypeDouble(PARAMETER_MAXCOV_NEGATIVE, getParameterDescription(PARAMETER_MAXCOV_NEGATIVE),
				0, Double.MAX_VALUE, defaultParams.getMaxcovNegative());
		types.add(tmp);

		tmp = new ParameterTypeDouble(PARAMETER_PENALTY_STRENGTH, getParameterDescription(PARAMETER_PENALTY_STRENGTH),
				0, Double.MAX_VALUE, defaultParams.getPenaltyStrength());
		types.add(tmp);

		tmp = new ParameterTypeDouble(
				PARAMETER_PENALTY_SATURATION, getParameterDescription(PARAMETER_PENALTY_SATURATION),
				0, Double.MAX_VALUE, defaultParams.getPenaltySaturation());
		types.add(tmp);

		types.add(new ParameterTypeInt(PARAMETER_MAX_PASSES_COUNT, getParameterDescription(PARAMETER_MAX_PASSES_COUNT),
				1, Integer.MAX_VALUE, defaultParams.getMaxPassesCount()));

		tmp = new ParameterTypeString(PARAMETER_MINCOV_ALL_LIST, getParameterDescription(PARAMETER_MINCOV_ALL), "");
		types.add(tmp);

		return types;
    }
	 
	 /**
	  * Creates instance of a quality measure object.
	  * @param destination Destination of measure (growing/pruning/voting).
	  * @param defaultMeasure Default measure.
	  * @return New quality measure.
	  * @throws OperatorException
	  * @throws IllegalAccessException
	  */
	 protected IQualityMeasure createMeasure(MeasureDestination destination, IQualityMeasure defaultMeasure) throws OperatorException, IllegalAccessException {
		
		 String measureName;
		 String equation;
		 
		 if (destination == MeasureDestination.INDUCTION) {
			 measureName = getParameterAsString(PARAMETER_INDUCTION_MEASURE);
			 equation = getParameter(PARAMETER_USER_INDUCTION_EQUATION);
		 } else if (destination == MeasureDestination.PRUNING) {
			 measureName = getParameterAsString(PARAMETER_PRUNING_MEASURE);
			 equation = getParameter(PARAMETER_USER_PRUNING_EQUATION);
		 } else {
			 measureName = getParameterAsString(PARAMETER_VOTING_MEASURE);
			 equation = getParameter(PARAMETER_USER_VOTING_EQUATION);
		 }
		 
		int variant = -1;
		for (int i = 0; i < ClassificationMeasure.NAMES.length; i++) {
			if (ClassificationMeasure.getName(i).equals(measureName)) {
				variant = i;
			}
		}
		if (variant != -1) {
			ClassificationMeasure classificationMeasure = new ClassificationMeasure(variant);
			String userMeasure = ClassificationMeasure.getName(ClassificationMeasure.UserDefined);
			if (measureName.equals(userMeasure)) {
				classificationMeasure.createUserMeasure(equation);
			}
			return classificationMeasure;
		} else {
			log("No quality measure defined, using default (" + defaultMeasure.getName() + ")");
			return defaultMeasure;
		}
	}
	 
	 /**
	  * Calculates rule model characteristics.
	  * @param rs Rule set to be investigated.
	  * @return Performance vector with model characteristics.
	  */
	 public static PerformanceVector recalculatePerformance(RuleSetBase rs) {
		PerformanceVector pv = new PerformanceVector();
		
		pv.addCriterion(new EstimatedPerformance("time_total_s", rs.getTotalTime(), 1, false));
		pv.addCriterion(new EstimatedPerformance("time_growing_s", rs.getGrowingTime(), 1, false));
		pv.addCriterion(new EstimatedPerformance("time_pruning_s", rs.getPruningTime(), 1, false));
		
		pv.addCriterion(new EstimatedPerformance("#rules", rs.getRules().size(), 1, false));
		pv.addCriterion(new EstimatedPerformance("#conditions_per_rule", rs.calculateConditionsCount(), 1, false));
		pv.addCriterion(new EstimatedPerformance("#induced_conditions_per_rule", rs.calculateInducedCondtionsCount(), 1, false));

		if (rs instanceof ContrastRuleSet) {
			ContrastRuleSet crs = (ContrastRuleSet)rs;

			ContrastIndicators indicators = crs.calculateAvgContrastIndicators();

			for (String k: indicators.values.keySet()) {
				pv.addCriterion(new EstimatedPerformance(k, indicators.get(k), 1, false));
			}

			double[] stats = crs.calculateAttributeStats();
			pv.addCriterion(new EstimatedPerformance("attribute_occurence", stats[0], 1, false));
			pv.addCriterion(new EstimatedPerformance("redundancy", stats[1], 1, false));
			pv.addCriterion(new EstimatedPerformance("total_duplicates", crs.getTotalDuplicates(), 1, false));
		} else {
			pv.addCriterion(new EstimatedPerformance("avg_rule_coverage", rs.calculateAvgRuleCoverage(), 1, false));
			pv.addCriterion(new EstimatedPerformance("avg_rule_precision", rs.calculateAvgRulePrecision(), 1, false));
			pv.addCriterion(new EstimatedPerformance("avg_rule_quality", rs.calculateAvgRuleQuality(), 1, false));

			pv.addCriterion(new EstimatedPerformance("avg_pvalue", rs.calculateSignificance(0.05).p, 1, false));
			pv.addCriterion(new EstimatedPerformance("avg_FDR_pvalue", rs.calculateSignificanceFDR(0.05).p, 1, false));
			pv.addCriterion(new EstimatedPerformance("avg_FWER_pvalue", rs.calculateSignificanceFWER(0.05).p, 1, false));

			pv.addCriterion(new EstimatedPerformance("fraction_0.05_significant", rs.calculateSignificance(0.05).fraction, 1, false));
			pv.addCriterion(new EstimatedPerformance("fraction_0.05_FDR_significant", rs.calculateSignificanceFDR(0.05).fraction, 1, false));
			pv.addCriterion(new EstimatedPerformance("fraction_0.05_FWER_significant", rs.calculateSignificanceFWER(0.05).fraction, 1, false));
		}
		return pv;
	 }
}
