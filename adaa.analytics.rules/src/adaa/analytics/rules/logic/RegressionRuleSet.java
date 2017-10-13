package adaa.analytics.rules.logic;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

public class RegressionRuleSet extends RuleSetBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -676053943659766492L;
	
	protected double defaultValue = -1;
	
	public double getDefaultValue() { return defaultValue; }
	public void setDefaultValue(double defaultValue) { this.defaultValue = defaultValue; }
	

	public RegressionRuleSet(ExampleSet exampleSet, boolean isVoting, Knowledge knowledge) {
		super(exampleSet, isVoting, knowledge);
	}

	@Override
	public double predict(Example example) throws OperatorException {
		double result = 0.0;
		double weightSum = 0.0;
		
		for (Rule rule : rules) {
			if (rule.getPremise().evaluate(example)) {
				ConditionBase c = rule.getConsequence();
				double partial = Double.NaN;
				if (c instanceof ElementaryCondition) {
					ElementaryCondition consequence = ((ElementaryCondition)c);
					SingletonSet d = (SingletonSet)consequence.getValueSet();
					partial = d.getValue();
				}
				
				result += partial * rule.getWeight();
				weightSum += rule.getWeight();
			}
		}
		
		return (weightSum > 0) ? (result / weightSum) : defaultValue;
	}

}
