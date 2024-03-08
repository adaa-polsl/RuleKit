package adaa.analytics.rules.logic.performance;


import adaa.analytics.rules.rm.example.Example;

public class RecountedPerformance extends AbstractPerformanceCounter {

	private String name;

	private double value;


	public RecountedPerformance(String name, double value) {
		this.name = name;
		this.value = value;
	}


	@Override
	public final double getAverage() {
		return value;
	}

	@Override
	public void countExample(Example example) {
	}

	@Override
	public String getName() {
		return name;
	}


}
