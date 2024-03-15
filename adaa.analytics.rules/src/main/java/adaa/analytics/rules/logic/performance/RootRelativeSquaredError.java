package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * Relative squared error is the total squared error made relative to what the error would have been
 * if the prediction had been the average of the absolute value. As done with the root mean-squared
 * error, the square root of the relative squared error is taken to give it the same dimensions as
 * the predicted values themselves. Also, just like root mean-squared error, this exaggerates the
 * cases in which the prediction error was significantly greater than the mean error.
 *
 */
public class RootRelativeSquaredError extends AbstractPerformanceCounter {


    public RootRelativeSquaredError() {
    }

    @Override
    public PerformanceResult countExample(IExampleSet exampleSet){
        if (exampleSet.size() <= 1) {
            throw new IllegalStateException("root relative squared error can only be calculated for test sets with more than 2 examples.");
        }
        IAttribute predictedAttribute = exampleSet.getAttributes().getPredictedLabel();
        IAttribute labelAttribute = exampleSet.getAttributes().getLabel();
        IAttribute weightAttribute = exampleSet.getAttributes().getWeight();


        double trueLabelSum = 0.0d;
        double deviationSum = 0.0d;
        double relativeSum = 0.0d;
        double exampleCounter = 0.0d;
        Iterator<Example> reader = exampleSet.iterator();
        while (reader.hasNext()) {
            Example example = reader.next();
            double label = example.getValue(labelAttribute);
            if (!Double.isNaN(label)) {
                exampleCounter += 1;
                trueLabelSum += label;
            }
        }



        Iterator<Example> exampleIterator = exampleSet.iterator();
        while (exampleIterator.hasNext()) {
            Example example = exampleIterator.next();

            if ((Double.isNaN(example.getLabel()) || Double.isNaN(example.getPredictedLabel()))) {
                continue;
            }
            double plabel;
            double label = example.getValue(labelAttribute);

            if (!predictedAttribute.isNominal()) {
                plabel = example.getValue(predictedAttribute);
            } else {
                String labelS = example.getNominalValue(labelAttribute);
                plabel = example.getConfidence(labelS);
                label = 1.0d;
            }

            double weight = 1.0d;
            if (weightAttribute != null) {
                weight = example.getValue(weightAttribute);
            }

            double diff = Math.abs(label - plabel);
            deviationSum += diff * diff * weight * weight;
            double relDiff = Math.abs(label - (trueLabelSum / exampleCounter));
            relativeSum += relDiff * relDiff * weight * weight;
        }
        return new PerformanceResult("root_relative_squared_error", Math.sqrt(deviationSum / relativeSum));
    }

}
