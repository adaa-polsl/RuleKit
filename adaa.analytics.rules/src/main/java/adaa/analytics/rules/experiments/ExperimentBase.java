package adaa.analytics.rules.experiments;

import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.RulePerformanceEvaluator;
import adaa.analytics.rules.utils.RapidMiner5;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	protected SynchronizedReport qualityReport;
	
	protected SynchronizedReport modelReport;
	
	protected List<Map<String,Object>> paramsSets;
	
	public ExperimentBase(
			SynchronizedReport qualityReport,
			SynchronizedReport modelReport,
			Map<String,Object> params) {
		this(qualityReport, modelReport, (List<Map<String,Object>>)null);
		
		this.paramsSets = new ArrayList<Map<String, Object>>();
		paramsSets.add(params);
	}
	
	public ExperimentBase (
			SynchronizedReport qualityReport,
			SynchronizedReport modelReport,
			List<Map<String,Object>> paramsSets) {

		try {
			
			this.qualityReport = qualityReport;
			this.modelReport = modelReport;
			this.paramsSets = paramsSets;
		 	process = new com.rapidminer.Process();
		    
			ruleGenerator = RapidMiner5.createOperator(ExpertRuleGenerator.class);
		//	ruleGenerator = new RuleGeneratorFromStream(new OperatorDescription("","",null,null, "", null));
			
    		validationEvaluator = RapidMiner5.createOperator(RulePerformanceEvaluator.class);
    		globalEvaluator = RapidMiner5.createOperator(RulePerformanceEvaluator.class);
	    	
	/*		List<PerformanceCriterion> criteria = validationEvaluator.getCriteria();
	    	for (PerformanceCriterion c: criteria) {
	    		validationEvaluator.setParameter(c.getName(), "true");
	    		globalEvaluator.setParameter(c.getName(), "true");
	    	}
	    	*/
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
