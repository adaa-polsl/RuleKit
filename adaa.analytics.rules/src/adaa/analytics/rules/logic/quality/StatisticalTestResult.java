
package adaa.analytics.rules.logic.quality;

public class StatisticalTestResult {
	public double stats;
	public double pvalue;
	
	public StatisticalTestResult() {
		stats = Double.NEGATIVE_INFINITY;
		pvalue = 1.0;
	}
}