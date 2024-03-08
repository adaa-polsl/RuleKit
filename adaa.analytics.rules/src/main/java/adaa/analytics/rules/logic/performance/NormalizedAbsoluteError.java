package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * Normalized absolute error is the total absolute error normalized by the error simply predicting
 * the average of the actual values.
 *
 */
public class NormalizedAbsoluteError extends AbstractPerformanceCounter {

    private IAttribute predictedAttribute;

    private IAttribute labelAttribute;

    private IAttribute weightAttribute;

    private double deviationSum = 0.0d;

    private double relativeSum = 0.0d;

    private double trueLabelSum = 0.0d;

    private double exampleCounter = 0.0d;

    public NormalizedAbsoluteError() {
    }

    @Override
    public String getName() {
        return "normalized_absolute_error";
    }

    @Override
    public void startCounting(IExampleSet exampleSet) {
        if (exampleSet.size() <= 1) {
            throw new IllegalStateException(getName() + " " +
                    "normalized absolute error can only be calculated for test sets with more than 2 examples.");
        }
        this.predictedAttribute = exampleSet.getAttributes().getPredictedLabel();
        this.labelAttribute = exampleSet.getAttributes().getLabel();
        this.weightAttribute = exampleSet.getAttributes().getWeight();

        this.trueLabelSum = 0.0d;
        this.deviationSum = 0.0d;
        this.relativeSum = 0.0d;
        this.exampleCounter = 0.0d;
        Iterator<Example> reader = exampleSet.iterator();
        while (reader.hasNext()) {
            Example example = reader.next();
            double label = example.getLabel();
            double weight = 1.0d;
            if (weightAttribute != null) {
                weight = example.getValue(weightAttribute);
            }
            if (!Double.isNaN(label)) {
                exampleCounter += weight;
                trueLabelSum += label * weight;
            }
        }
    }

    /**
     * Calculates the error for the current example.
     */
    @Override
    public void countExample(Example example) {
        double plabel;
        double label = example.getValue(labelAttribute);

        if (!predictedAttribute.isNominal()) {
            plabel = example.getValue(predictedAttribute);
        } else {
            String labelS = example.getValueAsString(labelAttribute);
            plabel = example.getConfidence(labelS);
            label = 1.0d;
        }

        double weight = 1.0d;
        if (weightAttribute != null) {
            weight = example.getValue(weightAttribute);
        }

        double diff = weight * Math.abs(label - plabel);
        deviationSum += diff;
        double relDiff = Math.abs(weight * label - (trueLabelSum / exampleCounter));
        relativeSum += relDiff;
    }

    @Override
    public double getAverage() {
        return deviationSum / relativeSum;
    }

}
