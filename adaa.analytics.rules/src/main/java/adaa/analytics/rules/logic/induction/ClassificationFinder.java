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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.Hypergeometric;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.representation.*;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.tools.container.Pair;


/**
 * Class for growing and pruning classification rules.
 * 
 * @author Adam Gudys
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
	 * If example set is unweighted, method precalculates conditions coverings and stores
	 * them as bit vectors in @see precalculatedCoverings field.
	 * @param trainSet Training set.
	 */
	public void preprocess(ExampleSet trainSet) {
	
		// do nothing for weighted datasets
		if (trainSet.getAttributes().getWeight() != null) {
			return;
		}

		precalculatedCoverings = new HashMap<Attribute, Map<Double, IntegerBitSet>>();
		precalculatedFilter = new HashMap<Attribute, Set<Double>>();
		Attributes attributes = trainSet.getAttributes();

		List<Future> futures = new ArrayList<Future>();

		// iterate over all allowed decision attributes
		for (Attribute attr : attributes) {

			Future f = pool.submit( () -> {
				Map<Double, IntegerBitSet> attributeCovering = new TreeMap<Double, IntegerBitSet>();

				// check if attribute is nominal
				if (attr.isNominal()) {
					// prepare bit vectors
					for (int val = 0; val != attr.getMapping().size(); ++val) {
						attributeCovering.put((double) val, new IntegerBitSet(trainSet.size()));
					}

					// get all distinctive values of attribute
					int id = 0;
					for (Example e : trainSet) {
						DataRow dr = e.getDataRow();
						double value = dr.get(attr);

						// omit missing values
						if (!Double.isNaN(value)) {
							attributeCovering.get(value).add(id);
						}
						++id;
					}
				}

				synchronized (this) {
					precalculatedCoverings.put(attr, attributeCovering);
					precalculatedFilter.put(attr, new TreeSet<Double>());
				}
			});

			futures.add(f);
		}

		try {
			for (Future f : futures) {
				f.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
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
		covered.addAll(rule.getCoveredPositives());
		covered.addAll(rule.getCoveredNegatives());

		// bit vectors for faster operations on coverings
		IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());

		Set<Attribute> allowedAttributes = new TreeSet<Attribute>(new AttributeComparator());
		for (Attribute a: dataset.getAttributes()) {
			allowedAttributes.add(a);
		}
		
		// add conditions to rule
		boolean carryOn = true;
		Rule currentRule = new ClassificationRule();
		currentRule.copyFrom(rule);

		do {
			ElementaryCondition condition = induceCondition(
					rule, dataset, uncovered, covered, allowedAttributes);
			
			if (condition != null) {

				if (params.getSelectBestCandidate()) {
					carryOn = tryAddCondition(currentRule, rule, condition, dataset, covered, conditionCovered);
				} else {
					carryOn = tryAddCondition(rule, null, condition, dataset, covered, conditionCovered);
				}

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

		/*
		if (precalculatedCoverings != null) {
			for (Attribute a: precalculatedFilter.keySet()) {
				precalculatedFilter.get(a).clear();
			}
		}
		*/



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
	public void prune(final Rule rule, final ExampleSet trainSet) {
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

		double P = rule.getWeighted_P();
		double N = rule.getWeighted_N();
		
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

		ContingencyTable ct = new ContingencyTable();
		rule.covers(trainSet, ct);
		double initialQuality = calculateQuality(trainSet, ct, params.getPruningMeasure());
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
					
					double q = ((ClassificationMeasure)params.getPruningMeasure()).calculate(p, n, P, N);
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

		ct = new ContingencyTable();
		IntegerBitSet positives = new IntegerBitSet(trainSet.size());
		IntegerBitSet negatives = new IntegerBitSet(trainSet.size());

		rule.covers(trainSet, ct, positives, negatives);

		rule.setWeighted_p(ct.weighted_p);
		rule.setWeighted_n(ct.weighted_n);
		rule.setCoveredPositives(positives);
		rule.setCoveredNegatives(negatives);

		Pair<Double,Double> qp = calculateQualityAndPValue(trainSet, ct, params.getVotingMeasure());
		rule.setWeight(qp.getFirst());
		rule.setPValue(qp.getSecond());
	}

	/**
	 * Induces an elementary condition.
	 * 
	 * @param rule Current rule.
	 * @param trainSet Training set.
	 * @param uncoveredPositives Set of positive examples uncovered by the model.
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

		double classId = ((SingletonSet)rule.getConsequence().getValueSet()).getValue();
		Attribute weightAttr = trainSet.getAttributes().getWeight();
		Set<Integer> positives = rule.getCoveredPositives();
		double P = rule.getWeighted_P();
		double N = rule.getWeighted_N();
		double apriori_prec = P / (P + N);

		List<Future<ConditionEvaluation>> futures = new ArrayList<Future<ConditionEvaluation>>();
		
		// iterate over all allowed decision attributes
		for (Attribute attr : allowedAttributes) {

			// consider attributes in parallel
			Future<ConditionEvaluation> future = (Future<ConditionEvaluation>) pool.submit(() -> {
				
				ConditionEvaluation best = new ConditionEvaluation();

				// check if attribute is numerical or nominal
				if (attr.isNumerical()) {
					// statistics from all points
					double left_p = 0;
					double left_n = 0;
					double right_p = 0;
					double right_n = 0;

					// statistics from points yet to cover
					int toCover_right_p = 0;
					int toCover_left_p = 0;

					class TotalPosNeg {
						double p = 0;
						double n = 0;
						int toCover_p = 0;
					}

					Map<Double, TotalPosNeg> totals = new TreeMap<Double, TotalPosNeg>();

					// get all distinctive values of attribute
					for (int id : coveredByRule) {
						DataRow dr = trainSet.getExample(id).getDataRow();
						double val = dr.get(attr);

						// exclude missing values from keypoints
						if (Double.isNaN(val)) {
							continue;
						}

						TotalPosNeg tot = totals.computeIfAbsent(val, (k) -> new TotalPosNeg());
						double w = (weightAttr != null) ? dr.get(weightAttr) : 1.0;

							// put to proper bin depending of class label
						if (positives.contains(id)) {
							right_p += w;
							tot.p += w;
							if (uncoveredPositives.contains(id)) {
								++toCover_right_p;
								++tot.toCover_p;
							}
						} else {
							right_n += w;
							tot.n += w;
						}
					}

					Double [] keys = totals.keySet().toArray(new Double[totals.size()]);
					//Logger.log(", " + keys.length, Level.INFO);

					// check all possible midpoints (number of distinctive attribute values - 1)
					// if only one attribute value - ignore it
					for (int keyId = 0; keyId < keys.length - 1; ++keyId) {
						double key = keys[keyId];

						double next = keys[keyId + 1];
						double midpoint = (key + next) / 2;

						TotalPosNeg tot = totals.get(key);
						left_p += tot.p;
						right_p -= tot.p;
						left_n += tot.n;
						right_n -= tot.n;
						toCover_left_p += tot.toCover_p;
						toCover_right_p -= tot.toCover_p;

						TotalPosNeg totNext = totals.get(next);
						if ((tot.n == 0 && totNext.n == 0) || (tot.p == 0 && totNext.p == 0)) {
							continue;
						}


						// calculate precisions
						double left_prec = left_p / (left_p + left_n);
						double right_prec = right_p / (right_p + right_n);

						// evaluate left-side condition: a in (-inf, v)
						if (left_prec > apriori_prec) {
							double quality = ((ClassificationMeasure) params.getInductionMeasure()).calculate(left_p, left_n, P, N);

							if ((quality > best.quality || (quality == best.quality && left_p > best.covered)) && (toCover_left_p > 0)) {
								ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(midpoint));
								if (checkCandidate(candidate, classId, P, toCover_left_p)) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + left_p + ", n=" + left_n + ", new_p=" + (double) toCover_left_p + ", quality=" + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = left_p;
									best.condition = candidate;
								}
							}
						}

						// evaluate right-side condition: a in <v, inf)
						if (right_prec > apriori_prec) {
							double quality = ((ClassificationMeasure) params.getInductionMeasure()).calculate(right_p, right_n, P, N);

							if ((quality > best.quality || (quality == best.quality && right_p > best.covered)) && (toCover_right_p > 0)) {
								ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint));
								if (checkCandidate(candidate, classId, P, toCover_right_p)) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + right_p + ", n=" + right_n + ", new_p=" + (double) toCover_right_p + ", quality=" + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = right_p;
									best.condition = candidate;
								}
							}
						}
					}
				} else {

					// weighted case - no precalculated converings
					if (precalculatedCoverings == null) {
						// sum of positive and negative weights for all values
						double[] p = new double[attr.getMapping().size()];
						double[] n = new double[attr.getMapping().size()];
					
						int[] toCover_p = new int[attr.getMapping().size()];

						// get all distinctive values of attribute
						for (int id : coveredByRule) {
							DataRow dr = trainSet.getExample(id).getDataRow();
							double value = dr.get(attr);

							// omit missing values
							if (Double.isNaN(value)) {
								continue;
							}

							int castedValue = (int) value;
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

						// try all possible conditions
						for (int i = 0; i < attr.getMapping().size(); ++i) {
							// evaluate equality condition a = v
							double quality = ((ClassificationMeasure) params.getInductionMeasure()).calculate(
									p[i], n[i], P, N);
							if ((quality > best.quality || (quality == best.quality && p[i] > best.covered)) && (toCover_p[i] > 0)) {
								ElementaryCondition candidate =
										new ElementaryCondition(attr.getName(), new SingletonSet((double) i, attr.getMapping().getValues()));
								if (checkCandidate(candidate, classId, P, toCover_p[i])) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + p[i] + ", n=" + n[i] + ", new_p=" + (double) toCover_p[i] + ", quality=" + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = p[i];
									best.condition = candidate;
								}
							}
						}

					} else {
						// unweighted case
						// try all possible conditions
						for (int i = 0; i < attr.getMapping().size(); ++i) {

							IntegerBitSet conditionCovered = precalculatedCoverings.get(attr).get((double) i);
							double p = conditionCovered.calculateIntersectionSize(rule.getCoveredPositives());
							int toCover_p = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule, (IntegerBitSet) uncoveredPositives);
							double n = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule) - p;

							// no need to analyze conditions that do not alter covering
							//if (p == rule.getWeighted_p() && n == rule.getWeighted_n()) {
								//continue;
							//}

							// evaluate equality condition a = v
							double quality = ((ClassificationMeasure) params.getInductionMeasure()).calculate(
									p, n, P, N);
							if ((quality > best.quality || (quality == best.quality && p > best.covered)) && (toCover_p > 0)) {
								ElementaryCondition candidate =
										new ElementaryCondition(attr.getName(), new SingletonSet((double) i, attr.getMapping().getValues()));
								if (checkCandidate(candidate, classId, P, toCover_p)) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + (double) toCover_p + ", quality=" + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = p;
									best.condition = candidate;
								}
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
				ConditionEvaluation eval = (ConditionEvaluation)f.get();
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

	/***
	 * Makes an attempt to add the condition to the rule.
	 * 
	 * @param currentRule Rule to be updated.
	 * @param bestRule Best rule found up to now. Use null value if not needed.
	 * @param condition Condition to be added.
	 * @param trainSet Training set.
	 * @param covered Set of examples covered by the rules.
	 * @param conditionCovered Bit vector of examples covered by the condition.
	 * @return Flag indicating whether condition has been added successfully.
	 */
	public boolean tryAddCondition(
		final Rule currentRule,
		final Rule bestRule,
		final ConditionBase condition, 
		final ExampleSet trainSet,
		final Set<Integer> covered,
		final IntegerBitSet conditionCovered) {
		
		boolean carryOn = true;
		boolean add = false;
		ContingencyTable ct = new ContingencyTable();
		
		if (condition != null) {
			conditionCovered.clear();
			condition.evaluate(trainSet, conditionCovered);

			// calculate  quality before addition
			ct.weighted_P = currentRule.getWeighted_P();
			ct.weighted_N = currentRule.getWeighted_N();
			ct.weighted_p = currentRule.getWeighted_p();
			ct.weighted_n = currentRule.getWeighted_n();

			double qualityBefore = calculateQuality(trainSet, ct, params.getInductionMeasure());

			if (trainSet.getAttributes().getWeight() != null) {
				// calculate weights
				
			} else {
				ct.weighted_p = currentRule.getCoveredPositives().calculateIntersectionSize(conditionCovered);
				ct.weighted_n = currentRule.getCoveredNegatives().calculateIntersectionSize(conditionCovered);
			}
			
			// analyse stopping criteria
			double adjustedMinCov = Math.min(
					params.getMinimumCovered(),
					Math.max(1.0, 0.2 * ct.weighted_P));

			if (ct.weighted_p < adjustedMinCov) {
				if (currentRule.getPremise().getSubconditions().size() == 0) {
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

				// recalculate quality
				double qualityAfter = calculateQuality(trainSet, ct, params.getInductionMeasure());

				if (bestRule != null) {
					if (qualityAfter > qualityBefore) {
						// quality increase
						double bestQuality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
								bestRule.getWeighted_p(), bestRule.getWeighted_n(), bestRule.getWeighted_P(), bestRule.getWeighted_N());

						if (bestRule.getPremise() != currentRule.getPremise() && qualityAfter > bestQuality) {
							// if current is better then previous best and has different premise
							bestRule.copyFrom(currentRule);
						}
					} else {
						// quality drop - local maximum found
						if (currentRule.getPremise() == bestRule.getPremise()) {
							// store current state in best rule
							bestRule.copyFrom(currentRule);

							// fork rules - make deep copy of selected components
							currentRule.setPremise(new CompoundCondition());
							currentRule.getPremise().getSubconditions().addAll(bestRule.getPremise().getSubconditions());
							currentRule.setCoveredPositives(bestRule.getCoveredPositives().clone());
							currentRule.setCoveredNegatives(bestRule.getCoveredNegatives().clone());
						}
					}
				}

				currentRule.getPremise().getSubconditions().add(condition);

				covered.retainAll(conditionCovered);
				currentRule.getCoveredPositives().retainAll(conditionCovered);
				currentRule.getCoveredNegatives().retainAll(conditionCovered);
				
				currentRule.setWeighted_p(ct.weighted_p);
				currentRule.setWeighted_n(ct.weighted_n);

				Pair<Double, Double> qp = calculateQualityAndPValue(trainSet, ct, params.getVotingMeasure());
				currentRule.setWeight(qp.getFirst());
				currentRule.setPValue(qp.getSecond());
				
				Logger.log("Condition " + currentRule.getPremise().getSubconditions().size() + " added: "
						+ currentRule.toString() + " " + currentRule.printStats() + "\n", Level.FINER);
			}
		}
		else {
			carryOn = false;
		}

		// best is current and has not been updated from the beginning
		if (carryOn == false && bestRule != null) {
			double bestQuality = ((ClassificationMeasure) params.getInductionMeasure()).calculate(
					bestRule.getWeighted_p(), bestRule.getWeighted_n(), bestRule.getWeighted_P(), bestRule.getWeighted_N());
			double currentQuality = ((ClassificationMeasure) params.getInductionMeasure()).calculate(
					currentRule.getWeighted_p(), currentRule.getWeighted_n(), currentRule.getWeighted_P(), currentRule.getWeighted_N());

			if (currentQuality > bestQuality) {
				bestRule.copyFrom(currentRule);
			}
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
	protected boolean checkCandidate(ElementaryCondition cnd, double classId, double totalPositives, double newlyCoveredPositives) {
		double adjustedMinCov = Math.min(
				params.getMinimumCovered(),
				Math.max(1.0, 0.2 * totalPositives));

		if (newlyCoveredPositives >= adjustedMinCov) {
			return true;
		} else {
			return false;
		}
	}
}
