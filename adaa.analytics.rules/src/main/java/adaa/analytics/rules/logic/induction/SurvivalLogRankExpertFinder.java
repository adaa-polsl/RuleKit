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

import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;

import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;

import java.util.HashSet;
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
			final ExampleSet dataset) {

		Covering cov = rule.covers(dataset);
		Set<Integer> covered = cov.positives;
		KaplanMeierEstimator kme = new KaplanMeierEstimator(dataset, covered);
		((SurvivalRule)rule).setEstimator(kme);
	}
}
