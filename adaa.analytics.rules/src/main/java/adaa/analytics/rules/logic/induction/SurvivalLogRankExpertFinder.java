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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;

import java.util.HashSet;
import java.util.Set;

public class SurvivalLogRankExpertFinder extends RegressionExpertFinder {

	public SurvivalLogRankExpertFinder(InductionParameters params) {
		super(params);
	}
	
	@Override
	protected double calculateQuality(ExampleSet trainSet, ContingencyTable ct, IQualityMeasure measure) {
		Covering cov = (Covering)ct;
		
		Set<Integer> coveredIndices = cov.positives; // in survival rules all examples are classified as positives
		Set<Integer> uncoveredIndices = new HashSet<Integer>();
		for (int i = 0; i < trainSet.size(); ++i) {
			if (!coveredIndices.contains(i)) { 
				uncoveredIndices.add(i);
			}
		}
		
		KaplanMeierEstimator coveredEstimator = new KaplanMeierEstimator(trainSet, coveredIndices);
		KaplanMeierEstimator uncoveredEstimator = new KaplanMeierEstimator(trainSet, uncoveredIndices);
		
		Pair<Double,Double> statsAndPValue = ((LogRank)measure).calculate(coveredEstimator, uncoveredEstimator);
		return 1 - statsAndPValue.getSecond();
	}

	@Override
	protected Pair<Double,Double> calculateQualityAndPValue(ExampleSet trainSet, ContingencyTable ct, IQualityMeasure measure) {
		double logrank = calculateQuality(trainSet, ct, measure);
		return new Pair<Double,Double>(logrank, 1-logrank);
	}

}
