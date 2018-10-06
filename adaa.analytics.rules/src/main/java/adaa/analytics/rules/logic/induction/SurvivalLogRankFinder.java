package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;

import java.util.HashSet;
import java.util.Set;

public class SurvivalLogRankFinder extends RegressionFinder{

	public SurvivalLogRankFinder(InductionParameters params) {
		super(params);
		// TODO Auto-generated constructor stub
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
