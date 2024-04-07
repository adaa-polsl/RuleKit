package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IExampleSet;

public abstract class AbstractPerformanceCounter {

	public AbstractPerformanceCounter() {
	}



	/** Counts a single example, e.g. by summing up errors. */
	public abstract PerformanceResult countExample(IExampleSet testSet);


}
