package adaa.analytics.rules.logic.performance.simple;

import adaa.analytics.rules.rm.tools.Tools;


/**
 * The average relative error in a lenient way of calculation: <i>Sum(|label-predicted|/max(|label|,
 * |predicted|))/#examples</i>. The relative error of label 0 and prediction 0 is defined as 0.
 *
 */
public class LenientRelativeError extends SimpleCriterion {


	public LenientRelativeError() {}


	@Override
	public double countExample(double label, double predictedLabel) {
		double diff = Math.abs(label - predictedLabel);
		double absLabel = Math.abs(label);
		double absPrediction = Math.abs(predictedLabel);
		if (Tools.isZero(diff)) {
			return 0.0d;
		} else {
			return diff / Math.max(absLabel, absPrediction);
		}
	}

	@Override
	public String getName() {
		return "relative_error_lenient";
	}

}
