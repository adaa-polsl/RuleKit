package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.Set;

import adaa.analytics.rules.logic.induction.AbstractFinder.QualityAndPValue;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.quality.StatisticalTestResult;
import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;

import com.rapidminer.example.ExampleSet;

public class SurvivalLogRankExpertFinder extends RegressionExpertFinder {

	public SurvivalLogRankExpertFinder(InductionParameters params) {
		super(params);
	}
	
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
		
		StatisticalTestResult res = ((LogRank)measure).calculate(coveredEstimator, uncoveredEstimator);
		return 1 - res.pvalue;
	}

	
	protected QualityAndPValue calculateQualityAndPValue(ExampleSet trainSet, Covering cov, IQualityMeasure measure) {
		QualityAndPValue res = new QualityAndPValue();
		res.quality = calculateQuality(trainSet, cov, measure);
		res.pvalue = 1 - res.quality;
		
		return res;
	}

}
