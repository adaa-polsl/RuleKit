package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;

public class ClassificationRuleSet extends RuleSetBase {

	private static final long serialVersionUID = -767459208536480802L;

	public static final String ATTRIBUTE_VOTING_RESULTS = "voting_results";
	
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
		
		String votingResults = "";
		
		// select decision with highest voting power 
		if (isVoting) {
			double maxVote = 0;
			for (int i = 0; i < votes.length; ++i) {
				votingResults += "" + i + ":" + votes[i] + ",";
				if (votes[i] > maxVote) {
					maxVote = votes[i];
					result = i;
				}
			}
		}
		
		example.setValue(example.getAttributes().getSpecial(ATTRIBUTE_VOTING_RESULTS), votingResults);
		
		return (double)result;
	}
	
	@Override
	protected Attribute createPredictionAttributes(ExampleSet exampleSet, Attribute label) {
		Attribute predictedLabel = super.createPredictionAttributes(exampleSet, label);
		
		ExampleTable table = exampleSet.getExampleTable();
		Attribute attr = AttributeFactory.createAttribute(ATTRIBUTE_VOTING_RESULTS, Ontology.STRING);
		table.addAttribute(attr);
		exampleSet.getAttributes().setSpecialAttribute(attr, attr.getName());
		
		return predictedLabel;
	}
}
