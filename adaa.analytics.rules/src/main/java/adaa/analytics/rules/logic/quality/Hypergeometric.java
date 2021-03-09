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

import org.apache.commons.math3.util.CombinatoricsUtils;

import com.rapidminer.tools.container.Pair;

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
			
		int count = (int)(P + N);
		int consequent = (int)P;
	    int antecedentAndConsequent = (int)p;
	    int antecedentButNotConsequent = (int)n;
	    int notAntecedentButConsequent = (int)(P - p);
	    
	    int limit = Math.min(notAntecedentButConsequent, antecedentButNotConsequent);

	    double pVal = 0.0;
	    for (int k = 0; k <= limit; k++)
	    {
	        double lnP = this.LnP(antecedentAndConsequent + k, antecedentButNotConsequent - k, count, consequent);
	        pVal += Math.exp(lnP);
	    }

	    double q = -Math.log(pVal);
	    if (Double.isInfinite(q))
	    {
	        q = Double.MAX_VALUE;
	    }

	    assert(q >= -1.0 / 10000000000.0);
	    assert(!Double.isNaN(q));
	    
	    Pair<Double,Double> out = new Pair<Double,Double>(q, pVal);
	    	  
	    return out;
	}
	
	
	private double LnP(int antecedentAndConsequent, int antecedentButNotConsequent, int count, int consequent)
	{
	    double a = CombinatoricsUtils.binomialCoefficientLog(
	        antecedentAndConsequent + antecedentButNotConsequent, antecedentAndConsequent);
	    
	    double b = CombinatoricsUtils.binomialCoefficientLog(
		    	count - antecedentAndConsequent - antecedentButNotConsequent, consequent - antecedentAndConsequent);

	    double c = CombinatoricsUtils.binomialCoefficientLog(count, consequent);
	    
	    assert(!Double.isInfinite(a));
	    assert(!Double.isInfinite(b));
	    assert(!Double.isInfinite(c));
	    assert(!Double.isInfinite(a * b));

	    return a + b - c;
	}

	//@Override
	public String getName() {
		return "HyperGeometricStatistics";
	}
}
