package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

public class ClassificationRuleSet extends RuleSetBase {

	private static final long serialVersionUID = -767459208536480802L;

	private int defaultClass = -1;
	
	public int getDefaultClass() { return defaultClass; }
	public void setDefaultClass(int defaultClass) { this.defaultClass = defaultClass; }
	
	public ClassificationRuleSet(ExampleSet exampleSet, boolean isVoting, Knowledge knowledge) {
		super(exampleSet, isVoting, knowledge);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public double predict(Example example) throws OperatorException {
		Attribute label = example.getAttributes().getLabel();
		assert(label.isNominal());
		int result = defaultClass;

		double[] votes = new double[label.getMapping().size()];
		
		for (Rule rule : rules) {
			if (rule.getPremise().evaluate(example)) {
				ConditionBase c = rule.getConsequence();
				
				if (c instanceof ElementaryCondition) {
					ElementaryCondition consequence = ((ElementaryCondition)c);
					SingletonSet d = (SingletonSet)consequence.getValueSet();
					result = (int)(Math.round(d.getValue()));
				}
				
				if (isVoting) {
					votes[result] += rule.getWeight();
				} else {
					break;
				}
			}
		}
		
		
		// select decision with highest voting power 
		if (isVoting) {
			double maxVote = 0;
			for (int i = 0; i < votes.length; ++i) {
				if (votes[i] > maxVote) {
					maxVote = votes[i];
					result = i;
				}
			}
		}
		
		return (double)result;
	}

}
