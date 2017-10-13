package adaa.analytics.rules.induction;

import java.util.HashSet;
import java.util.Set;

import adaa.analytics.rules.logic.KaplanMeierEstimator;
import adaa.analytics.rules.quality.IQualityMeasure;
import adaa.analytics.rules.quality.LogRank;

import com.rapidminer.example.ExampleSet;

public class SurvivalLogRankFinder extends RegressionFinder{

	public SurvivalLogRankFinder(InductionParameters params) {
		super(params);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param cov
	 * @return
	 */
	protected double calculateQuality(ExampleSet trainSet, Covering cov, IQualityMeasure measure) {
		Set<Integer> coveredIndices = cov.positives; // in survival rules all examples are classified as positives
		Set<Integer> uncoveredIndices = new HashSet<Integer>();
		for (int i = 0; i < trainSet.size(); ++i) {
			if (!coveredIndices.contains(i)) { 
				uncoveredIndices.add(i);
			}
		}
		
		KaplanMeierEstimator coveredEstimator = new KaplanMeierEstimator(trainSet, coveredIndices);
		KaplanMeierEstimator uncoveredEstimator = new KaplanMeierEstimator(trainSet, uncoveredIndices);
		
		double quality = ((LogRank)measure).calculate(coveredEstimator, uncoveredEstimator);
		
		return quality;
	}

}
