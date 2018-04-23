package adaa.analytics.rules.logic.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.RemappedExampleSet;
import com.rapidminer.operator.OperatorException;

public class SurvivalRuleSet extends RuleSetBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1186396337399240471L;

	protected KaplanMeierEstimator trainingEstimator;
	
	public KaplanMeierEstimator getTrainingEstimator() { return trainingEstimator; }
	
	

	
	public SurvivalRuleSet(ExampleSet exampleSet, boolean isVoting, Knowledge knowledge) {
		super(exampleSet, isVoting, knowledge);
		
		trainingEstimator = new KaplanMeierEstimator(exampleSet);
	}

	@Override 
	public double predict(Example example) throws OperatorException {
		throw new OperatorException("SurvivalRuleSet: use performPrediction() method instead of predict()");
	}
	
	
	/**
	 * Applies the model by creating a predicted label attribute and setting the
	 * predicted label values.
	 */
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
	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
		
		SurvivalExampleSet outSet = new SurvivalExampleSet((RemappedExampleSet)exampleSet, trainingEstimator);
		
		Iterator<Example> r = exampleSet.iterator();
		for (int i = 0; i < exampleSet.size(); ++i) {
			Example example = r.next();
			KaplanMeierEstimator kme = predictEstimator(example);
			example.setValue(predictedLabel, i);
			outSet.getEstimators()[i] = kme;
			// fixme:
		//	outSet.getEstimators()[i] = trainingEstimator;
		}
		return outSet;
	}
	
	protected KaplanMeierEstimator predictEstimator(Example example) throws OperatorException {
		List<SurvivalRule> matchingRules = new ArrayList<SurvivalRule>();
		
		for (Rule rule : rules) {
			SurvivalRule survRule = (SurvivalRule)rule; 
			if (rule.getPremise().evaluate(example)) {
				matchingRules.add(survRule);
			}
		}
		
 	    KaplanMeierEstimator pred;
 	    if (matchingRules.size() == 0) {
 	        pred = trainingEstimator;
 	    } else {
 	        KaplanMeierEstimator[] survfits = new KaplanMeierEstimator[matchingRules.size()];
 	        for (int ruleidx = 0; ruleidx < matchingRules.size(); ruleidx++) {
 	        	survfits[ruleidx] = matchingRules.get(ruleidx).getEstimator();
 	        }
 	
 	        pred = KaplanMeierEstimator.average(survfits);
 	    }
 	    
 	    return pred;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("\nEstimator:\n");	
		
		// get times from training estimator
		ArrayList<Double> times = trainingEstimator.getTimes();
		
		// build header
		sb.append("time, entire-set, ");
		for (int i = 0; i < rules.size(); ++i) {
			sb.append("r" + (i + 1) + ", ");
		}
		sb.append("\n");
		
		for (double t : times) {
			sb.append(t + ", " + trainingEstimator.getProbabilityAt(t) + ", ");
			for (Rule r: rules) {
				KaplanMeierEstimator kme = ((SurvivalRule)r).getEstimator();
				sb.append(kme.getProbabilityAt(t) + ",");
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
