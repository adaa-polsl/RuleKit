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

	protected Map<Attribute, Map<Double, IntegerBitSet>> precalculatedCoveringsComplement;

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
		precalculatedCoveringsComplement = new HashMap<Attribute, Map<Double, IntegerBitSet>>();
		precalculatedFilter = new HashMap<Attribute, Set<Double>>();
		Attributes attributes = trainSet.getAttributes();

		List<Future> futures = new ArrayList<Future>();

		// iterate over all allowed decision attributes
		for (Attribute attr : attributes) {

			Future f = pool.submit( () -> {
				Map<Double, IntegerBitSet> attributeCovering = new TreeMap<Double, IntegerBitSet>();
				Map<Double, IntegerBitSet> attributeCoveringComplement = new TreeMap<Double, IntegerBitSet>();

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

					for (int val = 0; val != attr.getMapping().size(); ++val) {
						attributeCoveringComplement.put((double) val, attributeCovering.get((double) val).clone());
						attributeCoveringComplement.get((double) val).negate();
					}
				}

				synchronized (this) {
					precalculatedCoverings.put(attr, attributeCovering);
					precalculatedCoveringsComplement.put(attr, attributeCoveringComplement);
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

		for (IFinderObserver o: observers) {
			o.growingStarted(rule);
		}

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

		Rule currentRule, bestRule;

		if (params.getSelectBestCandidate()) {
			currentRule = new ClassificationRule();
			currentRule.copyFrom(rule);
			bestRule = rule;
		} else {
			currentRule = rule;
			bestRule = null;
		}

		do {
			ElementaryCondition condition = induceCondition(
					currentRule, dataset, uncovered, covered, allowedAttributes);
			
			if (condition != null) {

				carryOn = tryAddCondition(currentRule, bestRule, condition, dataset, covered, conditionCovered);

				if (params.getMaxGrowingConditions() > 0) {
					if (currentRule.getPremise().getSubconditions().size() - initialConditionsCount >=
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

		for (IFinderObserver o: observers) {
			o.growingFinished(rule);
		}

		return addedConditionsCount;
	}
	
	
	/**
	 * Removes irrelevant conditions from the rule using hill-climbing strategy. 
	 * @param rule Rule to be pruned.
	 * @param trainSet Training set. 
	 * @return Updated covering object.
	 */
	public void prune(final Rule rule, final ExampleSet trainSet, final Set<Integer> uncovered) {
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
		long[] uncoveredMask = new long[maskLength];

		double P = rule.getWeighted_P();
		double N = rule.getWeighted_N();
		
		int[] conditionsPerExample = new int[trainSet.size()];
		int counter_p = 0;
		int counter_new_p = 0;

		for (int i = 0; i < trainSet.size(); ++i) {
			Example e = trainSet.getExample(i);
			int wordId = i / Long.SIZE;
			int wordOffset = i % Long.SIZE;

			// is positive
			if (rule.getConsequence().evaluate(e)) {
				labelMask[wordId] |= 1L << wordOffset;
				++counter_p;

				// is uncovered
				if (uncovered.contains(i)) {
					uncoveredMask[wordId] |= 1L << wordOffset;
					++counter_new_p;
				}
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
		double initialQuality = params.getPruningMeasure().calculate(trainSet, ct);
		initialQuality = modifier.modifyQuality(initialQuality, null, counter_p, counter_new_p);

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
					
					ConditionBase cnd = (ConditionBase) rule.getPremise().getSubconditions().get(fcid);
					// ignore already removed conditions
					if (removedConditions.contains(fcid)) {
						return Double.NEGATIVE_INFINITY;
					}
					
					// consider only prunable conditions
					if (!cnd.isPrunable()) {
						return Double.NEGATIVE_INFINITY;
					}

					// only elementary conditions are prunable
					String attr = ((ElementaryCondition)cnd).getAttribute();

					double p = 0;
					double n = 0;
					double new_p = 0;

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
						long uncovWord = uncoveredMask[wordId];

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

							new_p += Long.bitCount(posWord & uncovWord);
						}
					}
					
					double q = params.getPruningMeasure().calculate(p, n, P, N);
					q = modifier.modifyQuality(q, attr, p, new_p);
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
			if (bestQuality >= initialQuality && bestQuality != Double.NEGATIVE_INFINITY) {
				initialQuality = bestQuality;
				removedConditions.add(toRemove);

				for (IFinderObserver o: observers) {
					o.conditionRemoved(rule.getPremise().getSubconditions().get(toRemove));
				}

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

		rule.updateWeightAndPValue(trainSet, ct, params.getVotingMeasure());
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

		double apriori_prec = params.isControlAprioriPrecision()
				? P / (P + N)
				: Double.MIN_VALUE;

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
						if (left_prec > apriori_prec && toCover_left_p > 0) {
							double quality = params.getInductionMeasure().calculate(left_p, left_n, P, N);
							quality = modifier.modifyQuality(quality, attr.getName(), left_p, toCover_left_p);

							if (quality > best.quality || (quality == best.quality && left_p > best.covered)) {
								ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(midpoint));
								if (checkCandidate(candidate, classId, left_p, left_n, toCover_left_p, P)) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + left_p + ", n=" + left_n + ", new_p=" + (double) toCover_left_p + ", quality=" + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = left_p;
									best.condition = candidate;
									best.opposite = false;
								}
							}
						}

						// evaluate right-side condition: a in <v, inf)
						if (right_prec > apriori_prec && toCover_right_p > 0) {
							double quality = params.getInductionMeasure().calculate(right_p, right_n, P, N);
							quality = modifier.modifyQuality(quality, attr.getName(), right_p, toCover_right_p);

							if (quality > best.quality || (quality == best.quality && right_p > best.covered)) {
								ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint));
								if (checkCandidate(candidate, classId, right_p, right_n, toCover_right_p, P)) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + right_p + ", n=" + right_n + ", new_p=" + (double) toCover_right_p + ", quality=" + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = right_p;
									best.condition = candidate;
									best.opposite = false;
								}
							}
						}
					}
				} else { // nominal attribute

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
							double quality = params.getInductionMeasure().calculate(p[i], n[i], P, N);
							quality = modifier.modifyQuality(quality, attr.getName(), p[i], toCover_p[i]);

							if ((quality > best.quality || (quality == best.quality && p[i] > best.covered)) && (toCover_p[i] > 0)) {
								ElementaryCondition candidate =
										new ElementaryCondition(attr.getName(), new SingletonSet((double) i, attr.getMapping().getValues()));
								if (checkCandidate(candidate, classId, p[i], n[i], toCover_p[i], P)) {
									Logger.log("\tCurrent best: " + candidate + " (p=" + p[i] + ", n=" + n[i] + ", new_p=" + (double) toCover_p[i] + ", quality=" + quality + "\n", Level.FINEST);
									best.quality = quality;
									best.covered = p[i];
									best.condition = candidate;
									best.opposite = false;
								}
							}
						}

					} else {
						// unweighted case
						// try all possible conditions
						for (int i = 0; i < attr.getMapping().size(); ++i) {

							// evaluate straight condition
							IntegerBitSet conditionCovered = precalculatedCoverings.get(attr).get((double) i);
							double p = conditionCovered.calculateIntersectionSize(rule.getCoveredPositives());
							int toCover_p = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule, (IntegerBitSet) uncoveredPositives);
							double n = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule) - p;

							// no need to analyze conditions that do not alter covering
							//if (p == rule.getWeighted_p() && n == rule.getWeighted_n()) {
								//continue;
							//}

							double prec = p / (p + n);
							if (prec > apriori_prec && toCover_p > 0) {
								// evaluate equality condition a = v
								double quality = params.getInductionMeasure().calculate(p, n, P, N);
								quality = modifier.modifyQuality(quality, attr.getName(), p, toCover_p);
								// prefer (gender = female) over (gender = !male) for boolean attributes
								if (quality > best.quality ||
										(quality == best.quality && (p > best.covered || best.opposite))) {
									ElementaryCondition candidate =
											new ElementaryCondition(attr.getName(), new SingletonSet((double) i, attr.getMapping().getValues()));
									if (checkCandidate(candidate, classId, p, n, toCover_p, P)) {
										Logger.log("\tCurrent best: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + (double) toCover_p + ", quality=" + quality + "\n", Level.FINEST);
										best.quality = quality;
										best.covered = p;
										best.condition = candidate;
										best.opposite = false;
									}
								}
							}

							// evaluate complementary condition if enabled
							if (!params.isConditionComplementEnabled()) {
								continue;
							}

							conditionCovered = precalculatedCoveringsComplement.get(attr).get((double) i);
							p = conditionCovered.calculateIntersectionSize(rule.getCoveredPositives());
							toCover_p = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule, (IntegerBitSet) uncoveredPositives);
							n = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule) - p;

							prec = p / (p + n);
							if (prec > apriori_prec && toCover_p > 0) {
								// evaluate equality condition a = v
								double quality = params.getInductionMeasure().calculate(p, n, P, N);
								quality = modifier.modifyQuality(quality, attr.getName(), p, toCover_p);

								if (quality > best.quality || (quality == best.quality && p > best.covered)) {
									ElementaryCondition candidate =
											new ElementaryCondition(attr.getName(), new SingletonSetComplement((double) i, attr.getMapping().getValues()));
									if (checkCandidate(candidate, classId, p, n, toCover_p, P)) {
										Logger.log("\tCurrent best: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + (double) toCover_p + ", quality=" + quality + "\n", Level.FINEST);
										best.quality = quality;
										best.covered = p;
										best.condition = candidate;
										best.opposite = true;
									}
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

			double qualityBefore = params.getInductionMeasure().calculate(trainSet, ct);

			if (trainSet.getAttributes().getWeight() != null) {
				// calculate weights
				
			} else {
				ct.weighted_p = currentRule.getCoveredPositives().calculateIntersectionSize(conditionCovered);
				ct.weighted_n = currentRule.getCoveredNegatives().calculateIntersectionSize(conditionCovered);
			}
			
			// analyse stopping criteria
			double adjustedMinCov = Math.min(
					params.getAbsoluteMinimumCovered(ct.weighted_P),
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
				double qualityAfter = params.getInductionMeasure().calculate(trainSet, ct);

				if (bestRule != null && qualityAfter < qualityBefore) {
					// if quality drop - local maximum found
					if (currentRule.getPremise() == bestRule.getPremise()) {
						// if current is best rule - fork rules (deep copy of selected components)
						bestRule.copyFrom(currentRule);

						currentRule.setPremise(new CompoundCondition());
						currentRule.getPremise().getSubconditions().addAll(bestRule.getPremise().getSubconditions());
						currentRule.setCoveredPositives(bestRule.getCoveredPositives().clone());
						currentRule.setCoveredNegatives(bestRule.getCoveredNegatives().clone());
					}
				}

				currentRule.getPremise().getSubconditions().add(condition);

				for (IFinderObserver o: observers) {
					o.conditionAdded(condition);
				}

				covered.retainAll(conditionCovered);
				currentRule.getCoveredPositives().retainAll(conditionCovered);
				currentRule.getCoveredNegatives().retainAll(conditionCovered);
				currentRule.setWeighted_p(ct.weighted_p);
				currentRule.setWeighted_n(ct.weighted_n);
				currentRule.updateWeightAndPValue(trainSet, ct, params.getVotingMeasure());

				if (bestRule != null && qualityAfter > qualityBefore) {
					// quality increase
					double bestQuality = params.getInductionMeasure().calculate(
							bestRule.getWeighted_p(), bestRule.getWeighted_n(), bestRule.getWeighted_P(), bestRule.getWeighted_N());

					// if current is better than best
					if (qualityAfter > bestQuality) {
						bestRule.copyFrom(currentRule);
					}
				}
				
				Logger.log("Condition " + currentRule.getPremise().getSubconditions().size() + " added: "
						+ currentRule.toString() + " " + currentRule.printStats() + "\n", Level.FINER);
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
	protected boolean checkCandidate(ElementaryCondition cnd, double classId, double p, double n, double new_p, double P) {
		double adjustedMinCov = Math.min(
				params.getAbsoluteMinimumCovered(P),
				Math.max(1.0, 0.2 * P));

		if (new_p >= adjustedMinCov && p >= params.getAbsoluteMinimumCoveredAll(P)) {
			return true;
		} else {
			return false;
		}
	}
}
