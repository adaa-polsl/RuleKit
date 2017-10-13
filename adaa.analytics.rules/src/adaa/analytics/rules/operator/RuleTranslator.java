package adaa.analytics.rules.operator;

import java.util.List;

import adaa.analytics.rules.logic.ClassificationRule;
import adaa.analytics.rules.logic.ClassificationRuleSet;
import adaa.analytics.rules.logic.CompoundCondition;
import adaa.analytics.rules.logic.ElementaryCondition;
import adaa.analytics.rules.logic.IValueSet;
import adaa.analytics.rules.logic.Interval;
import adaa.analytics.rules.logic.RegressionRuleSet;
import adaa.analytics.rules.logic.Rule;
import adaa.analytics.rules.logic.RuleSetBase;
import adaa.analytics.rules.logic.SingletonSet;

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
			ruleset = new ClassificationRuleSet(dataset, false, null);
		} else {
			ruleset = new RegressionRuleSet(dataset, false, null);
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
