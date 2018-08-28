package adaa.analytics.rules.operator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import adaa.analytics.rules.logic.quality.ClassificationRulesPerformance;
import adaa.analytics.rules.logic.quality.ExtendedBinaryPerformance;
import adaa.analytics.rules.logic.quality.IntegratedBrierScore;
import adaa.analytics.rules.logic.representation.SurvivalRule;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.AbsoluteError;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.BinaryClassificationPerformance;
import com.rapidminer.operator.performance.BinominalClassificationPerformanceEvaluator;
import com.rapidminer.operator.performance.CorrelationCriterion;
import com.rapidminer.operator.performance.CrossEntropy;
import com.rapidminer.operator.performance.LenientRelativeError;
import com.rapidminer.operator.performance.LogisticLoss;
import com.rapidminer.operator.performance.Margin;
import com.rapidminer.operator.performance.MultiClassificationPerformance;
import com.rapidminer.operator.performance.NormalizedAbsoluteError;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.performance.PolynominalClassificationPerformanceEvaluator;
import com.rapidminer.operator.performance.PredictionAverage;
import com.rapidminer.operator.performance.RegressionPerformanceEvaluator;
import com.rapidminer.operator.performance.RelativeError;
import com.rapidminer.operator.performance.RootMeanSquaredError;
import com.rapidminer.operator.performance.RootRelativeSquaredError;
import com.rapidminer.operator.performance.SoftMarginLoss;
import com.rapidminer.operator.performance.SquaredCorrelationCriterion;
import com.rapidminer.operator.performance.SquaredError;
import com.rapidminer.operator.performance.StrictRelativeError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.OrParameterCondition;
import com.rapidminer.parameter.conditions.ParameterCondition;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;

public class RulePerformanceEvaluator extends AbstractPerformanceEvaluator {
	
	/**
	 * Wrapper for performance criteria classes. Allows uniform handling of different criteria classes -
	 * a class may represent a single criterion, or several of them (this requires specifying additional integer parameter).
	 * 
	 * @author Adam
	 *
	 */
	protected static class CriterionClassWrapper {
		private Class className;
		private Integer param;
		
		public CriterionClassWrapper(Class className) {
			this.className = className;
			this.param = null;
		}
		
		public CriterionClassWrapper(Class className, Integer param) {
			this.className = className;
			this.param = param;
		}
		
