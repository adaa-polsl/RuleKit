package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * Calculates the cross-entropy for the predictions of a classifier.
 *
 */
public class CrossEntropy extends AbstractPerformanceCounter {


    /** The value of the criterion. */
    private double value = Double.NaN;

    private double counter = 1.0d;

    /** Clone constructor. */
    public CrossEntropy() {
    }

    /** Calculates the margin. */
    @Override
    public void startCounting(IExampleSet exampleSet) {
        // compute margin
        Iterator<Example> reader = exampleSet.iterator();
        this.value = 0.0d;
        IAttribute labelAttr = exampleSet.getAttributes().getLabel();
        IAttribute weightAttribute = null;
        weightAttribute = exampleSet.getAttributes().getWeight();

        while (reader.hasNext()) {
            Example example = reader.next();
            String trueLabel = example.getNominalValue(labelAttr);
            double confidence = example.getConfidence(trueLabel);
            double weight = 1.0d;
            if (weightAttribute != null) {
                weight = example.getValue(weightAttribute);
            }
            this.value -= weight * ld(confidence);

            this.counter += weight;
        }
    }

    private double ld(double value) {
        return Math.log(value) / Math.log(2.0);
    }

    @Override
    public void countExample(Example example) {
    }

    @Override
    public double getAverage() {
        return value / counter;
    }

    @Override
    public String getName() {
        return "cross-entropy";
    }


    /** Returns the super class implementation of toString(). */
    @Override
    public String toString() {
        return super.toString();
    }
}
