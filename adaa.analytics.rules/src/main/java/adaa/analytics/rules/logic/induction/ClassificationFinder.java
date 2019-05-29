/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.induction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.Hypergeometric;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
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
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.tools.container.Pair;


/**
 * Class for growing and pruning classification rules.
 * 
 * @author Adam Gudyœ
 *
 */
public class ClassificationFinder extends AbstractFinder {

	/**
	 * Map of precalculated coverings (time optimization). 
	 * For each attribute there is a set of distinctive values. For each value there is a bit vector of examples covered.
	 */
	protected Map<Attribute, Map<Double, IntegerBitSet>> precalculatedCoverings;
	
	/**
	 * Map of precalculated attribute filters (time optimization). 
	 */
	protected Map<Attribute, Set<Double>> precalculatedFilter;
	
	/**
	 * Initializes induction parameters.
	 * @param params Induction parameters.
	 */
	public ClassificationFinder(InductionParameters params) {
		super(params);
		MissingValuesHandler.ignore = params.isIgnoreMissing();
	}
	
	/***
	 * Calculates rule quality and p-value on a training set. P-value is calculated
	 * using a hypergeometric statistical test.
	 * 
	 * @param trainSet Training set.
	 * @param ct Contingency table.
	 * @param measure Quality measure to be calculated.
	 * @return Pair containing value of rule quality measure and p-value.
	 */
	protected Pair<Double,Double> calculateQualityAndPValue(ExampleSet trainSet, ContingencyTable ct, IQualityMeasure measure) {
		Hypergeometric test = new Hypergeometric();
		Pair<Double, Double> statAndPValue = test.calculate(ct);
	
		return new Pair<Double, Double>(
				calculateQuality(trainSet, ct, measure), 
				statAndPValue.getSecond());
	}
	
	
	/**
	 * Adds elementary conditions to the rule premise until termination conditions are fulfilled.
	 * 
	 * @param rule Rule to be grown.
	 * @param dataset Training set.
	 * @param uncovered Set of positive examples yet uncovered by the model.
	 * @return Number of conditions added.
	 */
	public int grow(
		final Rule rule,
		final ExampleSet dataset,
		final Set<Integer> uncovered) {

		Logger.log("ClassificationFinder.grow()\n", Level.FINE);
		
		int initialConditionsCount = rule.getPremise().getSubconditions().size();
		
		//HashSet<Integer> covered = new HashSet<Integer>();
		Set<Integer> covered = new IntegerBitSet(dataset.size());
		
		// bit vectors for faster operations on coverings
		IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());
		IntegerBitSet coveredPositives = new IntegerBitSet(dataset.size());
		IntegerBitSet coveredNegatives = new IntegerBitSet(dataset.size());
		
		// get current covering
		ContingencyTable table = new ContingencyTable();
		rule.covers(dataset, table, coveredPositives, coveredNegatives);
		covered.addAll(coveredPositives);
		covered.addAll(coveredNegatives);
	
		Set<Attribute> allowedAttributes = new TreeSet<Attribute>(new AttributeComparator());
		for (Attribute a: dataset.getAttributes()) {
			allowedAttributes.add(a);
		}
		
		// add conditions to rule
		boolean carryOn = true;
		
