package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.RemappedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;

public class ClassificationRuleSet extends RuleSetBase {

	private static final long serialVersionUID = -767459208536480802L;

	public static final String ATTRIBUTE_VOTING_RESULTS_WEIGHTS = "voting_result_weights";
	
	public static final String ATTRIBUTE_VOTING_RESULTS_COUNTS = "voting_results_count";
	
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
		int[] voteCounts = new int[label.getMapping().size()];
		
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
					++voteCounts[result];
				} else {
					break;
				}
			}
		}
		
		StringBuilder sb_weights = new StringBuilder(); 
		StringBuilder sb_counts = new StringBuilder(); 
		
		// select decision with highest voting power 
		if (isVoting) {
			double maxVote = 0;
			for (int i = 0; i < votes.length; ++i) {
				sb_weights.append(votes[i] + " ");
				sb_counts.append(voteCounts[i] + " ");
				if (votes[i] > maxVote) {
					maxVote = votes[i];
					result = i;
				}
			}
		}
		
		example.setValue(example.getAttributes().getSpecial(ATTRIBUTE_VOTING_RESULTS_WEIGHTS), sb_weights.toString());
		example.setValue(example.getAttributes().getSpecial(ATTRIBUTE_VOTING_RESULTS_COUNTS), sb_counts.toString());
		
		return (double)result;
	}
	
	@Override
	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		ExampleSet mappedExampleSet = new RemappedExampleSet(exampleSet, getTrainingHeader(), false);
        checkCompatibility(mappedExampleSet);
		Attribute predictedLabel = createPredictionAttributes(mappedExampleSet, getLabel());
		ExampleSet result = performPrediction(mappedExampleSet, predictedLabel);
		
		// Copy in order to avoid RemappedExampleSets wrapped around each other accumulating over time
		//copyPredictedLabel(result, exampleSet);
        return result;
	}
	
	@Override
	protected Attribute createPredictionAttributes(ExampleSet exampleSet, Attribute label) {
		Attribute predictedLabel = super.createPredictionAttributes(exampleSet, label);
		
		ExampleTable table = exampleSet.getExampleTable();
		Attribute attr = AttributeFactory.createAttribute(ATTRIBUTE_VOTING_RESULTS_WEIGHTS, Ontology.STRING);
		table.addAttribute(attr);
		exampleSet.getAttributes().setSpecialAttribute(attr, attr.getName());
		
		attr = AttributeFactory.createAttribute(ATTRIBUTE_VOTING_RESULTS_COUNTS, Ontology.STRING);
		table.addAttribute(attr);
		exampleSet.getAttributes().setSpecialAttribute(attr, attr.getName());
		
		return predictedLabel;
	}
}
