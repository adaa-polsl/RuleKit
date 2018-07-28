package adaa.analytics.rules.logic.induction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import adaa.analytics.rules.logic.induction.AbstractFinder.QualityAndPValue;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.Hypergeometric;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.StatisticalTestResult;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.IntegerBitSet;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.MissingValuesHandler;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;

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
	 * Grows a rule.
	 * @param rule Rule to be grown.
	 * @param trainSet Training set.
	 * @param uncovered Collection of examples yet to cover (either all or positives).
	 * @return Number of conditions added.
	 */
	public int grow(
		final Rule rule,
		final ExampleSet dataset,
		final Set<Integer> uncovered) {

		Logger.log("AbstractFinder.grow()\n", Level.FINE);
		
		int initialConditionsCount = rule.getPremise().getSubconditions().size();
		
		HashSet<Integer> covered = new HashSet<Integer>();
		
		// bit vectors for faster operations on coverings
		IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());
		IntegerBitSet positives = new IntegerBitSet(dataset.size());
		IntegerBitSet negatives = new IntegerBitSet(dataset.size());
		
		// get current covering
		Covering covering = rule.covers(dataset);
		covered.addAll(covering.positives);
		covered.addAll(covering.negatives);
		
		int id = 0;
		for (Example e : dataset) {
			if (rule.getConsequence().evaluate(e)) {
				positives.add(id);
			} else {
				negatives.add(id);
			}
			++id;
		}
	
		Set<Attribute> allowedAttributes = new TreeSet<Attribute>(new AttributeComparator());
		for (Attribute a: dataset.getAttributes()) {
			allowedAttributes.add(a);
		}
		
		// add conditions to rule
		boolean carryOn = true;
		
		do {
			ElementaryCondition condition = induceCondition(
					rule, dataset, uncovered, covered, allowedAttributes, positives);
			
			if (condition != null) {
				conditionCovered.clear();
				condition.evaluate(dataset, conditionCovered);
				rule.getPremise().addSubcondition(condition);
				
				covered.retainAll(conditionCovered);
				
				positives.retainAll(conditionCovered);
				negatives.retainAll(conditionCovered);
			
				covering.weighted_p = positives.size();
				covering.weighted_n = negatives.size();
				
				rule.setCoveringInformation(covering);
				QualityAndPValue qp = calculateQualityAndPValue(dataset, covering, params.getVotingMeasure());
				rule.setWeight(qp.quality);
				rule.setPValue(qp.pvalue);
				
				Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: " 
						+ rule.toString() + "\n", Level.FINER);
				
				if (params.getMaxGrowingConditions() > 0) {
					if (rule.getPremise().getSubconditions().size() - initialConditionsCount >= 
						params.getMaxGrowingConditions() * dataset.getAttributes().size()) {
						carryOn = false;
					}
				}
				
			} else {
				carryOn = false;
			}
			
		} while (carryOn); 
		
		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;
		rule.setInducedContitionsCount(addedConditionsCount);
		return addedConditionsCount;
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
		
		int conditionsCount = rule.getPremise().getSubconditions().size();
		int maskLength = (trainSet.size() + Long.SIZE - 1) / Long.SIZE; 
		long[] masks = new long[conditionsCount * maskLength]; 
		long[] labelMask = new long[maskLength];
		
		for (int i = 0; i < trainSet.size(); ++i) {
			Example e = trainSet.getExample(i);
			int wordId = i / Long.SIZE;
			int wordOffset = i % Long.SIZE;
			
			if (rule.getConsequence().evaluate(e)) {
				labelMask[wordId] |= 1L << wordOffset;
			}
			
			for (int m = 0; m < conditionsCount; ++m) {
				ConditionBase cnd = rule.getPremise().getSubconditions().get(m);
				if (cnd.evaluate(e)) {
					masks[m * maskLength + wordId] |= 1L << wordOffset;
				}
			}
		}

		IntegerBitSet removedConditions = new IntegerBitSet(conditionsCount);
		int conditionsLeft = rule.getPremise().getSubconditions().size();
		
		Covering covering = rule.covers(trainSet);
		double initialQuality = calculateQuality(trainSet, covering, params.getPruningMeasure());
		boolean continueClimbing = true;
		boolean weighting = (trainSet.getAttributes().getWeight() != null);
		
		while (continueClimbing) {
			int toRemove = -1;
			double bestQuality = Double.NEGATIVE_INFINITY;
			
			for (int cid = 0; cid < conditionsCount; ++cid) {
				ConditionBase cnd = rule.getPremise().getSubconditions().get(cid);
				// ignore already removed conditions
				if (removedConditions.contains(cid)) {
					continue;
				}
				
				// consider only prunable conditions
				if (!cnd.isPrunable()) {
					continue;
				}
				
				// try to remove condition from output set
				removedConditions.remove(cid);
				
				// iterate over all words
				double p = 0;
				double n = 0;
				for (int wordId = 0; wordId < maskLength; ++wordId) {
					long word = ~(0L);
					long labelWord = labelMask[wordId];
					// iterate over all present conditions
					for (int m = 0; m < conditionsCount; ++m ) {
						//int m = conditionToMask.get(other);
						if (!removedConditions.contains(m)) {
							word &= masks[m * maskLength + wordId];
						}
					}
					
					// no weighting - use popcount
					if (!weighting) {
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
				
				removedConditions.add(cid);

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
				removedConditions.add(toRemove);
				--conditionsLeft;
				
				if (conditionsLeft == 1) {
					continueClimbing = false;
				}
			} else {
				continueClimbing = false;
			}
		}
		
		CompoundCondition prunedPremise = new CompoundCondition();
	
		for (int cid = 0; cid < conditionsCount; ++cid) {
			if (!removedConditions.contains(cid)) {
				prunedPremise.addSubcondition(rule.getPremise().getSubconditions().get(cid));
			}
		}
		
		rule.setPremise(prunedPremise);
		
		covering = rule.covers(trainSet);
		rule.setCoveringInformation(covering);
		QualityAndPValue qp = calculateQualityAndPValue(trainSet, covering, params.getVotingMeasure());
		rule.setWeight(qp.quality);
		rule.setPValue(qp.pvalue);
		
		//System.exit(0);
		
		return covering;	
	}

	@Override
	protected ElementaryCondition induceCondition(
		Rule rule,
		ExampleSet trainSet,
		Set<Integer> uncoveredPositives,
		Set<Integer> coveredByRule, 
		Set<Attribute> allowedAttributes,
		Object... extraParams) {
			
		double bestQuality = -Double.MAX_VALUE;
		ElementaryCondition bestCondition = null;
		double mostCovered = 0;
		Attribute ignoreCandidate = null;
		double classId = ((SingletonSet)rule.getConsequence().getValueSet()).getValue();
		Attribute weightAttr = trainSet.getAttributes().getWeight();
		
		Set<Integer> positives = (Set<Integer>)extraParams[0];
		
		ExampleTable table = trainSet.getExampleTable();
		
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
					DataRow dr = table.getDataRow(id);
					double val = dr.get(attr);
					
					// exclude missing values from keypoints
					if (!Double.isNaN(val)) {
						if (!values2ids.containsKey(val)) {
							values2ids.put(val, new ArrayList<Integer>());
						} 
						values2ids.get(val).add(id);
						double w = (weightAttr != null) ? dr.get(weightAttr) : 1.0;
						
						// put to proper bin depending of class label 
						if (positives.contains(id)) {
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
						DataRow dr = table.getDataRow(id);
						double w = (weightAttr != null) ? dr.get(weightAttr) : 1.0;
						
						// update p and n statistics 
						if (positives.contains(id)) {
					//	if (rule.getConsequence().evaluate(ex)) {
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
					DataRow dr = table.getDataRow(id);
					double value = dr.get(attr);
					
					// omit missing values
					if (!Double.isNaN(value)) {
						int castedValue = (int)value;
						double w = (weightAttr != null) ? dr.get(weightAttr) : 1.0;
						
						if (positives.contains(id)) {
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
