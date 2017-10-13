package adaa.analytics.rules.experiments;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.SurvivalPerformanceEvaluator;
import adaa.analytics.rules.stream.RuleGeneratorFromStream;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.BinaryClassificationPerformance;
import com.rapidminer.operator.performance.BinominalClassificationPerformanceEvaluator;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceEvaluator;
import com.rapidminer.operator.performance.PolynominalClassificationPerformanceEvaluator;
import com.rapidminer.operator.performance.RegressionPerformanceEvaluator;
import com.rapidminer.tools.OperatorService;

public abstract class ExperimentBase implements Runnable {
	public enum Type {
		CLASSIFICATION,
		BINARY_CLASSIFICATION,
		REGRESSION,
		SURVIVAL_BY_CLASSIFICATION,
		SURVIVAL_BY_REGRESSION
	}
	
	protected AbstractPerformanceEvaluator validationEvaluator;
	protected AbstractPerformanceEvaluator globalEvaluator;
	
	protected com.rapidminer.Process process;
	
	protected RuleGenerator ruleGenerator;
	
	protected Report report;
	
	protected List<Map<String,Object>> paramsSets;
	
	public ExperimentBase(
			Report report,
			Type type,
			Map<String,Object> params) {
		this(report, type, (List<Map<String,Object>>)null);
		
		this.paramsSets = new ArrayList<Map<String, Object>>();
		paramsSets.add(params);
	}
	
	public ExperimentBase (
			Report report,
			Type type,
			List<Map<String,Object>> paramsSets) {

		try {
			
			this.report = report;
			this.paramsSets = paramsSets;
		 	process = new com.rapidminer.Process();
		    
			ruleGenerator = new ExpertRuleGenerator(new OperatorDescription("", "", null, null, "", null));
		//	ruleGenerator = new RuleGeneratorFromStream(new OperatorDescription("","",null,null, "", null));
			
			switch (type) {
	    	case CLASSIFICATION:
	    		validationEvaluator = (AbstractPerformanceEvaluator)OperatorService.createOperator(PolynominalClassificationPerformanceEvaluator.class);
	    		globalEvaluator = (AbstractPerformanceEvaluator)OperatorService.createOperator(PolynominalClassificationPerformanceEvaluator.class);
	    		break;
	    	case BINARY_CLASSIFICATION:
	    		validationEvaluator = (AbstractPerformanceEvaluator)OperatorService.createOperator(BinominalClassificationPerformanceEvaluator.class);
	    		globalEvaluator = (AbstractPerformanceEvaluator)OperatorService.createOperator(BinominalClassificationPerformanceEvaluator.class);
	    		break;
	    	case REGRESSION:
	    		validationEvaluator = (AbstractPerformanceEvaluator)OperatorService.createOperator(RegressionPerformanceEvaluator.class); 
	    		globalEvaluator = (AbstractPerformanceEvaluator)OperatorService.createOperator(RegressionPerformanceEvaluator.class);
	    		break;
	    	case SURVIVAL_BY_CLASSIFICATION:
	    		validationEvaluator = new SurvivalPerformanceEvaluator(new OperatorDescription("", "", null, null, "", null)); 
	    		globalEvaluator = new SurvivalPerformanceEvaluator(new OperatorDescription("", "", null, null, "", null));
	    		break;
	    	case SURVIVAL_BY_REGRESSION:
	    		validationEvaluator = new SurvivalPerformanceEvaluator(new OperatorDescription("", "", null, null, "", null));
	    		globalEvaluator = new SurvivalPerformanceEvaluator(new OperatorDescription("", "", null, null, "", null));
	    		break;
	    	}
			
			List<PerformanceCriterion> criteria = validationEvaluator.getCriteria();
	    	for (PerformanceCriterion c: criteria) {
	    		validationEvaluator.setParameter(c.getName(), "true");
	    		globalEvaluator.setParameter(c.getName(), "true");
	    	}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
