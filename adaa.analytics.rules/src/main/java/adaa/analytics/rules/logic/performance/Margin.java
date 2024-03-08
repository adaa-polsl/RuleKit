package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * The margin of a classifier, defined as the minimal confidence for the correct label.
 *
 */
public class Margin extends AbstractPerformanceCounter {


	/** The value of the criterion. */
	private double margin = Double.NaN;

	private double counter = 1.0d;

	/** Clone constructor. */
	public Margin() {}

	/** Calculates the margin. */
	@Override
	public void startCounting(IExampleSet exampleSet) {
		// compute margin
		Iterator<Example> reader = exampleSet.iterator();
		this.margin = 1.0d;
		IAttribute labelAttr = exampleSet.getAttributes().getLabel();
		while (reader.hasNext()) {
			Example example = reader.next();
			String trueLabel = example.getNominalValue(labelAttr);
			double confidence = example.getConfidence(trueLabel);
			this.margin = Math.min(margin, confidence);
		}
	}

	@Override
	public void countExample(Example example) {}

	@Override
	public double getAverage() {
		return margin / counter;
	}


	@Override
	public String getName() {
		return "margin";
	}

}
