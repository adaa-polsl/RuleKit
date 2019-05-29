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
	
	public ExperimentBase (
			SynchronizedReport qualityReport,
			SynchronizedReport modelReport) {

		try {
			
			this.qualityReport = qualityReport;
			this.modelReport = modelReport;
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
