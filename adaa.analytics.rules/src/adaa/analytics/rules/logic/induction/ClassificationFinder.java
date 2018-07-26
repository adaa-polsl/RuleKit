package adaa.analytics.rules.logic.induction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import adaa.analytics.rules.logic.induction.AbstractFinder.QualityAndPValue;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.Hypergeometric;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.StatisticalTestResult;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.MissingValuesHandler;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

/**
 * Algorithm for growing and pruning classification rules.
 * @author Adam
 *
 */
public class ClassificationFinder extends AbstractFinder {

	public ClassificationFinder(InductionParameters params) {
		super(params);
		MissingValuesHandler.ignore = params.isIgnoreMissing();
	}
	
	protected QualityAndPValue calculateQualityAndPValue(ExampleSet trainSet, Covering cov, IQualityMeasure measure) {
		QualityAndPValue res = new QualityAndPValue();
		res.quality = calculateQuality(trainSet, cov, measure);
		
		Hypergeometric test = new Hypergeometric();
		StatisticalTestResult sts = test.calculate(cov);
		
		res.pvalue = sts.pvalue;
		
		return res;
	}
	
	/**
	 * Removes irrelevant conditions from rule using hill-climbing strategy. 
	 * @param rule Rule to be pruned.
	 * @param trainSet Training set. 
	 * @return Updated covering object.
	 */
	public Covering prune(final Rule rule, final ExampleSet trainSet) {
		Logger.log("ClassificationFinder.prune()\n", Level.FINE);
		
		// check preconditions
		if (rule.getWeighted_p() == Double.NaN || rule.getWeighted_p() == Double.NaN ||
			rule.getWeighted_P() == Double.NaN || rule.getWeighted_N() == Double.NaN) {
			throw new IllegalArgumentException();
		}
		
		int maskCount = rule.getPremise().getSubconditions().size();
		int maskLength = (trainSet.size() + Long.SIZE - 1) / Long.SIZE; 
		long[] masks = new long[maskCount * maskLength]; 
		long[] labelMask = new long[maskLength];
		
		for (int i = 0; i < trainSet.size(); ++i) {
			Example e = trainSet.getExample(i);
			int wordId = i / Long.SIZE;
			int wordOffset = i % Long.SIZE;
			
			if (rule.getConsequence().evaluate(e)) {
				labelMask[wordId] |= 1L << wordOffset;
			}
			
			for (int m = 0; m < maskCount; ++m) {
				ConditionBase cnd = rule.getPremise().getSubconditions().get(m);
				if (cnd.evaluate(e)) {
					masks[m * maskLength + wordId] |= 1L << wordOffset;
				}
			}
		}

		boolean[] removedConditions = new boolean[rule.getPremise().getSubconditions().size()];
		int conditionsLeft = rule.getPremise().getSubconditions().size();
		
		Covering covering = rule.covers(trainSet);
		double initialQuality = calculateQuality(trainSet, covering, params.getPruningMeasure());
		boolean continueClimbing = true;
		
		while (continueClimbing) {
			int toRemove = -1;
			double bestQuality = Double.NEGATIVE_INFINITY;
			
			for (int cid = 0; cid < rule.getPremise().getSubconditions().size(); ++cid) {
				ConditionBase cnd = rule.getPremise().getSubconditions().get(cid);
				// ignore already removed conditions
				if (removedConditions[cid]) {
					continue;
				}
				
				// consider only prunable conditions
				if (!cnd.isPrunable()) {
					continue;
				}
				
				// try to remove condition from output set
				removedConditions[cid] = true;
				
				// iterate over all words
				double p = 0;
				double n = 0;
				for (int wordId = 0; wordId < maskLength; ++wordId) {
					long word = ~(0L);
					long labelWord = labelMask[wordId];
					// iterate over all present conditions
					for (int m = 0; m < rule.getPremise().getSubconditions().size(); ++m ) {
						//int m = conditionToMask.get(other);
						if (!removedConditions[m]) {
							word &= masks[m * maskLength + wordId];
						}
					}
					
					// no weighting - use popcount
					if (trainSet.getAttributes().getWeight() == null) {
						p += Long.bitCount(word & labelWord);
						n += Long.bitCount(word & ~labelWord);
					} else {
						long posWord = word & labelWord;
						long negWord = word & ~labelWord;
						for (int wordOffset = 0; wordOffset < Long.SIZE; ++wordOffset) {
							if ((posWord & (1L << wordOffset)) != 0) {
								p += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							} else if ((negWord & (1L << wordOffset)) != 0) {
								n += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							}
						}
					}
				}
				
				removedConditions[cid] = false;

				double q = ((ClassificationMeasure)params.getPruningMeasure()).calculate(
						p, n, rule.getWeighted_P(), rule.getWeighted_N());
				
				if (q > bestQuality) {
					bestQuality = q;
					toRemove = cid;
				}
			}
			
			// if there is something to remove
			if (bestQuality >= initialQuality) {
				initialQuality = bestQuality;
				removedConditions[toRemove] = true;
				--conditionsLeft;
				
				if (conditionsLeft == 1) {
					continueClimbing = false;
				}
			} else {
				continueClimbing = false;
			}
		}
		
		CompoundCondition prunedPremise = new CompoundCondition();
	
		for (int cid = 0; cid < rule.getPremise().getSubconditions().size(); ++cid) {
			if (!removedConditions[cid]) {
				prunedPremise.addSubcondition(rule.getPremise().getSubconditions().get(cid));
			}
		}
		
		rule.setPremise(prunedPremise);
		
		covering = rule.covers(trainSet);
		rule.setCoveringInformation(covering);
		QualityAndPValue qp = calculateQualityAndPValue(trainSet, covering, params.getVotingMeasure());
		rule.setWeight(qp.quality);
		rule.setPValue(qp.pvalue);
		
		return covering;
	}