		do {
			ElementaryCondition condition = induceCondition(
					rule, dataset, uncovered, covered, allowedAttributes, coveredPositives);
			
			if (condition != null) {
				carryOn = tryAddCondition(rule, condition, dataset, covered, coveredPositives, coveredNegatives, conditionCovered);
				
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
		
		if (precalculatedCoverings != null) {
			for (Attribute a: precalculatedFilter.keySet()) {
				precalculatedFilter.get(a).clear();
			}
		}
		
		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;
		rule.setInducedContitionsCount(addedConditionsCount);
		return addedConditionsCount;
	}
	
	
	/**
	 * Removes irrelevant conditions from the rule using hill-climbing strategy. 
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
		
		int examplesCount = trainSet.size();
		int conditionsCount = rule.getPremise().getSubconditions().size();
		int maskLength = (trainSet.size() + Long.SIZE - 1) / Long.SIZE; 
		long[] masks = new long[conditionsCount * maskLength]; 
		long[] labelMask = new long[maskLength];
		
		int[] conditionsPerExample = new int[trainSet.size()];
		
		
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
					++conditionsPerExample[i];
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
			final int conditionsLeft_final = conditionsLeft;
			final int[] conditionsPerExample_final = conditionsPerExample;
			
			List<Future<Double>> futures = new ArrayList<Future<Double>>(conditionsCount);
			
			// distribute over threads
			for (int cid = 0; cid < conditionsCount; ++cid) {
				final int fcid = cid;
				Future<Double> f = pool.submit( () -> {
					
					ConditionBase cnd = rule.getPremise().getSubconditions().get(fcid);
					// ignore already removed conditions
					if (removedConditions.contains(fcid)) {
						return Double.NEGATIVE_INFINITY;
					}
					
					// consider only prunable conditions
					if (!cnd.isPrunable()) {
						return Double.NEGATIVE_INFINITY;
					}
					
					double p = 0;
					double n = 0;
						
					// iterate over all words
					int id = 0;
					for (int wordId = 0; wordId < maskLength; ++wordId) {
					
						long word = masks[fcid * maskLength + wordId];
						long filteredWord = 0;
						
						for (int wordOffset = 0; wordOffset < Long.SIZE && id < examplesCount; ++wordOffset, ++id) {
							// an example is covered by rule after condition removal in two cases:
							// - it is covered by all conditions prior the removal
							// - it is covered by all conditions except the one being removed
							 
							if ((conditionsPerExample_final[id] == conditionsLeft_final) || 
								((conditionsPerExample_final[id] == conditionsLeft_final - 1) && (word & (1L << wordOffset)) == 0) ) {
								filteredWord |= 1L << wordOffset;
							}
						}

						long labelWord = labelMask[wordId];
						long posWord = filteredWord & labelWord;
						long negWord = filteredWord & ~labelWord;
						
						if (weighting) {
							// weighted - iterate over bits and sum weights
							for (int wordOffset = 0; wordOffset < Long.SIZE; ++wordOffset) {
								if ((posWord & (1L << wordOffset)) != 0) {
									p += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
								} else if ((negWord & (1L << wordOffset)) != 0) {
									n += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
								}
							}
						} else {
							
							// not weighted - bit operations
							p += Long.bitCount(posWord);
							n += Long.bitCount(negWord);
						}
					}
					
					double q = ((ClassificationMeasure)params.getPruningMeasure()).calculate(
							p, n, rule.getWeighted_P(), rule.getWeighted_N());
					
					return q;
				});
				
				futures.add(f);	
			}
			
			// gather results for conditions
			for (int cid = 0; cid < futures.size(); ++cid) {
				Future<Double> f = futures.get(cid);
				try {
					Double q = f.get();
					if (q > bestQuality) {
						bestQuality = q;
						toRemove = cid;
					}
					
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// if there is something to remove
			if (bestQuality >= initialQuality) {
				initialQuality = bestQuality;
				removedConditions.add(toRemove);
				--conditionsLeft;
				
				// decrease counters for examples covered by removed condition
				int id = 0;
				for (int wordId = 0; wordId < maskLength; ++wordId) {
					long word = masks[toRemove * maskLength + wordId];
					for (int wordOffset = 0; wordOffset < Long.SIZE && id < examplesCount; ++wordOffset, ++id) {
						
						if ((word & (1L << wordOffset)) != 0) {
							--conditionsPerExample[id];
						}
					}
				}
				
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
		rule.setWeighted_p(covering.weighted_p);
		rule.setWeighted_n(covering.weighted_n);
		Pair<Double,Double> qp = calculateQualityAndPValue(trainSet, covering, params.getVotingMeasure());
		rule.setWeight(qp.getFirst());
		rule.setPValue(qp.getSecond());
		
		//System.exit(0);
		
		return covering;	
	}

	/**
	 * Precalculates conditions coverings and stores them as bit vectors in @see precalculatedCoverings field.
	 * @param classId Class identifier.
	 * @param trainSet Training set.
	 * @param positives Set of positives examples yet uncovered by the model.
	 */
	public void precalculateConditions(int classId, ExampleSet trainSet, Set<Integer> positives) {
		
		precalculatedCoverings = new HashMap<Attribute, Map<Double, IntegerBitSet>>();
		precalculatedFilter = new HashMap<Attribute, Set<Double>>();
		Attributes attributes = trainSet.getAttributes();
		
		ExampleTable table = trainSet.getExampleTable();
		
		// iterate over all allowed decision attributes
		for (Attribute attr : attributes) {
			
			Map<Double, IntegerBitSet> attributeCovering = new TreeMap<Double, IntegerBitSet>();
			precalculatedCoverings.put(attr, attributeCovering);
			precalculatedFilter.put(attr, new TreeSet<Double>());
			
			// check if attribute is numerical or nominal
			if (attr.isNumerical()) {
				Map<Double, List<Integer>> values2ids = new TreeMap<Double, List<Integer>>();
				
				// get all distinctive values of attribute
				for (int id = 0; id < table.size(); ++id) {
					DataRow dr = table.getDataRow(id);
					double val = dr.get(attr);
					
					// exclude missing values from keypoints
					if (!Double.isNaN(val)) {
						if (!values2ids.containsKey(val)) {
							values2ids.put(val, new ArrayList<Integer>());
						} 
						values2ids.get(val).add(id);
					}
				}
				
				Double [] keys = values2ids.keySet().toArray(new Double[values2ids.size()]);
				IntegerBitSet covered = new IntegerBitSet(table.size());
				
				// check all possible midpoints (number of distinctive attribute values - 1)
				// if only one attribute value - ignore it
				for (int keyId = 0; keyId < keys.length - 1; ++keyId) {
					double key = keys[keyId];
					
					double next = keys[keyId + 1];
					double midpoint = (key + next) / 2;
					
					// this condition covers everything covered previously with some extra objects
					List<Integer> ids = values2ids.get(key);
					for (int id : ids) {
						covered.add(id);	
					}
					
					attributeCovering.put(midpoint, covered.clone());
				}
			} else { // nominal attributes
				
				// prepare bit vectors
				for (int val = 0; val != attr.getMapping().size(); ++val) {
					attributeCovering.put((double)val, new IntegerBitSet(table.size()));
				}
				
				// get all distinctive values of attribute
				for (int id = 0; id < table.size(); ++id) {
					DataRow dr = table.getDataRow(id);
					double value = dr.get(attr);
					
					// omit missing values
					if (!Double.isNaN(value)) {
						attributeCovering.get(value).add(id);
					}
				}
			}
		}
		
	}
	
	
	/**
	 * Induces an elementary condition.
	 * 
	 * @param rule Current rule.
	 * @param trainSet Training set.
	 * @param uncoveredByRuleset Set of examples uncovered by the model.
	 * @param coveredByRule Set of examples covered by the rule being grown.
	 * @param allowedAttributes Set of attributes that may be used during induction.
	 * @param extraParams Additional parameters.
	 * @return Induced elementary condition.
	 */
	@Override
	protected ElementaryCondition induceCondition(
		Rule rule,
		ExampleSet trainSet,
		Set<Integer> uncoveredPositives,
		Set<Integer> coveredByRule, 
		Set<Attribute> allowedAttributes,
		Object... extraParams) {
		
		if (allowedAttributes.size() == 0) {
			return null;
		}
		
		
		if (precalculatedCoverings != null) {
		//	return inducePrecalculatedCondition(rule, trainSet, uncoveredPositives, coveredByRule, allowedAttributes, extraParams);
		}
		
		double classId = ((SingletonSet)rule.getConsequence().getValueSet()).getValue();
		Attribute weightAttr = trainSet.getAttributes().getWeight();
		Set<Integer> positives = (Set<Integer>)extraParams[0];
		ExampleTable table = trainSet.getExampleTable();
		
		List<Future<ConditionEvaluation>> futures = new ArrayList<Future<ConditionEvaluation>>();
		
		// iterate over all allowed decision attributes
		for (Attribute attr : allowedAttributes) {
			
			// consider attributes in parallel
			Future<ConditionEvaluation> future = (Future<ConditionEvaluation>) pool.submit(() -> {
				
				ConditionEvaluation best = new ConditionEvaluation();
				
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
									
							if ((quality > best.quality || (quality == best.quality && left_p > best.covered)) && (toCover_left_p > 0)) {	
								ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(midpoint)); 
								if (checkCandidate(candidate, classId, toCover_left_p)) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + left_p + ", n=" + left_n + ", new_p=" + (double)toCover_left_p +", quality="  + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = left_p;
									best.condition = candidate;
								}
							}
						}
						
						// evaluate right-side condition: a in <v, inf)
						if (right_prec > apriori_prec) {
							double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
									right_p, right_n, rule.getWeighted_P(), rule.getWeighted_N());
							if ((quality > best.quality || (quality == best.quality && right_p > best.covered)) && (toCover_right_p > 0)) {
								ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint));
								if (checkCandidate(candidate, classId, toCover_right_p)) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + right_p + ", n=" + right_n + ", new_p=" + (double)toCover_right_p + ", quality="  + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = right_p;
									best.condition = candidate;
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
						if ((quality > best.quality || (quality == best.quality && p[i] > best.covered)) && (toCover_p[i] > 0)) {
							ElementaryCondition candidate = 
									new ElementaryCondition(attr.getName(), new SingletonSet((double)i, attr.getMapping().getValues())); 
							if (checkCandidate(candidate, classId, toCover_p[i])) {
								Logger.log("\tCurrent best: " + candidate + " (p=" + p[i] + ", n=" + n[i] + ", new_p=" + (double)toCover_p[i] + ", quality="  + quality + "\n", Level.FINEST);
								best.quality = quality;
								best.covered = p[i];
								best.condition = candidate;
							}
						}
					}
					
				}
				
				return best;

			});

			futures.add(future);
		}
		
		ConditionEvaluation best = null;
		
		try {
			for (Future f : futures) {
				ConditionEvaluation eval;
			
				eval = (ConditionEvaluation)f.get();
				if (best == null || eval.quality > best.quality || (eval.quality == best.quality && eval.covered > best.covered)) {
					best = eval;
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (best.condition != null) {
			Attribute bestAttr = trainSet.getAttributes().get(((ElementaryCondition)best.condition).getAttribute());
			if (bestAttr.isNominal()) {
				allowedAttributes.remove(bestAttr);
			}
		}

		return (ElementaryCondition)best.condition;
	}
	
	/**
	 * Induces an elementary condition using precalculated coverings.
	 * 
	 * @param rule Current rule.
	 * @param trainSet Training set.
	 * @param uncoveredByRuleset Set of examples uncovered by the model.
	 * @param coveredByRule Set of examples covered by the rule being grown.
	 * @param allowedAttributes Set of attributes that may be used during induction.
	 * @param extraParams Additional parameters.
	 * @return Induced elementary condition.
	 */
	protected ElementaryCondition inducePrecalculatedCondition(
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
		
		IntegerBitSet positives = (IntegerBitSet)extraParams[0];
		
		ExampleTable table = trainSet.getExampleTable();
		
		// iterate over all allowed decision attributes
		for (Attribute attr : allowedAttributes) {
			Map<Double, IntegerBitSet> attributeCovering = precalculatedCoverings.get(attr);
				
			Set<Double> ks = attributeCovering.keySet();
			IntegerBitSet previousConditionCovered = null;
			
			for (Double value : ks) {
				
				boolean filtered = false;
				
				if (precalculatedFilter.get(attr).contains(value)) {
				//	filtered = true;
					continue;
				}
				
				IntegerBitSet conditionCovered = attributeCovering.get(value);
						
				if (attr.isNumerical()) {
					// numerical attribute
					
					// some conditions become equivalent as rule grows (midpoints can be eliminated)
					if (previousConditionCovered != null && ((IntegerBitSet)coveredByRule).filteredCompare(previousConditionCovered, conditionCovered)) {
						precalculatedFilter.get(attr).add(value);
						previousConditionCovered = conditionCovered;
					//	filtered = true;
						continue;
					}
					
					previousConditionCovered = conditionCovered;
					
					double apriori_prec = rule.getWeighted_P() / (rule.getWeighted_P() + rule.getWeighted_N());
					
					double left_p = conditionCovered.calculateIntersectionSize(positives);
					double toCover_left_p = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule, (IntegerBitSet)uncoveredPositives);
					double left_n = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule) - left_p;
					double left_prec = left_p / (left_p + left_n);
					
					IntegerBitSet oppositeCover = conditionCovered.clone();
					oppositeCover.negate();
					
					double right_p = oppositeCover.calculateIntersectionSize(positives);
					double toCover_right_p = oppositeCover.calculateIntersectionSize((IntegerBitSet)coveredByRule, (IntegerBitSet)uncoveredPositives);
					double right_n =  oppositeCover.calculateIntersectionSize((IntegerBitSet)coveredByRule) - right_p;
					double right_prec = right_p / (right_p + right_n);
					
					// evaluate left-side condition: a in (-inf, v)
					if (left_prec > apriori_prec && (right_p + right_n != 0)) {
						double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
								left_p, left_n, rule.getWeighted_P(), rule.getWeighted_N());
						
						if ((quality > bestQuality || (quality == bestQuality && left_p > mostCovered)) && (toCover_left_p > 0)) {	
							ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(value)); 
							if (checkCandidate(candidate, classId, toCover_left_p)) {
								Logger.log("\tCurrent best: " + candidate + " (p=" + left_p + ", n=" + left_n + ", new_p=" + toCover_left_p + ", quality="  + quality + ", filtered=" + filtered + "\n", Level.FINEST);
								bestQuality = quality;
								mostCovered = left_p;
								bestCondition = candidate;
								ignoreCandidate = null;
							}
						}
					}
					
					// evaluate right-side condition: a in <v, inf)
					if (right_prec > apriori_prec && (left_p + left_n != 0)) {
						double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
								right_p, right_n, rule.getWeighted_P(), rule.getWeighted_N());
						if ((quality > bestQuality || (quality == bestQuality && right_p > mostCovered)) && (toCover_right_p > 0)) {
							ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(value));
							if (checkCandidate(candidate, classId, toCover_right_p)) {
								Logger.log("\tCurrent best: " + candidate + " (p=" + right_p + ", n=" + right_n + ", new_p=" + toCover_right_p + ", quality="  + quality + ", filtered=" + filtered +"\n", Level.FINEST);
								bestQuality = quality;
								mostCovered = right_p;
								bestCondition = candidate;
								ignoreCandidate = null;
							}
						}
					}
					
					
				} else { 
					// nominal attribute
					double p = conditionCovered.calculateIntersectionSize(positives);
					double toCover_p = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule, (IntegerBitSet)uncoveredPositives);
					double n = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule) - p;
					
					// evaluate equality condition a = v
					double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
							p, n, rule.getWeighted_P(), rule.getWeighted_N());
					if ((quality > bestQuality || (quality == bestQuality && p > mostCovered)) && (toCover_p > 0) && (p + n != coveredByRule.size())) {
						ElementaryCondition candidate = 
								new ElementaryCondition(attr.getName(), new SingletonSet(value, attr.getMapping().getValues())); 
						if (checkCandidate(candidate, classId, toCover_p)) {
							Logger.log("\tCurrent best: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + toCover_p + ", quality="  + quality + ", filtered=" + filtered + "\n", Level.FINEST);
							bestQuality = quality;
							mostCovered = p;
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
	
	
	/***
	 * Makes an attempt to add the condition to the rule.
	 * 
	 * @param rule Rule to be updated.
	 * @param condition Condition to be added.
	 * @param trainSet Training set.
	 * @param covered Set of examples covered by the rules.
	 * @param coveredPositives Bit vector of positive examples in the entire training set.
	 * @param coveredNegatives Bit vector of negative examples in the entire training set.
	 * @param conditionCovered Bit vector of examples covered by the condition.
	 * @return Flag indicating whether condition has been added successfully.
	 */
	public boolean tryAddCondition(
		final Rule rule, 
		final ConditionBase condition, 
		final ExampleSet trainSet,
		final Set<Integer> covered,
		final IntegerBitSet coveredPositives,
		final IntegerBitSet coveredNegatives,
		final IntegerBitSet conditionCovered) {
		
		boolean carryOn = true;
		boolean add = false;
		ContingencyTable ct = new ContingencyTable();
		
		if (condition != null) {
			conditionCovered.clear();
			condition.evaluate(trainSet, conditionCovered);
			
			ct.weighted_P = rule.getWeighted_P();
			ct.weighted_N = rule.getWeighted_N();
			
			if (trainSet.getAttributes().getWeight() != null) {
				// calculate weights
				
			} else {
				ct.weighted_p = coveredPositives.calculateIntersectionSize(conditionCovered);
				ct.weighted_n = coveredNegatives.calculateIntersectionSize(conditionCovered);
			}
			
			// analyse stopping criteria
			if (ct.weighted_p < params.getMinimumCovered()) {
				if (rule.getPremise().getSubconditions().size() == 0) {
					// special case of empty rule - add condition anyway
			//		add = true;
				}
				carryOn = false;
			} else {
				// exact rule
				if (ct.weighted_n == 0) { 
					carryOn = false; 
				}
				add = true;
			}
			
			// update coverage if condition was added
			if (add) {
				rule.getPremise().getSubconditions().add(condition);

				covered.retainAll(conditionCovered);
				coveredPositives.retainAll(conditionCovered);
				coveredNegatives.retainAll(conditionCovered);
				
				rule.setWeighted_p(ct.weighted_p);
				rule.setWeighted_n(ct.weighted_n);
				
				Pair<Double,Double> qp = calculateQualityAndPValue(trainSet, ct, params.getVotingMeasure());
				rule.setWeight(qp.getFirst());
				rule.setPValue(qp.getSecond());
				
				Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: " 
						+ rule.toString() + " " + rule.printStats() + "\n", Level.FINER);
			}
		}
		else {
			 carryOn = false;
		}
	
		return carryOn;
	}	
	
	/***
	 * Checks if candidate condition fulfills coverage requirement.
	 * 
	 * @param cnd Candidate condition.
	 * @param classId Class identifier.
	 * @param newlyCoveredPositives Number of newly covered positive examples after addition of the condition.
	 * @return
	 */
	protected boolean checkCandidate(ElementaryCondition cnd, double classId, double newlyCoveredPositives) {
		if (newlyCoveredPositives >= params.getMinimumCovered()) {
			return true;
		} else {
			return false;
		}
	}
}
