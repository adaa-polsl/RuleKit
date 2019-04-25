package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import com.rapidminer.tools.container.Pair;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class LogRank implements IQualityMeasure, Serializable {
	
	private static final long serialVersionUID = -6859067049486703913L;
	
	protected ChiSquaredDistribution dist = new ChiSquaredDistribution(1.0);
	
	public Pair<Double,Double> calculate(KaplanMeierEstimator kme1, KaplanMeierEstimator kme2) {
		
		Pair<Double,Double> res = new Pair<Double,Double>(0.0, 0.0);
		
		Set<Double> eventTimes = new HashSet<Double>();
		eventTimes.addAll(kme1.getTimes());
		eventTimes.addAll(kme2.getTimes());
		
		// fixme:
		if (kme1.getTimes().size() == 0 || kme2.getTimes().size() == 0) {
			return res;
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

        res.setFirst((x * x) / y);
        res.setSecond(1.0 - dist.cumulativeProbability(res.getFirst()));
        
		return res;
	}

	@Override
	public String getName() {
		return "LogRankStatistics";
	}
}
