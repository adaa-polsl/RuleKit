package adaa.analytics.rules.logic.quality;

import java.util.BitSet;

import adaa.analytics.rules.logic.representation.ClassificationRuleSet;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.MeasuredPerformance;
import com.rapidminer.tools.math.Averagable;

public class ClassificationRulesPerformance extends MeasuredPerformance {

	public static final int RULES_PER_EXAMPLE = 1;
	
	public static final int VOTING_CONFLICTS = 2;
	
	public static final int NEGATIVE_VOTING_CONFLICTS = 3;
	
	public static final int BALANCED_ACCURACY = 4;
	
	private int type;
	
	private double value = 0;
	
	public ClassificationRulesPerformance(int type) {
		this.type = type;
	}
	
	 @Override
	 public void startCounting(ExampleSet testSet, boolean useExampleWeights) throws OperatorException {
		 
		 int conflictCount = 0;
		 int negativeConflictCount = 0;
		 int covCounts = 0;
		 
		 int numClasses = testSet.getAttributes().getLabel().getMapping().size();
		 
		 int[] good = new int[numClasses];
		 int[] bad = new int[numClasses];
		 
		 for (Example e: testSet) {
			 int label = (int)e.getLabel();
			 if (label == (int)e.getPredictedLabel()) {
				 ++good[label];
			 } else {
				 ++bad[label];
			 }
			 
			 // get conflict measures
			 String[] counts = e.getValueAsString(e.getAttributes().getSpecial(ClassificationRuleSet.ATTRIBUTE_VOTING_RESULTS_COUNTS)).split(" ");
			
			 BitSet mask = new BitSet(counts.length);
			 
			 for (int i = 0; i < counts.length; ++i) {
				int k = Integer.parseInt(counts[i]); 
				covCounts += k;
				if (k > 0) {
					mask.set(i);
				}
			 }
			 
			 // when more than one bit is set - conflict
			 if (mask.cardinality() > 1) {
				 ++conflictCount;
				 if (label != (int)e.getPredictedLabel()) {
					 ++negativeConflictCount;
				 }
			 } 
		 }
		 
		 double bacc = 0;
		 for (int i = 0; i < numClasses; ++i) {
			 bacc += (double)good[i] / (good[i] + bad[i]);
		 }
		 bacc /= numClasses;
		 
		 if (type == VOTING_CONFLICTS) {
			 value = (double)conflictCount;			 
		 } else if (type == NEGATIVE_VOTING_CONFLICTS) {
			 value = (double)negativeConflictCount;	
		 } else if (type == RULES_PER_EXAMPLE) {
			 value = (double)covCounts / testSet.size();
		 } else if (type == BALANCED_ACCURACY) {
			 value = bacc;
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
		case RULES_PER_EXAMPLE: return "#rules_per_example";
		case VOTING_CONFLICTS: return "#voting_conflicts";
		case NEGATIVE_VOTING_CONFLICTS: return "#negative_voting_conflicts";
		case BALANCED_ACCURACY: return "balanced_accuracy";
		}
		
		return "unspecified_name";
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
