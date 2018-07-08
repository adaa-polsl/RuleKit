package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.representation.ClassificationRuleSet;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.MeasuredPerformance;
import com.rapidminer.tools.math.Averagable;

public class ClassificationRulesPerformance extends MeasuredPerformance {

	public static final int VOTING_CONFLICTS = 1;
	
	public static final int COVERING_RULES = 2;
	
	public static final int NEGATIVE_VOTING_CONFLICTS = 3;
	
	private int type;
	
	private double value = 0;
	
	public ClassificationRulesPerformance(int type) {
		this.type = type;
	}
	
	 @Override
	 public void startCounting(ExampleSet testSet, boolean useExampleWeights) throws OperatorException {
		 
		 double conflictWeights = 0;
		 double covWeights = 0;
		 int conflictCounts = 0;
		 int covCounts = 0;
		 
		 for (Example e: testSet) {
			 
			 String[] votingWeights = e.getValueAsString(e.getAttributes().getSpecial(ClassificationRuleSet.ATTRIBUTE_VOTING_RESULTS_WEIGHTS)).split(" ");
			 String[] votingCounts = e.getValueAsString(e.getAttributes().getSpecial(ClassificationRuleSet.ATTRIBUTE_VOTING_RESULTS_COUNTS)).split(" ");
				
			 int label = (int)e.getLabel();
			 int numLabels = testSet.getAttributes().getLabel().getMapping().size();
			 
			 for (int i = 0; i < numLabels; ++i) {
				 if (i != label) {
					 conflictWeights += Double.parseDouble(votingWeights[i]); 
					 conflictCounts += Integer.parseInt(votingCounts[i]); 
				 }
				 covWeights += Double.parseDouble(votingWeights[i]); 
				 covCounts += Integer.parseInt(votingCounts[i]);
			 }
			 
		 }
		 
		 if (type == VOTING_CONFLICTS) {
			 value = (double)conflictCounts / testSet.size();			 
		 } else if (type == COVERING_RULES) {
			 value = (double)covCounts / testSet.size();
		 }
	 }
	
	@Override
	public void countExample(Example example) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getExampleCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getFitness() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		switch (type) {
		case VOTING_CONFLICTS: return "voting_conflicts";
		case COVERING_RULES: return "covering_rules";
		case NEGATIVE_VOTING_CONFLICTS: return "negative_voting_conflicts";
		}
		
		return null;
	}

	@Override
	public double getMikroAverage() {
		return value;
	}

	@Override
	public double getMikroVariance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void buildSingleAverage(Averagable averagable) {
		// TODO Auto-generated method stub
		
	}

}
