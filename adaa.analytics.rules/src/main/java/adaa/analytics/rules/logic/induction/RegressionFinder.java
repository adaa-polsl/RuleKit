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

import adaa.analytics.rules.logic.quality.ChiSquareVarianceTest;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.representation.*;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.tools.container.Pair;

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
	}
	
	@Override
	protected ElementaryCondition induceCondition(
		final Rule rule,
		final ExampleSet dataset,
		final Set<Integer> uncovered, 
		final Set<Integer> covered, 
		final Set<Attribute> allowedAttributes,
		Object... extraParams) {
		
		if (allowedAttributes.size() == 0) {
			return null;
		}
		
		List<Future<ConditionEvaluation>> futures = new ArrayList<Future<ConditionEvaluation>>();
				
		// iterate over all possible decision attributes
		for (Attribute attr : allowedAttributes) {
			
			// consider attributes in parallel
			Future<ConditionEvaluation> future = (Future<ConditionEvaluation>) pool.submit(() -> {
			
				ConditionEvaluation best = new ConditionEvaluation();
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
						checkCandidate(dataset, rule, candidate, uncovered, best);
							
						// evaluate right-side condition v <= a
						candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint)); 
						checkCandidate(dataset, rule, candidate, uncovered, best);
					}
				} else {
					// try all possible conditions
					for (int i = 0; i < attr.getMapping().size(); ++i) {
						ElementaryCondition candidate = new ElementaryCondition(
								attr.getName(), new SingletonSet((double)i, attr.getMapping().getValues())); 
						
						checkCandidate(dataset, rule, candidate, uncovered, best);
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
			Attribute bestAttr = dataset.getAttributes().get(((ElementaryCondition)best.condition).getAttribute());
			if (bestAttr.isNominal()) {
				allowedAttributes.remove(bestAttr);
			}
		}

		return (ElementaryCondition)best.condition;
	}


	protected boolean checkCandidate(
			ExampleSet dataset,
			Rule rule,
			ConditionBase candidate,
			Set<Integer> uncovered,
			ConditionEvaluation currentBest) {

		try {
			Logger.log("Evaluating candidate: " + candidate, Level.FINEST);

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
				// calculate weights of newly covered examples
				for (int id : cov.positives) {
					new_p += uncovered.contains(id) ? dataset.getExample(id).getWeight() : 0;
				}
				for (int id : cov.negatives) {
					new_n += uncovered.contains(id) ? dataset.getExample(id).getWeight() : 0;
				}
			}

			if (checkCoverage(cov.weighted_p, cov.weighted_n, new_p, new_n, rule.getWeighted_P(), rule.getWeighted_N())) {
				double quality = params.getInductionMeasure().calculate(dataset, cov);
				
				Logger.log(", q=" + quality, Level.FINEST);

				if (quality > currentBest.quality || (quality == currentBest.quality && new_p + new_n > currentBest.covered)) {
					currentBest.quality = quality;
					currentBest.condition = candidate;
					currentBest.covered = new_p + new_n;
					currentBest.covering = cov;
					Logger.log(", approved!\n", Level.FINEST);
					//rule.setWeight(quality);
					return true;
				}
			}

			Logger.log("\n", Level.FINEST);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	boolean checkCoverage(double p, double n, double new_p, double new_n, double P, double N) {
		return ((new_p + new_n) >= params.getAbsoluteMinimumCovered(P + N)) &&
				((p + n) >= params.getAbsoluteMinimumCoveredAll(P + N));
	}
}
