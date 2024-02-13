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

import adaa.analytics.rules.logic.representation.*;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.SortedExampleSet;

import java.util.Set;
import java.util.logging.Level;

/**
 * Class for growing and pruning log rank-based survival rules.
 * 
 * @author Adam Gudys
 *
 */
public class SurvivalLogRankFinder extends RegressionFinder{

	public static class Implementation {
		public IExampleSet preprocess(IExampleSet trainSet) {
			IAttribute survTime = trainSet.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE);
			SortedExampleSetEx ses = new SortedExampleSetEx(trainSet, survTime, SortedExampleSet.INCREASING);
			return ses;
		}

		public void postprocess(
				final Rule rule,
				final IExampleSet dataset) {

			KaplanMeierEstimator kme = new KaplanMeierEstimator(dataset, rule.getCoveredPositives());
			((SurvivalRule)rule).setEstimator(kme);
		}

		protected boolean checkCandidate(
				IExampleSet dataset,
				Rule rule,
				ConditionBase candidate,
				Set<Integer> uncovered,
				Set<Integer> covered,
				ConditionEvaluation currentBest,
				RegressionFinder finder) {

			try {

				IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());
				candidate.evaluate(dataset, conditionCovered);

				IntegerBitSet ruleCovered = conditionCovered.clone();
				ruleCovered.retainAll(covered);

				double p = 0;
				double new_p = 0;

				if (dataset.getAttributes().getWeight() == null) {
					// unweighted examples
					p = conditionCovered.calculateIntersectionSize((IntegerBitSet) covered);
					new_p = conditionCovered.calculateIntersectionSize((IntegerBitSet) uncovered, (IntegerBitSet) covered);

				} else {
					// calculate weights of newly covered examples
					for (int id : conditionCovered) {
						if (covered.contains(id)) {
							double w = dataset.getExample(id).getWeight();
							p += w;
							if (uncovered.contains(id)) {
								new_p += w;
							}
						}
					}
				}

				if (finder.checkCoverage(p, 0, new_p, 0, dataset.size(), 0, uncovered.size(), rule.getRuleOrderNum())) {
					Covering cov = new Covering();
					cov.positives = ruleCovered;
					cov.weighted_p = p;

					double quality = finder.params.getInductionMeasure().calculate(dataset, cov);

					if (candidate instanceof ElementaryCondition) {
						ElementaryCondition ec = (ElementaryCondition) candidate;
						quality = finder.modifier.modifyQuality(quality, ec.getAttribute(), cov.weighted_p, new_p);
					}

					if (quality > currentBest.quality ||
							(quality == currentBest.quality && (new_p > currentBest.covered || currentBest.opposite))) {

						Logger.log("\t\tCurrent best: " + candidate + " (p=" + cov.weighted_p +
								", new_p=" + (double) new_p +
								", P=" + cov.weighted_P +
								", mean_y=" + cov.mean_y + ", mean_y2=" + cov.mean_y2 + ", stddev_y=" + cov.stddev_y +
								", quality=" + quality + "\n", Level.FINEST);

						candidate.setCovering(conditionCovered);

						currentBest.quality = quality;
						currentBest.condition = candidate;
						currentBest.covered = new_p;
						currentBest.covering = cov;
						currentBest.opposite = (candidate instanceof ElementaryCondition) &&
								(((ElementaryCondition) candidate).getValueSet() instanceof SingletonSetComplement);

						//rule.setWeight(quality);
						return true;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	private Implementation implementation = new Implementation();

	public SurvivalLogRankFinder(InductionParameters params) {
		super(params);
		this.params.setMeanBasedRegression(false);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IExampleSet preprocess(IExampleSet trainSet) {
		return implementation.preprocess(trainSet);
	}

	/**
	 * Postprocesses a rule.
	 *
	 * @param rule Rule to be postprocessed.
	 * @param dataset Training set.
	 *
	 */
	@Override
	public void postprocess(
			final Rule rule,
			final IExampleSet dataset) {

		super.postprocess(rule, dataset);
		implementation.postprocess(rule, dataset);
	}

	@Override
	protected boolean checkCandidate(
			IExampleSet dataset,
			Rule rule,
			ConditionBase candidate,
			Set<Integer> uncovered,
			Set<Integer> covered,
			ConditionEvaluation currentBest) {

		return implementation.checkCandidate(dataset, rule, candidate, uncovered, covered, currentBest, this);
	}
}
