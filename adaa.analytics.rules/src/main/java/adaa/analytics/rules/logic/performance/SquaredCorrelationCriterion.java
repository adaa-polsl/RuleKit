package adaa.analytics.rules.logic.performance;

/**
 * Computes the square of the empirical corellation coefficient 'r' between label and prediction.
 * Eith P=prediction, L=label, V=Variance, Cov=Covariance we calculate r by: <br>
 * Cov(L,P) / sqrt(V(L)*V(P)). Uses the calculation of the superclass.
 *
 */
public class SquaredCorrelationCriterion extends CorrelationCriterion {


	@Override
	public double getAverage() {
		double r = super.getAverage();
		return r * r;
	}

	@Override
	public String getName() {
		return "squared_correlation";
	}
}
