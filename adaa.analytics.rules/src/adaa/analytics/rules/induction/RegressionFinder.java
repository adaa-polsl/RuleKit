package adaa.analytics.rules.induction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import adaa.analytics.rules.logic.ConditionBase;
import adaa.analytics.rules.logic.ElementaryCondition;
import adaa.analytics.rules.logic.Interval;
import adaa.analytics.rules.logic.Logger;
import adaa.analytics.rules.logic.Rule;
import adaa.analytics.rules.logic.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * Algorithm for growing and pruning regression rules.
 * @author Adam
 *
 */
public class RegressionFinder extends AbstractFinder {
	
	/**
	 * Helper class for storing information about evaluated condition. 
	 * @author Adam
	 *
	 */
	class ConditionEvaluation {
		public ConditionBase condition = null;
		public Covering covering = null;
		public double quality = -Double.MAX_VALUE;
		public double covered = 0;
	}
	
	public RegressionFinder(final InductionParameters params) {
		super(params);
	}
	
	protected ElementaryCondition induceCondition(
		Rule rule,
		ExampleSet dataset,
		Set<Integer> uncovered, 
		Set<Integer> covered, 
		Set<Attribute> allowedAttributes) {
		
		ConditionEvaluation bestEvaluation = new ConditionEvaluation();
		Attribute ignoreCandidate = null;
		
		// iterate over all possible decision attributes
		for (Attribute attr : allowedAttributes) {
			Logger.log("Analysing attribute: " + attr.getName() + "\n", Level.FINEST);
			
			// check if attribute is numerical or nominal
			if (attr.isNumerical()) {
				Map<Double, List<Integer>> values2ids = new TreeMap<Double, List<Integer>>();
				
				// get all distinctive values of attribute
				for (int id : covered) {
					Example ex = dataset.getExample(id);
					double val = ex.getValue(attr);
					
					if (!values2ids.containsKey(val)) {
						values2ids.put(val, new ArrayList<Integer>());
					} 
					values2ids.get(val).add(id);
				}
				
				Double [] keys = values2ids.keySet().toArray(new Double[values2ids.size()]);
	
				// check all possible midpoints
				for (int keyId = 0; keyId < keys.length - 1; ++keyId) {
					double key = keys[keyId];
					double next = keys[keyId + 1];
					double midpoint = (key + next) / 2;
					
					// evaluate left-side condition a < v
					ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(midpoint)); 
					if (checkCandidate(dataset, rule, candidate, uncovered, bestEvaluation)) {
						ignoreCandidate = null;
					}
					
					// evaluate right-side condition v <= a
					candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint)); 
					if (checkCandidate(dataset, rule, candidate, uncovered, bestEvaluation)) {
						ignoreCandidate = null;
					}
				}
			} else {
				// try all possible conditions
				for (int i = 0; i < attr.getMapping().size(); ++i) {
					ElementaryCondition candidate = new ElementaryCondition(
							attr.getName(), new SingletonSet((double)i, attr.getMapping().getValues())); 
					
					if (checkCandidate(dataset, rule, candidate, uncovered, bestEvaluation)) {
						ignoreCandidate = attr;
					}
				}
			}
		}
		
		if (ignoreCandidate != null) {
			allowedAttributes.remove(ignoreCandidate);
		}

		return (ElementaryCondition)bestEvaluation.condition;
	}

	protected boolean checkCandidate(
			ExampleSet dataset, 
			Rule rule,
			ConditionBase candidate,
			Set<Integer> uncovered, 
			ConditionEvaluation currentBest) {
		
		Logger.log("Evaluating candidate: " + candidate, Level.FINEST);
		rule.getPremise().addSubcondition(candidate);
		Covering cov = rule.covers(dataset);
		rule.getPremise().removeSubcondition(candidate);
		
		double newlyCovered = 0;
		if (dataset.getAttributes().getWeight() == null) {
			// unweighted examples
			newlyCovered = SetHelper.intersectionSize(uncovered, cov.positives) +
					SetHelper.intersectionSize(uncovered, cov.negatives);
		} else {
			// calculate weights of newly covered examples
			for (int id : cov.positives) {
				newlyCovered += uncovered.contains(id) ? dataset.getExample(id).getWeight() : 0;
			}
			for (int id : cov.negatives) {
				newlyCovered += uncovered.contains(id) ? dataset.getExample(id).getWeight() : 0;
			}
		}
		
		if (newlyCovered > 0 && newlyCovered >= params.getMinimumCovered()) {
			double quality = calculateQuality(dataset, cov, params.getInductionMeasure());
			
			Logger.log(", q=" + quality, Level.FINEST);
			
			if (quality > currentBest.quality || (quality == currentBest.quality && newlyCovered > currentBest.covered)) {
				currentBest.quality = quality;
				currentBest.condition = candidate;
				currentBest.covered = newlyCovered;
				currentBest.covering = cov;
				Logger.log(", approved!\n", Level.FINEST);
				//rule.setWeight(quality);
				return true;
			} 
		}
		
		Logger.log("\n", Level.FINEST);
		return false;
	}
}
