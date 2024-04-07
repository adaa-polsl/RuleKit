package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.utils.Tools;

import java.util.Iterator;


/**
 * Simple criteria are those which error can be counted for each example and can be averaged by the
 * number of examples. Since errors should be minimized, the fitness is calculated as -1 multiplied
 * by the the error. Subclasses might also want to implement the method
 * <code>transform(double)</code> which applies a transformation on the value sum divided by the
 * number of counted examples. This is for example usefull in case of root_means_squared error. All
 * subclasses can be used for both regression and classification problems. In case of classification
 * the confidence value for the desired true label is used as prediction.
 */
public class SimpleCriterion extends AbstractPerformanceCounter {

    private int type = -1;

    public static final int ABSOLUTE_ERROR = 0;
    public static final int LENIENT_RELATIVE_ERROR = 1;
    public static final int RELATIVE_ERROR = 2;
    public static final int ROOT_MEAN_SQUARED_ERROR = 3;
    public static final int SQUARED_ERROR = 4;
    public static final int STRICT_RELATIVE_ERROR = 5;

    private static final String[] NAMES = {"absolute_error", "relative_error_lenient", "relative_error","root_mean_squared_error", "squared_error", "relative_error_strict"};


    public SimpleCriterion(int type) {
        this.type = type;
    }


    /**
     * Subclasses must count the example and return the value to sum up.
     */
    protected double countExample(double label, double predictedLabel) {
        double diff, absLabel;
        switch (type) {
            case ABSOLUTE_ERROR:
                return Math.abs(label - predictedLabel);
            case LENIENT_RELATIVE_ERROR:
                diff = Math.abs(label - predictedLabel);
                absLabel = Math.abs(label);
                double absPrediction = Math.abs(predictedLabel);
                if (Tools.isZero(diff)) {
                    return 0.0d;
                } else {
                    return diff / Math.max(absLabel, absPrediction);
                }
            case RELATIVE_ERROR:
                diff = Math.abs(label - predictedLabel);
                absLabel = Math.abs(label);
                if (Tools.isZero(absLabel)) {
                    return Double.NaN;
                } else {
                    return diff / absLabel;
                }
            case ROOT_MEAN_SQUARED_ERROR:
            case SQUARED_ERROR:
                diff = label - predictedLabel;
                return diff * diff;
            case STRICT_RELATIVE_ERROR:
                diff = Math.abs(label - predictedLabel);
                absLabel = Math.abs(label);
                absPrediction = Math.abs(predictedLabel);
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
        return -1;
    }

    /**
     * Simply returns the given value. Subclasses might apply a transformation on the error sum
     * divided by the number of examples.
     */
    protected double transform(double value) {
        switch (type) {
            case ROOT_MEAN_SQUARED_ERROR:
                return Math.sqrt(value);
        }
        return value;
    }


    @Override
    public PerformanceResult countExample(IExampleSet eset) {
        double exampleCount = 0.0d;
        double sum = 0.0d;
        IAttribute predictedAttribute = eset.getAttributes().getPredictedLabel();
        IAttribute labelAttribute = eset.getAttributes().getLabel();
        IAttribute weightAttribute = eset.getAttributes().getWeight();
        Iterator<Example> exampleIterator = eset.iterator();
        while (exampleIterator.hasNext()) {
            Example example = exampleIterator.next();

            if ((Double.isNaN(example.getLabel()) || Double.isNaN(example.getPredictedLabel()))) {
                continue;
            }

            double plabel;
            double label = example.getValue(labelAttribute);
            double weight = 1.0d;
            if (weightAttribute != null) {
                weight = example.getValue(weightAttribute);
            }
            if (!predictedAttribute.isNominal()) {
                plabel = example.getValue(predictedAttribute);
            } else {
                String labelS = example.getNominalValue(labelAttribute);
                plabel = example.getConfidence(labelS);
                label = 1.0d;
            }

            double deviation = countExample(label, plabel);
            if (!Double.isNaN(deviation)) {
                sum += deviation * weight;
                exampleCount += weight;
            }
        }
        return new PerformanceResult(NAMES[type], transform(sum / exampleCount));
    }

}
