package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.tools.Tools;

import java.util.List;


public abstract class AbstractPerformanceCounter {

	public AbstractPerformanceCounter() {
	}
	/**
	 * Returns the name of this averagable.
	 */
	public abstract String getName();

	/**
	 * Returns the (current) value of the averagable (the average itself).
	 */
	public abstract double getAverage();


	/** Counts a single example, e.g. by summing up errors. */
	public abstract void countExample(Example example);


	/** Initializes the criterion. The default implementation does nothing. */
	public void startCounting(IExampleSet set)  {}

	public static String toString(List<AbstractPerformanceCounter> list) {
		StringBuffer result = new StringBuffer(Tools.getLineSeparator() + "Performance [");
		for (AbstractPerformanceCounter mp :list) {
			result.append(Tools.getLineSeparator() + "-----");
			result.append(mp.getName()+": "+mp.getAverage());
		}
		result.append(Tools.getLineSeparator() + "]");
		return result.toString();
	}
}
