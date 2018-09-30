package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.induction.Covering;
import org.apache.commons.math3.util.CombinatoricsUtils;

public class Hypergeometric implements IQualityMeasure {
	
	public StatisticalTestResult calculate(Covering cov) {
		return this.calculate(cov.weighted_p, cov.weighted_n,
				cov.weighted_P, cov.weighted_N);
	}
	
	public StatisticalTestResult calculate(double p, double n, double P, double N) {
		
		StatisticalTestResult res = new StatisticalTestResult();
		
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
	    
	    res.stats = q;
	    res.pvalue = pVal;
	    
	    return res;
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
