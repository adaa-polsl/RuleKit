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

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.rules.RuleModel;
import com.rapidminer.operator.learner.tree.SplitCondition;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.PassThroughRule;

import java.util.List;

/**
 * Class translating rules generated by RapidMiner SimpleRuleLearner (PRISM algorithm implementation) to RuleKit
 * format.
 *
 * @author Adam Gudys
 */
public class RuleTranslator extends Operator {
	
	static String LESS = "<";
	static String LESS_EQUAL = "\u2264";
	static String GREATER = ">";
	static String GREATER_EQUAL = "\u2265";
	
	private InputPort modelInput = getInputPorts().createPort("rapidminer rules", RuleModel.class);
	private OutputPort modelOutput = getOutputPorts().createPort("disesor rules");
	private InputPort exampleSetInput = getInputPorts().createPort("example set", ExampleSet.class);
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	
	protected PerformanceVector performances; 
	
	
	public RuleTranslator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new PassThroughRule(exampleSetInput, exampleSetOutput, true));
		getTransformer().addRule(new GenerateNewMDRule(modelOutput, new MetaData(RuleModel.class)));
		getTransformer().addRule(new GenerateNewMDRule(exampleSetOutput, new MetaData(ExampleSet.class)));
	}
	
	@Override
	public void doWork() throws OperatorException {
		RuleModel model = modelInput.getData(RuleModel.class);
		ExampleSet dataset = exampleSetInput.getData(ExampleSet.class);
		
		int rulesCount = 0;
		int conditionsCount = 0;
		
		// create rule set
		RuleSetBase ruleset;
		if (dataset.getAttributes().getLabel().isNominal()) {
			ruleset = new ClassificationRuleSet(dataset, false, null, null);
		} else {
			ruleset = new RegressionRuleSet(dataset, false, null, null);
		}
		
		// perform rule translation
		for (com.rapidminer.operator.learner.rules.Rule rule : model.getRules()) {
			List<SplitCondition> conditions = rule.getTerms();
			
			// form premise
			CompoundCondition premise = new CompoundCondition();
			for (SplitCondition sc : conditions) {
				Attribute attribute = dataset.getAttributes().get(sc.getAttributeName());
				String stringValue = sc.getValueString();
				IValueSet valueSet = null;
				
				String relation = sc.getRelation();
				
				double value;
				if (attribute.isNominal()) {
					value = attribute.getMapping().getIndex(stringValue);
					valueSet = new SingletonSet(value, attribute.getMapping().getValues());
				} else {
					value = Double.parseDouble(stringValue);
					if (relation.equals(GREATER)) {
						valueSet = new Interval(value, Double.POSITIVE_INFINITY, false, false);
					} else if (relation.equals(GREATER_EQUAL)) {
						valueSet = new Interval(value, Double.POSITIVE_INFINITY, true, false);
					} else if (relation.equals(LESS)) {
						valueSet = new Interval(Double.NEGATIVE_INFINITY, value, false, false);
					} else if (relation.equals(LESS_EQUAL)) {
						valueSet = new Interval(Double.NEGATIVE_INFINITY, value, false, true);
					}	
				}
				
				ElementaryCondition ec = new ElementaryCondition(attribute.getName(), valueSet);
				premise.addSubcondition(ec);
				++conditionsCount;
			}
			
			// form consequence
			String stringValue = rule.getLabel();
			Attribute attribute = dataset.getAttributes().getLabel();
			double value;
			
			if (attribute.isNominal()) {
				value = attribute.getMapping().getIndex(stringValue);
			} else {
				value = Double.parseDouble(stringValue);
			}
			IValueSet valueSet = new SingletonSet(value, attribute.getMapping().getValues()); 
			ElementaryCondition consequence = new ElementaryCondition(attribute.getName(), valueSet);
			
			// generate newRule
			Rule newRule = new ClassificationRule(premise, consequence);
			ruleset.addRule(newRule);
			++rulesCount;
		}
		
		performances = new PerformanceVector();
		performances.addCriterion(new EstimatedPerformance("rules count", rulesCount, 1, false));
		performances.addCriterion(new EstimatedPerformance("conditions count", conditionsCount, 1, false));
		
		modelOutput.deliver(ruleset);
		exampleSetOutput.deliver(dataset);
	}
}
