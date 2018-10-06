package adaa.analytics.rules.logic.quality;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import com.rapidminer.tools.container.Pair;

public class ChiSquareVarianceTest extends StatisticalTest {
	
	public Pair<Double,Double> calculateLower(double expectedDev, double sampleDev, int samplesize) {
		double factor = sampleDev / expectedDev;
		double T = ((double)(samplesize - 1)) * (factor * factor); 
	
		ChiSquaredDistribution chi = new ChiSquaredDistribution(samplesize - 1);
		
		return new Pair<Double, Double>(T, chi.cumulativeProbability(T)) ;
	}
}
