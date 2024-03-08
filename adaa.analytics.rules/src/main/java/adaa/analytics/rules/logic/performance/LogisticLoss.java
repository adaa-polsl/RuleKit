package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * The logistic loss of a classifier, defined as the average over all ln(1 + exp(-y * f(x)))
 *
 */
public class LogisticLoss extends AbstractPerformanceCounter {


    /**
     * The value of the loss.
     */
    private double loss = Double.NaN;

    private double counter = 0.0d;

    /**
     * Clone constructor.
     */
    public LogisticLoss() {
    }

    /**
     * Calculates the margin.
     */
    @Override
    public void startCounting(IExampleSet exampleSet) {
        // compute margin
        Iterator<Example> reader = exampleSet.iterator();
        this.loss = 0.0d;
        this.counter = 0.0d;
        IAttribute labelAttr = exampleSet.getAttributes().getLabel();
        IAttribute weightAttr = null;
        weightAttr = exampleSet.getAttributes().getWeight();


        while (reader.hasNext()) {
            Example example = reader.next();
            String trueLabel = example.getNominalValue(labelAttr);
            double confidence = example.getConfidence(trueLabel);
            double weight = 1.0d;
            if (weightAttr != null) {
                weight = example.getValue(weightAttr);
            }
            double currentMargin = weight * Math.log(1.0d + Math.exp(-1 * confidence));
            this.loss += currentMargin;
            this.counter += weight;
        }
    }

    @Override
    public void countExample(Example example) {
    }

    @Override
    public double getAverage() {
        return this.loss / counter;
    }

    @Override
    public String getName() {
        return "logistic_loss";
    }

}
