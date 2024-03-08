package adaa.analytics.rules.logic.performance.simple;


/**
 * The squared error. Sums up the square of the absolute deviations and divides the sum by the
 * number of examples.
 *
 */
public class SquaredError extends SimpleCriterion {


	public SquaredError() {}


	@Override
	public String getName() {
		return "squared_error";
	}

	/** Calculates the error for the current example. */
	@Override
	public double countExample(double label, double predictedLabel) {
		double dif = label - predictedLabel;
		return dif * dif;
	}
}
