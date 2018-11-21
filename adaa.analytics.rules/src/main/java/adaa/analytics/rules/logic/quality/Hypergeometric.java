package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.induction.ContingencyTable;

import org.apache.commons.math3.util.CombinatoricsUtils;

import com.rapidminer.tools.container.Pair;


public class Hypergeometric implements IQualityMeasure {
	
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

	@Override
	public String getName() {
		return "HyperGeometricStatistics";
	}
}
