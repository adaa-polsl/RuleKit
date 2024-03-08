package adaa.analytics.rules.logic.performance.simple;


import adaa.analytics.rules.rm.tools.Tools;

/**
 * The average relative error in a strict way of calculation: <i>Sum(|label-predicted|/min(|label|,
 * |predicted|))/#examples</i>. The relative error of label 0 and prediction 0 is defined as 0. If
 * the minimum of label and prediction is 0, the relative error is defined as infinite.
 *
 */
public class StrictRelativeError extends SimpleCriterion {


	public StrictRelativeError() {}


	@Override
	public double countExample(double label, double predictedLabel) {
		double diff = Math.abs(label - predictedLabel);
		double absLabel = Math.abs(label);
		double absPrediction = Math.abs(predictedLabel);
		if (Tools.isZero(diff)) {
			return 0.0d;
		} else {
			double min = Math.min(absLabel, absPrediction);
			if (Tools.isZero(min)) {
				return Double.POSITIVE_INFINITY;
			} else {
				return diff / min;
			}
		}
	}

	@Override
	public String getName() {
		return "relative_error_strict";
	}

}
