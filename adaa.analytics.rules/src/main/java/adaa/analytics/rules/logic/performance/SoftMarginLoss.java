package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * The soft margin loss of a classifier, defined as the average over all 1 - y * f(x).
 *
 */
public class SoftMarginLoss extends AbstractPerformanceCounter {


    /** The value of the margin. */
    private double margin = Double.NaN;

    /** A counter for average building. */
    private double counter = 1;

    /** Clone constructor. */
    public SoftMarginLoss() {
    }

    /** Calculates the margin. */
    @Override
    public void startCounting(IExampleSet exampleSet) {
        // compute margin
        Iterator<Example> reader = exampleSet.iterator();
        this.margin = 0.0d;
        this.counter = 0.0d;
        IAttribute labelAttr = exampleSet.getAttributes().getLabel();
        IAttribute weightAttribute = exampleSet.getAttributes().getWeight();

        while (reader.hasNext()) {
            Example example = reader.next();
            String trueLabel = example.getNominalValue(labelAttr);
            double confidence = example.getConfidence(trueLabel);
            double currentMargin = Math.max(0, 1.0d - confidence);

            double weight = 1.0d;
            if (weightAttribute != null) {
                weight = example.getValue(weightAttribute);
            }
            this.margin += currentMargin * weight;

            this.counter += weight;
        }
    }

    /** Does nothing. Everything is done in {@link #startCounting(ExampleSet, boolean)}. */
    @Override
    public void countExample(Example example) {
    }

    @Override
    public double getAverage() {
        return margin / counter;
    }

    @Override
    public String getName() {
        return "soft_margin_loss";
    }
}
