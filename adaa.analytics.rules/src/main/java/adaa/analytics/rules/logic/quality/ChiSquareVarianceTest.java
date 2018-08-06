package adaa.analytics.rules.logic.quality;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class ChiSquareVarianceTest extends StatisticalTest {
	
	public StatisticalTestResult calculateLower(double expectedDev, double sampleDev, int samplesize) {
		double factor = sampleDev / expectedDev;
		double T = ((double)(samplesize - 1)) * (factor * factor); 
		
		StatisticalTestResult res = new StatisticalTestResult();
		res.stats = T;
		
		ChiSquaredDistribution chi = new ChiSquaredDistribution(samplesize - 1);
		
		res.pvalue = chi.cumulativeProbability(T);
		
		return res;
	}
}
