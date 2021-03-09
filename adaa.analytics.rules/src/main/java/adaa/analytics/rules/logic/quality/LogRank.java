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
package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.induction.ContingencyTable;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;

import com.rapidminer.example.ExampleSet;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import com.rapidminer.tools.container.Pair;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing log-rank test.
 *
 * @author Adam Gudys
 */
public class LogRank implements IQualityMeasure, Serializable {
	
	private static final long serialVersionUID = -6859067049486703913L;
	
	protected ChiSquaredDistribution dist = new ChiSquaredDistribution(1.0);

	@Override
	public String getName() {
		return "LogRankStatistics";
	}

	@Override
	public double calculate(double p, double n, double P, double N) {
		assert false: "LogRank: unable to calculate quality from contingency matrix only";
		return 0;
	}

	@Override
	public double calculate(ExampleSet dataset, ContingencyTable ct) {
		Covering cov = (Covering)ct;

		Set<Integer> coveredIndices = cov.positives; // in survival rules all examples are classified as positives
		Set<Integer> uncoveredIndices = new HashSet<Integer>();
		for (int i = 0; i < dataset.size(); ++i) {
			if (!coveredIndices.contains(i)) {
				uncoveredIndices.add(i);
			}
		}

		KaplanMeierEstimator coveredEstimator = new KaplanMeierEstimator(dataset, coveredIndices);
		KaplanMeierEstimator uncoveredEstimator = new KaplanMeierEstimator(dataset, uncoveredIndices);

		Pair<Double,Double> statsAndPValue = compareEstimators(coveredEstimator, uncoveredEstimator);
		return 1 - statsAndPValue.getSecond();
	}


	public Pair<Double,Double> compareEstimators(KaplanMeierEstimator kme1, KaplanMeierEstimator kme2) {
		
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


}
