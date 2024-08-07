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

import adaa.analytics.rules.utils.Pair;
import org.apache.commons.math3.distribution.HypergeometricDistribution;

/**
 * Class representing a hypergeometric statistical test.
 *
 * @author Adam Gudys
 */
public class Hypergeometric extends StatisticalTest {
	
	/**
	 * 
	 * @param ct Contingency table.
	 * @return (test statistics, p-value) pair
	 */
	public Pair<Double,Double> calculate(ContingencyTable ct) {
		return this.calculate(ct.weighted_p, ct.weighted_n,
				ct.weighted_P, ct.weighted_N);
	}
	
	public Pair<Double,Double> calculate(double p, double n, double P, double N) {

		int totalCount = (int)(P + N);
		int successCount = (int)P;
		int totalDrawn = (int)(p + n);
	    int successDrawn = (int)p;

		HypergeometricDistribution dist = new HypergeometricDistribution(totalCount, successCount, totalDrawn);
		double pVal = dist.upperCumulativeProbability(successDrawn);

		/*
		double pVal = 0.0;
	    for (int k = successDrawn; k <= Math.min(successCount, totalDrawn); k++) {
			pVal += dist.probability(k);
	    }
		*/
	    Pair<Double,Double> out = new Pair<Double,Double>(0.0, pVal);
	    	  
	    return out;
	}
	
	/*
	private double pmf(int totalDrawn, int successDrawn, int totalCount, int successCount)
	{
	    double a = CombinatoricsUtils.binomialCoefficientLog(
	        totalDrawn, successDrawn);
	    
	    double b = CombinatoricsUtils.binomialCoefficientLog(
		    	totalCount - totalDrawn, successCount - successDrawn);

	    double c = CombinatoricsUtils.binomialCoefficientLog(totalCount, successCount);
	    
	    assert(!Double.isInfinite(a));
	    assert(!Double.isInfinite(b));
	    assert(!Double.isInfinite(c));
	    assert(!Double.isInfinite(a * b));

	    return a + b - c;
	}
	*/

	//@Override
	public String getName() {
		return "HyperGeometricStatistics";
	}
}
