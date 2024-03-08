package adaa.analytics.rules.logic.performance.simple;


/**
 * The absolute error: <i>Sum(|label-predicted|)/#examples</i>. Mean absolue error is the average of
 * the difference between predicted and actual value in all test cases; it is the average prediction
 * error.
 *
 */
public class AbsoluteError extends SimpleCriterion {

	public AbsoluteError() {}


	@Override
	public double countExample(double label, double predictedLabel) {
		double dif = Math.abs(label - predictedLabel);
		return dif;
	}

	@Override
	public String getName() {
		return "absolute_error";
	}
}
