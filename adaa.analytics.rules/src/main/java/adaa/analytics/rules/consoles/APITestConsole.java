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
package adaa.analytics.rules.consoles;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.RulePerformanceEvaluator;
import adaa.analytics.rules.utils.RapidMiner5;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.OperatorService;
import com.rapidminer5.operator.io.ArffExampleSource;

/**
 * This console was created for testing purposes only.
 *
 * @author Adam Gudys
 */
public class APITestConsole {

	public static void main(String[] args) {
		
		try {
			RapidMiner.init();
			
			// create all operators
			ArffExampleSource trainArff = RapidMiner5.createOperator(ArffExampleSource.class);
			ArffExampleSource testArff = RapidMiner5.createOperator(ArffExampleSource.class);
	
			ChangeAttributeRole trainRoleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
	    	ChangeAttributeRole testRoleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
	    
	    	AbstractLearner ruleGenerator = RapidMiner5.createOperator(ExpertRuleGenerator.class);
	    	ModelApplier applier = OperatorService.createOperator(ModelApplier.class);
	    	AbstractPerformanceEvaluator evaluator = RapidMiner5.createOperator(RulePerformanceEvaluator.class);
	
	    	// configure process workflow
	    	com.rapidminer.Process process = new com.rapidminer.Process();
	    	process.getRootOperator().getSubprocess(0).addOperator(trainArff);
	    	process.getRootOperator().getSubprocess(0).addOperator(testArff);
	    	process.getRootOperator().getSubprocess(0).addOperator(trainRoleSetter);
	    	process.getRootOperator().getSubprocess(0).addOperator(testRoleSetter);
	    	process.getRootOperator().getSubprocess(0).addOperator(ruleGenerator);
	    	process.getRootOperator().getSubprocess(0).addOperator(applier);
	    	process.getRootOperator().getSubprocess(0).addOperator(evaluator);
	    	
	    	// training set is passed to the role setter and then to the rule generator
	      	trainArff.getOutputPorts().getPortByName("output").connectTo(trainRoleSetter.getInputPorts().getPortByName("example set input"));	
	    	trainRoleSetter.getOutputPorts().getPortByName("example set output").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
	    	
	    	// testing set is passed to the role setter and then to the model applier as unlabelled data
	    	testArff.getOutputPorts().getPortByName("output").connectTo(testRoleSetter.getInputPorts().getPortByName("example set input"));	
	    	testRoleSetter.getOutputPorts().getPortByName("example set output").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
	 	    
	    	// trained model is applied on unlabelled data
	    	ruleGenerator.getOutputPorts().getPortByName("model").connectTo(applier.getInputPorts().getPortByName("model"));
	    	
	    	// labelled data together are used for performance evaluation 
	    	applier.getOutputPorts().getPortByName("labelled data").connectTo(
	    			evaluator.getInputPorts().getPortByName("labelled data"));
	   	
	    	// model characteristics are also passed to the evaluator
	    	ruleGenerator.getOutputPorts().getPortByName("estimated performance").connectTo(
	    			evaluator.getInputPorts().getPortByName("performance"));
	    	
	    	// return model and performance from the process
	    	evaluator.getOutputPorts().getPortByName("performance").connectTo(
	    			process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
	    	applier.getOutputPorts().getPortByName("model").connectTo(
	    			process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));
			
	    	// configure all operators
	    	// set names of the input files
	    	trainArff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, "../data/deals/deals-train.arff");
			testArff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, "../data/deals/deals-test.arff");
	    	
			// use "Future Customer" as the label attribute
	    	trainRoleSetter.setParameter(trainRoleSetter.PARAMETER_NAME, "Future Customer");
	    	trainRoleSetter.setParameter(trainRoleSetter.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME); 	
	    	testRoleSetter.setParameter(testRoleSetter.PARAMETER_NAME, "Future Customer");
	    	testRoleSetter.setParameter(testRoleSetter.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
	    	
	    	// configure rule induction algorithm
	    	ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_MIN_RULE_COVERED, "8");
	    	ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_INDUCTION_MEASURE, ClassificationMeasure.getName(ClassificationMeasure.BinaryEntropy));
	    	ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_PRUNING_MEASURE, ClassificationMeasure.getName(ClassificationMeasure.UserDefined));
	    	ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_USER_PRUNING_EQUATION, "2 * p / n");
	    	ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_VOTING_MEASURE, ClassificationMeasure.getName(ClassificationMeasure.C2));
	    	
	    	// run process and get its results
	    	IOContainer out = process.run();
			IOObject[] objs = out.getIOObjects();
		
	    	PerformanceVector performance = (PerformanceVector)objs[0];	
	    	Model model = (Model)objs[1];

	    	System.out.print(performance);
	    	System.out.print(model);
	    	
		} catch (OperatorCreationException | OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