	@Override
	protected ElementaryCondition induceCondition(
		Rule rule,
		ExampleSet trainSet,
		Set<Integer> uncoveredPositives,
		Set<Integer> coveredByRule, 
		Set<Attribute> allowedAttributes) {
			
		double bestQuality = -Double.MAX_VALUE;
		ElementaryCondition bestCondition = null;
		double mostCovered = 0;
		Attribute ignoreCandidate = null;
		double classId = ((SingletonSet)rule.getConsequence().getValueSet()).getValue();
		
		// iterate over all allowed decision attributes
		for (Attribute attr : allowedAttributes) {
			
			// check if attribute is numerical or nominal
			if (attr.isNumerical()) {
				Map<Double, List<Integer>> values2ids = new TreeMap<Double, List<Integer>>();
				
				// statistics from all points
				double left_p = 0;
				double left_n = 0;
				double right_p = 0;
				double right_n = 0;
				
				// statistics from points yet to cover
				int toCover_right_p = 0;
				int toCover_left_p = 0;
				
				// get all distinctive values of attribute
				for (int id : coveredByRule) {
					Example ex = trainSet.getExample(id);
					double val = ex.getValue(attr);
					
					// exclude missing values from keypoints
					if (!Double.isNaN(val)) {
						if (!values2ids.containsKey(val)) {
							values2ids.put(val, new ArrayList<Integer>());
						} 
						values2ids.get(val).add(id);
						double w = trainSet.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
						
						// put to proper bin depending of class label 
						if (rule.getConsequence().evaluate(ex)) {
							right_p += w;
							if (uncoveredPositives.contains(id)) { 
								++toCover_right_p; 
							}
						} else {
							right_n += w;
						}	
					}
				}
				
				Double [] keys = values2ids.keySet().toArray(new Double[values2ids.size()]);
	
				// check all possible midpoints (number of distinctive attribute values - 1)
				// if only one attribute value - ignore it
				for (int keyId = 0; keyId < keys.length - 1; ++keyId) {
					double key = keys[keyId];
					
					double next = keys[keyId + 1];
					double midpoint = (key + next) / 2;
					
					List<Integer> ids = values2ids.get(key);
					for (int id : ids) {
						Example ex = trainSet.getExample(id);
						double w = trainSet.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
						
						// update p and n statistics 
						if (rule.getConsequence().evaluate(ex)) {
							left_p += w;
							right_p -= w;
							if (uncoveredPositives.contains(id)) { 
								++toCover_left_p; 
								--toCover_right_p;
							}
						} else {
							left_n += w;
							right_n -= w;
						}
					}
			
					// calculate precisions
					double apriori_prec = rule.getWeighted_P() / (rule.getWeighted_P() + rule.getWeighted_N());
					double left_prec = left_p / (left_p + left_n);
					double right_prec = right_p / (right_p + right_n);
					
					// evaluate left-side condition: a in (-inf, v)
					if (left_prec > apriori_prec) {
						double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
								left_p, left_n, rule.getWeighted_P(), rule.getWeighted_N());
						
						if ((quality > bestQuality || (quality == bestQuality && left_p > mostCovered)) && (toCover_left_p > 0)) {	
							ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(midpoint)); 
							if (checkCandidate(candidate, classId, left_p + left_n)) {
								bestQuality = quality;
								mostCovered = left_p;
								bestCondition = candidate;
								ignoreCandidate = null;
							}
						}
					}
					
					// evaluate right-side condition: a in <v, inf)
					if (right_prec > apriori_prec) {
						double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
								right_p, right_n, rule.getWeighted_P(), rule.getWeighted_N());
						if ((quality > bestQuality || (quality == bestQuality && right_p > mostCovered)) && (toCover_right_p > 0)) {
							ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint));
							if (checkCandidate(candidate, classId, right_p + right_n)) {
								bestQuality = quality;
								mostCovered = right_p;
								bestCondition = candidate;
								ignoreCandidate = null;
							}
						}
					}
				}
			} else {
				// sum of positive and negative weights for all values
				double[] p = new double[attr.getMapping().size()];
				double[] n = new double[attr.getMapping().size()];
				
				int[] toCover_p = new int[attr.getMapping().size()];
				
				// get all distinctive values of attribute
				for (int id : coveredByRule) {
					Example ex = trainSet.getExample(id);
					double value = ex.getValue(attr);
					
					// omit missing values
					if (!Double.isNaN(value)) {
						int castedValue = (int)value;
						double w = trainSet.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
						
						if (rule.getConsequence().evaluate(ex)) {
							p[castedValue] += w;
							if (uncoveredPositives.contains(id)) {
								++toCover_p[castedValue];
							}
							
						} else {
							n[castedValue] += w;
						}
					}
				}
				
				// try all possible conditions
				for (int i = 0; i < attr.getMapping().size(); ++i) {
					// evaluate equality condition a = v
					double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
							p[i], n[i], rule.getWeighted_P(), rule.getWeighted_N());
					if ((quality > bestQuality || (quality == bestQuality && p[i] > mostCovered)) && (toCover_p[i] > 0)) {
						ElementaryCondition candidate = 
								new ElementaryCondition(attr.getName(), new SingletonSet((double)i, attr.getMapping().getValues())); 
						if (checkCandidate(candidate, classId, p[i] + n[i])) {
							bestQuality = quality;
							mostCovered = p[i];
							bestCondition = candidate;
							ignoreCandidate = attr;
						}
					}
				}
			}
		}

		if (ignoreCandidate != null) {
			allowedAttributes.remove(ignoreCandidate);
		}

		return bestCondition;
	}
	
	
	protected boolean checkCandidateCoverage(double covered_pn) {
		if (covered_pn >= params.getMinimumCovered()) {
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean checkCandidate(ElementaryCondition cnd, double classId, double covered_pn) {
		return checkCandidateCoverage(covered_pn);
	}
}
