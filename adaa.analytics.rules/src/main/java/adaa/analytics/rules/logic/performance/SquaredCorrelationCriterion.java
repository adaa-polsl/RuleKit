package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IExampleSet;

/**
 * Computes the square of the empirical corellation coefficient 'r' between label and prediction.
 * Eith P=prediction, L=label, V=Variance, Cov=Covariance we calculate r by: <br>
 * Cov(L,P) / sqrt(V(L)*V(P)). Uses the calculation of the superclass.
 *
 */
public class SquaredCorrelationCriterion extends AbstractPerformanceCounter {

	@Override
	public PerformanceResult countExample(IExampleSet eset) {
		CorrelationCriterion cc = new CorrelationCriterion();
		PerformanceResult pr = cc.countExample(eset);
		return new PerformanceResult("squared_correlation", pr.getValue()*pr.getValue());
	}

}
