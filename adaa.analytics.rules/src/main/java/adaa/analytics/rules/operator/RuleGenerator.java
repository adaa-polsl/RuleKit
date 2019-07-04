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
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.utils.OperatorI18N;
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

import java.util.ArrayList;
import java.util.List;

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
	 * Positive integer representing minimum number of previously uncovered examples to be covered by a new rule 
	 * (positive examples for classification problems).
	 */
	public static final String PARAMETER_MIN_RULE_COVERED = "min_rule_covered";
	
	/**
	 * Floating-point number from [0,1] interval representing maximum fraction of examples that may remain uncovered by the rule set.
	 */
	public static final String PARAMETER_MAX_UNCOVERED_FRACTION = "max_uncovered_fraction";
	
	/**
	 * Non-negative integer representing maximum number of conditions which can be added to the rule in the growing phase 
	 * (use this parameter for large datasets if execution time is prohibitive); 0 indicates no limit.
	 */
	public static final String PARAMETER_MAX_GROWING = "max_growing";
	
	/**
	 * Name of the rule quality measure used during growing (ignored in the survival analysis where log-rank statistics is used).
	 */
	public static final String PARAMETER_INDUCTION_MEASURE = "induction_measure";
	
	/**
	 * Binary parameter indicating whether pruning should be enabled.
	 */
	public static final String PARAMETER_PRUNING_ENABLED = "pruning_enabled";
	
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
			params.setMinimumCovered(getParameterAsDouble(PARAMETER_MIN_RULE_COVERED));
			params.setEnablePruning(getParameterAsBoolean(PARAMETER_PRUNING_ENABLED));
			params.setIgnoreMissing(getParameterAsBoolean(PARAMETER_IGNORE_MISSING));
			params.setMaxGrowingConditions(getParameterAsDouble(PARAMETER_MAX_GROWING));
			
			AbstractSeparateAndConquer snc; 
			
			if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
				// survival problem
			//	if (getParameterAsBoolean(PARAMETER_LOGRANK_SURVIVAL)) {
					params.setInductionMeasure(new LogRank());
					params.setPruningMeasure(new LogRank());
					params.setVotingMeasure(new LogRank());
					SurvivalLogRankFinder finder = new SurvivalLogRankFinder(params);
					snc = new SurvivalLogRankSnC(finder, params);
			//	} else {
			//		ClassificationFinder finder = new ClassificationFinder(params);
			//		snc = new SurvivalClassificationSnC(finder, params);
			//	}
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
		
		types.add(new ParameterTypeDouble(
				PARAMETER_MAX_GROWING, getParameterDescription(PARAMETER_MAX_GROWING), 0, Double.MAX_VALUE, 0));


		// get log rank flag only in survival mode
/*		tmp = new ParameterTypeBoolean(
				PARAMETER_LOGRANK_SURVIVAL, getParameterDescription(PARAMETER_LOGRANK_SURVIVAL), true);
		tmp.registerDependencyCondition(survivalMetaCondition);
		tmp.setHidden(true);
		types.add(tmp);
*/
		// add measures only when log rank flag is not set
		ParameterCondition measuresCondition = new OrParameterCondition(this, false,
				classificationMetaCondition,
				regressionMetaCondition);
//				new BooleanParameterCondition(this, PARAMETER_LOGRANK_SURVIVAL, false, false)
		
		tmp = new ParameterTypeStringCategory(
				PARAMETER_INDUCTION_MEASURE, getParameterDescription(PARAMETER_INDUCTION_MEASURE),
				ClassificationMeasure.NAMES, ClassificationMeasure.getName(ClassificationMeasure.Correlation), false);
		tmp.registerDependencyCondition(measuresCondition);
		types.add(tmp);

		//tmp = new ParameterTypeBoolean(PARAMETER_USER_EQUATION,getParameterDescription(PARAMETER_USER_EQUATION),true);
		//tmp = new ParameterTypeConfiguration(RuleGeneratorWizard.class, this);
		//tmp.setKey(PARAMETER_USER_EQUATION);
		tmp = new ParameterTypeString(PARAMETER_USER_INDUCTION_EQUATION, getParameterDescription(PARAMETER_USER_INDUCTION_EQUATION),true);
		tmp.registerDependencyCondition(new EqualStringCondition(this,PARAMETER_INDUCTION_MEASURE,true,"UserDefined"));
		types.add(tmp);

		tmp = new ParameterTypeBoolean(
				PARAMETER_PRUNING_ENABLED, getParameterDescription(PARAMETER_PRUNING_ENABLED), true);
		types.add(tmp);
		tmp.registerDependencyCondition(measuresCondition);
		
		tmp = new ParameterTypeStringCategory(
				PARAMETER_PRUNING_MEASURE, getParameterDescription(PARAMETER_PRUNING_MEASURE),
				ClassificationMeasure.NAMES, ClassificationMeasure.getName(ClassificationMeasure.Correlation), false);
		tmp.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_PRUNING_ENABLED, true, true));
		types.add(tmp);
		tmp = new ParameterTypeString(PARAMETER_USER_PRUNING_EQUATION, getParameterDescription(PARAMETER_USER_PRUNING_EQUATION),true);
		tmp.registerDependencyCondition(new EqualStringCondition(this,PARAMETER_PRUNING_MEASURE,true,"UserDefined"));
		types.add(tmp);

		tmp = new ParameterTypeStringCategory(
				PARAMETER_VOTING_MEASURE, getParameterDescription(PARAMETER_VOTING_MEASURE),
				ClassificationMeasure.NAMES, ClassificationMeasure.getName(ClassificationMeasure.Correlation), false);
		tmp.registerDependencyCondition(measuresCondition);
		types.add(tmp);
		tmp = new ParameterTypeString(PARAMETER_USER_VOTING_EQUATION, getParameterDescription(PARAMETER_USER_VOTING_EQUATION),true);
		tmp.registerDependencyCondition(new EqualStringCondition(this,PARAMETER_VOTING_MEASURE,true,"UserDefined"));
		types.add(tmp);
		
		types.add(new ParameterTypeBoolean(
				PARAMETER_IGNORE_MISSING, getParameterDescription(PARAMETER_IGNORE_MISSING), false));
		
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
