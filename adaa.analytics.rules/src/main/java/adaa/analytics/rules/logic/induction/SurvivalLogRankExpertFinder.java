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

import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.logic.representation.condition.ConditionBase;
import adaa.analytics.rules.logic.representation.rule.Rule;

import java.util.Set;

/**
 * Class for growing and pruning log rank-based survival rules with user's knowledge.
 * 
 * @author Adam Gudys
 *
 */
public class SurvivalLogRankExpertFinder extends RegressionExpertFinder {

	public SurvivalLogRankExpertFinder(InductionParameters params) {
		super(params);
		this.params.setMeanBasedRegression(false);
	}

	SurvivalLogRankFinder.Implementation implementation = new SurvivalLogRankFinder.Implementation();


	@Override
	public IExampleSet preprocess(IExampleSet trainSet) {
		super.preprocess(trainSet);
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