		public PerformanceCriterion create() 
				throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
			if (param == null) {
				Constructor<PerformanceCriterion> cons = className.getConstructor();
				return cons.newInstance();
			} else {
				Constructor<PerformanceCriterion> cons = className.getConstructor(int.class);
				return cons.newInstance(param.intValue());
			}
		}
	};
	
	public static final int TYPE_CLASSIFICATION = 0;
	public static final int TYPE_BINARY_CLASSIFICATION = 1;
	public static final int TYPE_REGRESSION = 2;
	public static final int TYPE_SURVIVAL = 3;
	
	private static final CriterionClassWrapper[] COMMON_CRITERIA_CLASSES = {
		new CriterionClassWrapper(AbsoluteError.class),
		new CriterionClassWrapper(RelativeError.class),
		new CriterionClassWrapper(LenientRelativeError.class),
		new CriterionClassWrapper(StrictRelativeError.class),
		new CriterionClassWrapper(NormalizedAbsoluteError.class),
		new CriterionClassWrapper(RootMeanSquaredError.class),
		new CriterionClassWrapper(RootRelativeSquaredError.class),
		new CriterionClassWrapper(SquaredError.class),
		new CriterionClassWrapper(CorrelationCriterion.class),
		new CriterionClassWrapper(SquaredCorrelationCriterion.class)
	};
	
	private static final CriterionClassWrapper[] MULTICLASS_CRITERIA_CLASSES = { 
		new CriterionClassWrapper(CrossEntropy.class),
		new CriterionClassWrapper(Margin.class),
		new CriterionClassWrapper(SoftMarginLoss.class),
		new CriterionClassWrapper(LogisticLoss.class),
		new CriterionClassWrapper(MultiClassificationPerformance.class, MultiClassificationPerformance.ACCURACY),
		new CriterionClassWrapper(MultiClassificationPerformance.class, MultiClassificationPerformance.ERROR),
		new CriterionClassWrapper(MultiClassificationPerformance.class, MultiClassificationPerformance.KAPPA),
		new CriterionClassWrapper(ClassificationRulesPerformance.class, ClassificationRulesPerformance.RULES_PER_EXAMPLE),
		new CriterionClassWrapper(ClassificationRulesPerformance.class, ClassificationRulesPerformance.VOTING_CONFLICTS),
		new CriterionClassWrapper(ClassificationRulesPerformance.class, ClassificationRulesPerformance.NEGATIVE_VOTING_CONFLICTS),
		new CriterionClassWrapper(ClassificationRulesPerformance.class, ClassificationRulesPerformance.BALANCED_ACCURACY)	
	};
	
	private static final CriterionClassWrapper[] BINARY_CRITERIA_CLASSES = { 
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.PRECISION),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.RECALL),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.LIFT),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.FALLOUT),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.F_MEASURE),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.FALSE_POSITIVE),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.FALSE_NEGATIVE),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.TRUE_POSITIVE),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.TRUE_NEGATIVE),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.SENSITIVITY),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.SPECIFICITY),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.YOUDEN),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.POSITIVE_PREDICTIVE_VALUE),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.NEGATIVE_PREDICTIVE_VALUE),
		new CriterionClassWrapper(BinaryClassificationPerformance.class, BinaryClassificationPerformance.PSEP),
		new CriterionClassWrapper(ExtendedBinaryPerformance.class)
	};
	
	private static final CriterionClassWrapper[] REGRESSION_CRITERIA_CLASSES = {
		new CriterionClassWrapper(PredictionAverage.class)
	};
	
	private static final CriterionClassWrapper[] SURVIVAL_CRITERIA_CLASSES = {
		new CriterionClassWrapper(IntegratedBrierScore.class)
	};
	
	
	protected ClassificationMetaCondition classificationMetaCondition = new ClassificationMetaCondition(this, false, this);
	protected RegressionMetaCondition regressionMetaCondition = new RegressionMetaCondition(this, false, this);
	protected SurvivalMetaCondition survivalMetaCondition = new SurvivalMetaCondition(this, true, this);
	
	protected ArrayList<ArrayList<String>> criteriaNames;
			
	protected ExampleSet testSet;
	

	public RulePerformanceEvaluator(OperatorDescription description) 
			throws OperatorCreationException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {	
		super(description);
		
		criteriaNames = new ArrayList<ArrayList<String>>();
		for (int i = 0; i <= 3; ++i) {
			criteriaNames.add(new ArrayList<String>());
		}
		
		for (CriterionClassWrapper ccw : COMMON_CRITERIA_CLASSES) {
			criteriaNames.get(TYPE_CLASSIFICATION).add(ccw.create().getName());
			criteriaNames.get(TYPE_BINARY_CLASSIFICATION).add(ccw.create().getName());
			criteriaNames.get(TYPE_REGRESSION).add(ccw.create().getName());
		}

		for (CriterionClassWrapper ccw : MULTICLASS_CRITERIA_CLASSES) {
			criteriaNames.get(TYPE_CLASSIFICATION).add(ccw.create().getName());
			criteriaNames.get(TYPE_BINARY_CLASSIFICATION).add(ccw.create().getName());
		}
		
		for (CriterionClassWrapper ccw : BINARY_CRITERIA_CLASSES) {
			criteriaNames.get(TYPE_BINARY_CLASSIFICATION).add(ccw.create().getName());
		}
		
		for (CriterionClassWrapper ccw : REGRESSION_CRITERIA_CLASSES) {
			criteriaNames.get(TYPE_REGRESSION).add(ccw.create().getName());
		}
		
		for (CriterionClassWrapper ccw : SURVIVAL_CRITERIA_CLASSES) {
			criteriaNames.get(TYPE_SURVIVAL).add(ccw.create().getName());
		}
		
	}
	
	
	@Override
    public List<ParameterType> getParameterTypes() {
		List<ParameterType> params = super.getParameterTypes();
		
		return params;
	}
	
	@Override
	public List<PerformanceCriterion> getCriteria() {
		List<PerformanceCriterion> performanceCriteria = new LinkedList<PerformanceCriterion>();
		
		try {
			for (CriterionClassWrapper ccw : COMMON_CRITERIA_CLASSES) {
				performanceCriteria.add(ccw.create());
			}
	
			for (CriterionClassWrapper ccw : MULTICLASS_CRITERIA_CLASSES) {
				performanceCriteria.add(ccw.create());
			}
			
			for (CriterionClassWrapper ccw : BINARY_CRITERIA_CLASSES) {
				performanceCriteria.add(ccw.create());
			}
			
			for (CriterionClassWrapper ccw : REGRESSION_CRITERIA_CLASSES) {
				performanceCriteria.add(ccw.create());
			}
			
			for (CriterionClassWrapper ccw : SURVIVAL_CRITERIA_CLASSES) {
				performanceCriteria.add(ccw.create());
			}
		
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return performanceCriteria;
	}
	
	
	public static int determineExperimentType(ExampleSet set) {
		Attribute label = set.getAttributes().getLabel();
		if (set.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
			return TYPE_SURVIVAL;
		} else if (label.isNominal()) {
			if (label.getMapping().size() == 2) {
				return TYPE_BINARY_CLASSIFICATION;
			} else {
				return TYPE_CLASSIFICATION;
			}
			
		} else {
			return TYPE_REGRESSION;
		}
	}
	
	@Override
	protected void init(ExampleSet exampleSet) {
		testSet = exampleSet;
		
		int type = determineExperimentType(this.testSet);
		
		// disable all criteria
		for (PerformanceCriterion c: getCriteria()) {
    		setParameter(c.getName(), "false");
    	}
		
		// enable only suitable criteria
		for (String cname: criteriaNames.get(type)) {
			setParameter(cname, "true");
		}
	}
	
	
	@Override
	protected boolean canEvaluate(int valueType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected double[] getClassWeights(Attribute label)
			throws UndefinedParameterError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void checkCompatibility(ExampleSet exampleSet)
			throws OperatorException {
		// TODO Auto-generated method stub
		
	}
	
}
