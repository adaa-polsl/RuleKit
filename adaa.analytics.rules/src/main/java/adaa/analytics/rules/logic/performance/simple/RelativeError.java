package adaa.analytics.rules.logic.performance.simple;

import adaa.analytics.rules.rm.tools.Tools;

/**
 * The average relative error: <i>Sum(|label-predicted|/label)/#examples</i>. The relative error of
 * label 0 and prediction 0 is defined as 0, the relative error of label 0 and prediction != 0 is
 * infinite.
 *
 */
public class RelativeError extends SimpleCriterion {


	public RelativeError() {}


	@Override
	public double countExample(double label, double predictedLabel) {
		double diff = Math.abs(label - predictedLabel);
		double absLabel = Math.abs(label);
		if (Tools.isZero(absLabel)) {
			return Double.NaN;
		} else {
			return diff / absLabel;
		}
	}

	@Override
	public String getName() {
		return "relative_error";
	}
}
