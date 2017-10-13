package adaa.analytics.rules.quality;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import adaa.analytics.rules.logic.KaplanMeierEstimator;

public class LogRank implements IQualityMeasure {
	
	protected ChiSquaredDistribution dist = new ChiSquaredDistribution(1.0);
	
	public double calculate(KaplanMeierEstimator kme1, KaplanMeierEstimator kme2) {
		
		Set<Double> eventTimes = new HashSet<Double>();
		eventTimes.addAll(kme1.getTimes());
		eventTimes.addAll(kme2.getTimes());
		
		// fixme:
		if (kme1.getTimes().size() == 0 || kme2.getTimes().size() == 0) {
			return 0;
		}
		
        double x = 0;
        double y = 0;
        for (double time : eventTimes) {
            double m1 = kme1.getEventsCountAt(time);
            double n1 = kme1.getRiskSetCountAt(time);

            double m2 = kme2.getEventsCountAt(time);
            double n2 = kme2.getRiskSetCountAt(time);

            //Debug.WriteLine(string.Format("time={0}, m1={1} m2={2} n1={3} n2={4}", time, m1, m2, n1, n2));

            double e2 = (n2 / (n1 + n2)) * (m1 + m2);

            x += m2 - e2;

            double n = n1 + n2;
            y += (n1 * n2 * (m1 + m2) * (n - m1 - m2)) / (n * n * (n - 1));
        }

        double chi = (x * x) / y;
        double p = dist.cumulativeProbability(chi);
        
		return p;
	}
}
