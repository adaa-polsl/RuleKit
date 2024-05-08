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

import adaa.analytics.rules.data.DataColumnDoubleAdapter;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.logic.representation.*;

import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Algorithm for growing and pruning regression rules.
 * @author Adam
 *
 */
public class RegressionFinder extends AbstractFinder {
	
	public RegressionFinder(final InductionParameters params) {
		super(params);
		RegressionRule.setUseMean(params.isMeanBasedRegression());
	}

	@Override
	public IExampleSet preprocess(IExampleSet trainSet) {
		IAttribute label = trainSet.getAttributes().getLabel();
		SortedExampleSetEx ses = new SortedExampleSetEx(trainSet, label, EColumnSortDirections.INCREASING);
		return ses;
	}

	protected ElementaryCondition induceCondition_mean(
			final Rule rule,
			final IExampleSet dataset,
			final Set<Integer> uncovered,
			final Set<Integer> covered,
			final Set<IAttribute> allowedAttributes,
			Object... extraParams) {

		SortedExampleSetEx set = (dataset instanceof SortedExampleSetEx) ? (SortedExampleSetEx)dataset : null;
		if (set == null) {
			throw new InvalidParameterException("RegressionRules support only ListedExampleSet example sets");
		}

		boolean weighted = dataset.getAttributes().getWeight() != null;
		List<Future<ConditionEvaluation>> futures = new ArrayList<Future<ConditionEvaluation>>();

		// iterate over all possible decision attributes
		for (IAttribute attr : allowedAttributes) {
			DataColumnDoubleAdapter attDataColumnDoubleAdapter = set.getDataColumnDoubleAdapter(attr, Double.NaN);

			// consider attributes in parallel
			Future<ConditionEvaluation> future = (Future<ConditionEvaluation>) pool.submit(() -> {

				ConditionEvaluation best = new ConditionEvaluation();
				best.covering = new Covering();
				//Logger.log("\tattribute: " + attr.getName() + "\n", Level.FINEST);

				// check if attribute is numerical or nominal
				if (attr.isNumerical()) {

					IntegerBitSet mask = set.nonMissingVals.get(attr);
					IntegerBitSet localCov = new IntegerBitSet(set.size());
					IntegerBitSet localCovNew = new IntegerBitSet(set.size());

					localCov.setAll(covered);
					localCov.retainAll(mask);
					localCovNew.setAll(localCov);
					localCovNew.retainAll(uncovered);

					IntegerBitSet [] covs = new IntegerBitSet[2];
					covs[0] = new IntegerBitSet(dataset.size());
					covs[1] = localCov;

					// initial values for left hand side and right hand side conditions
					class Stats{
						double sum_w = 0;
						double sum_new_w = 0;
						double sum_y = 0;
						double sum_y2 = 0;
						double mean_y = 0 ;
						double stddev_y = 0;
					}

					Stats[] stats = new Stats[2];
					stats[0] = new Stats();
					stats[1] = new Stats();

					// get indices array
					Integer [] ids = new Integer[localCov.size()];
					int i = 0;
					for (int id : localCov) {
						ids[i++] = id;
						double y = set.labelsWeighted[id];
						stats[1].sum_y += y;
						stats[1].sum_y2 += y * y;
						stats[1].sum_w += set.weights[id];
					}

					// fill newly covered weights
					if (weighted) {
						if (localCov.size() != localCovNew.size()) {
							for (int id : localCovNew) {
								stats[1].sum_new_w += set.labelsWeighted[id];
							}
						} else {
							stats[1].sum_new_w = stats[1].sum_w;
						}
					} else {
						stats[1].sum_new_w = localCovNew.size();
					}

					// sort ids array according to the attribute value
					Arrays.sort(ids, Comparator.comparingDouble(a -> attDataColumnDoubleAdapter.getDoubleValue(a)));

					// iterate over examples in increasing attribute value order
					double prev_val = Double.MAX_VALUE;
					for (i = 0; i < ids.length; ++i) {
						int id = ids[i];
//						double val = dataset.getExample(id).getValue(attr);
						double val = attDataColumnDoubleAdapter.getDoubleValue(id);
						if (Double.isNaN(val)) {
							continue;
						}

						// we moved to another value - verify midpoint
						if (val > prev_val) {
							double midpoint = (val + prev_val) / 2;

							// evaluate both conditions
							for (int c = 0; c < 2; ++c) {
								stats[c].mean_y = stats[c].sum_y / stats[c].sum_w;
								double mean_y2 = stats[c].sum_y2 / stats[c].sum_w;
								stats[c].stddev_y = Math.sqrt(mean_y2 - stats[c].mean_y * stats[c].mean_y); // VX = E(X^2) - (EX)^2

								// binary search to get elements inside epsilon
								// assumption: double value preceeding/following one being search appears at most once
								int lo = Arrays.binarySearch(set.labels, Math.nextDown(stats[c].mean_y - stats[c].stddev_y));
								if (lo < 0) {
									lo = -(lo + 1); // if element not found, extract id of the next larger: ret = (-(insertion point) - 1)
								} else { lo += 1;} // if element found move to next one (first inside a range)

								int hi = Arrays.binarySearch(set.labels, Math.nextUp(stats[c].mean_y + stats[c].stddev_y));
								if (hi < 0) { hi = -(hi + 1); // if element not found, extract id of the next larger: ret = (-(insertion point) - 1)
								} // if element found - do nothing (first after the range)

								double P = set.totalWeightsBefore[hi] - set.totalWeightsBefore[lo];
								double N = set.totalWeightsBefore[set.size()] - P;
								double n = stats[c].sum_w;
								double p = 0;
								double new_n = stats[c].sum_new_w;
								double new_p = 0;

								// iterate over elements from the positive range
								for (int j = lo; j < hi; ++j) {
									if (covs[c].contains(j)) {
										double wj = set.weights[j];
										n -= wj;
										p += wj;
										if (uncovered.contains(j)) {
											new_n -= wj;
											new_p += wj;
										}
									}
								}

								double quality = params.getInductionMeasure().calculate(p, n, P, N);
								quality = modifier.modifyQuality(quality, attr.getName(), p, new_p);

								if (quality > best.quality || (quality == best.quality && (new_p + new_n) > best.covered)) {

									ElementaryCondition candidate = new ElementaryCondition(attr.getName(),
											(c == 0) ? Interval.create_le(midpoint) : Interval.create_geq(midpoint));

									if (checkCandidate(candidate, p, n, new_p, new_n, P, N, uncovered.size(),rule.getRuleOrderNum())) {
										/*
										Logger.log("\t\tCurrent best: " + candidate + " (p=" + p + ", n=" + n +
												", new_p=" + (double) new_p + ", new_n="+  new_n +
												", P=" + P + ", N=" + N +
												", mean_y=" + stats[c].mean_y + ", mean_y2=" + mean_y2 + ", stddev_y=" + stats[c].stddev_y +
												", quality=" + quality + "\n", Level.FINEST);
										*/
										best.quality = quality;
										best.covered = new_p + new_n;
										best.condition = candidate;
										best.opposite = false;
										best.covering.weighted_p = p;
										best.covering.weighted_n = n;
										best.covering.weighted_P = P;
										best.covering.weighted_N = N;
										best.covering.mean_y = stats[c].mean_y;
										best.covering.mean_y2 = mean_y2;
										best.covering.stddev_y = stats[c].stddev_y;
									}
								}

							}

						}

						// update stats
						double y = set.labelsWeighted[id];
						double w = set.weights[id];
						stats[0].sum_y += y;
						stats[0].sum_y2 += y * y;
						stats[0].sum_w += w;
						covs[0].add(id);

						stats[1].sum_y -= y;
						stats[1].sum_y2 -= y*y;
						stats[1].sum_w -= w;
						covs[1].remove(id);

						if (uncovered.contains(id)) {
							stats[0].sum_new_w += w;
							stats[1].sum_new_w -= w;
						}

						prev_val = val;
					}

				} else {
					// try all possible conditions
					for (int i = 0; i < attr.getMapping().size(); ++i) {
						// evaluate straight condition
						ElementaryCondition candidate = new ElementaryCondition(
								attr.getName(), new SingletonSet((double)i, attr.getMapping().getValues()));
						checkCandidate(dataset, rule, candidate, uncovered, covered, best);

						// evaluate complementary condition if enabled
						if (params.isConditionComplementEnabled()) {
							candidate = new ElementaryCondition(
									attr.getName(), new SingletonSetComplement((double) i, attr.getMapping().getValues()));
							checkCandidate(dataset, rule, candidate, uncovered, covered, best);
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

				if (eval.condition != null) {
					Logger.log("\tAttribute best: " + eval.condition + " (p=" + eval.covering.weighted_p + ", n=" + eval.covering.weighted_n +
							", P=" + eval.covering.weighted_P + ", N=" + eval.covering.weighted_N +
							", mean_y=" + eval.covering.mean_y + ", mean_y2=" + eval.covering.mean_y2 + ", stddev_y=" + eval.covering.stddev_y +
							", quality=" + eval.quality + "\n", Level.FINEST);
				}

				if (best == null || eval.quality > best.quality || (eval.quality == best.quality && eval.covered > best.covered)) {
					best = eval;
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (best.condition != null) {
			IAttribute bestAttr = dataset.getAttributes().get(((ElementaryCondition)best.condition).getAttribute());
			if (bestAttr.isNominal()) {
				allowedAttributes.remove(bestAttr);
			}
		}

		return (ElementaryCondition)best.condition;
	}


	@Override
	protected ElementaryCondition induceCondition(
		final Rule rule,
		final IExampleSet dataset,
		final Set<Integer> uncovered, 
		final Set<Integer> covered, 
		final Set<IAttribute> allowedAttributes,
		Object... extraParams) {
		
		if (allowedAttributes.size() == 0) {
			return null;
		}
		if (params.isMeanBasedRegression()) {
			return induceCondition_mean(rule, dataset, uncovered, covered, allowedAttributes, extraParams);
		}

		List<Future<ConditionEvaluation>> futures = new ArrayList<Future<ConditionEvaluation>>();
				
		// iterate over all possible decision attributes
		for (IAttribute attr : allowedAttributes) {
			DataColumnDoubleAdapter attDataColumnDoubleAdapter = dataset.getDataColumnDoubleAdapter(attr, Double.NaN);

			// consider attributes in parallel
			Future<ConditionEvaluation> future = (Future<ConditionEvaluation>) pool.submit(() -> {
			
				ConditionEvaluation best = new ConditionEvaluation();

				// check if attribute is numerical or nominal
				if (attr.isNumerical()) {
					Map<Double, List<Integer>> values2ids = new TreeMap<Double, List<Integer>>();
					
					// get all distinctive values of attribute
					for (int id : covered) {
//						Example ex = dataset.getExample(id);
//						double val = ex.getValue(attr);
						double val = attDataColumnDoubleAdapter.getDoubleValue(id);

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
						checkCandidate(dataset, rule, candidate, uncovered, covered, best);
							
						// evaluate right-side condition v <= a
						candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint)); 
						checkCandidate(dataset, rule, candidate, uncovered, covered, best);
					}
				} else {
					// try all possible conditions
					for (int i = 0; i < attr.getMapping().size(); ++i) {
						// evaluate straight condition
						ElementaryCondition candidate = new ElementaryCondition(
								attr.getName(), new SingletonSet((double)i, attr.getMapping().getValues()));
						checkCandidate(dataset, rule, candidate, uncovered, covered, best);

						// evaluate complementary condition if enabled
						if (params.isConditionComplementEnabled()) {
							candidate = new ElementaryCondition(
									attr.getName(), new SingletonSetComplement((double) i, attr.getMapping().getValues()));
							checkCandidate(dataset, rule, candidate, uncovered, covered, best);
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

				if (eval.condition != null) {
					Logger.log("\tAttribute best: " + eval.condition + " (p=" + eval.covering.weighted_p + ", n=" + eval.covering.weighted_n +
							", P=" + eval.covering.weighted_P + ", N=" + eval.covering.weighted_N +
							", mean_y=" + eval.covering.mean_y + ", mean_y2=" + eval.covering.mean_y2 + ", stddev_y=" + eval.covering.stddev_y +
							", quality=" + eval.quality + "\n", Level.FINEST);
				}
				if (best == null || eval.quality > best.quality || (eval.quality == best.quality && eval.covered > best.covered)) {
					best = eval;
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (best.condition != null) {
			IAttribute bestAttr = dataset.getAttributes().get(((ElementaryCondition)best.condition).getAttribute());
			if (bestAttr.isNominal()) {
				allowedAttributes.remove(bestAttr);
			}
		}

		return (ElementaryCondition)best.condition;
	}

	
	protected boolean checkCandidate(
			IExampleSet dataset,
			Rule rule,
			ConditionBase candidate,
			Set<Integer> uncovered,
			Set<Integer> covered,
			ConditionEvaluation currentBest) {

		try {
		CompoundCondition newPremise = new CompoundCondition();
		newPremise.getSubconditions().addAll(rule.getPremise().getSubconditions());
		newPremise.addSubcondition(candidate);
		
		Rule newRule = (Rule) rule.clone();
		newRule.setPremise(newPremise);

		 
		Covering cov = new Covering();
		newRule.covers(dataset, cov, cov.positives, cov.negatives);

		double new_p = 0, new_n = 0;

		if (dataset.getAttributes().getWeight() == null) {
			// unweighted examples
			new_p = SetHelper.intersectionSize(uncovered, cov.positives);
			new_n =	SetHelper.intersectionSize(uncovered, cov.negatives);
		} else {
			DataColumnDoubleAdapter weightDataColumnDoubleAdapter = dataset.getDataColumnDoubleAdapter(dataset.getAttributes().getWeight(), Double.NaN);

			// calculate weights of newly covered examples
			for (int id : cov.positives) {
				new_p += uncovered.contains(id) ? weightDataColumnDoubleAdapter.getDoubleValue(id) : 0;
			}
			for (int id : cov.negatives) {
				new_n += uncovered.contains(id) ? weightDataColumnDoubleAdapter.getDoubleValue(id) : 0;
			}
		}
		
		if (checkCoverage(cov.weighted_p, cov.weighted_n, new_p, new_n, cov.weighted_P, cov.weighted_N, uncovered.size(),rule.getRuleOrderNum())) {
			double quality = params.getInductionMeasure().calculate(dataset, cov);

			if (candidate instanceof  ElementaryCondition) {
				ElementaryCondition ec = (ElementaryCondition)candidate;
				quality = modifier.modifyQuality(quality, ec.getAttribute(), cov.weighted_p + cov.weighted_n, new_p + new_n);
			}

			if (quality > currentBest.quality ||
					(quality == currentBest.quality && (new_p + new_n > currentBest.covered || currentBest.opposite))) {

				/*
				Logger.log("\t\tCurrent best: " + candidate + " (p=" + cov.weighted_p + ", n=" + cov.weighted_n +
						", new_p=" + (double) new_p + ", new_n="+  new_n +
						", P=" + cov.weighted_P + ", N=" + cov.weighted_N +
						", mean_y=" + cov.mean_y + ", mean_y2=" + cov.mean_y2 + ", stddev_y=" + cov.stddev_y +
						", quality=" + quality + "\n", Level.FINEST);
*/
				currentBest.quality = quality;
				currentBest.condition = candidate;
				currentBest.covered = new_p + new_n;
				currentBest.covering = cov;
				currentBest.opposite = (candidate instanceof ElementaryCondition) &&
						(((ElementaryCondition)candidate).getValueSet() instanceof SingletonSetComplement);
				//rule.setWeight(quality);
				return true;
			} 
		}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected boolean checkCandidate(ElementaryCondition cnd, double p, double n, double new_p, double new_n, double P, double N,double uncoveredSize, int ruleOrderNum) {
		return checkCoverage(p, n, new_p, new_n, P, N, uncoveredSize,  ruleOrderNum);
	}


	boolean checkCoverage(double p, double n, double new_p, double new_n, double P, double N,double uncoveredSize, int ruleOrderNum) {
		double adjustedMinCov =
				countAbsoluteMinimumCovered(P+N, ruleOrderNum, uncoveredSize);
		return ((new_p + new_n) >= adjustedMinCov) &&
				((p + n) >= params.getAbsoluteMinimumCoveredAll(P + N));
	}

}
