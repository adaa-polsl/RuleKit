package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;


/**
 * Computes the empirical corelation coefficient 'r' between label and prediction. For
 * <code>P=prediction, L=label, V=Variance, Cov=Covariance</code> we calculate r by: <br>
 * <code>Cov(L,P) / sqrt(V(L)*V(P))</code>.
 *
 * Implementation hint: this implementation intensionally recomputes the mean and variance of
 * prediction and label despite the fact that they are available by the Attribute objects. The
 * reason: it can happen, that there are some examples which have a NaN as prediction or label, but
 * not both. In this case, mean and variance stored in tie Attributes and computed here can differ.
 *
 */
public class CorrelationCriterion extends AbstractPerformanceCounter {


    private IAttribute labelAttribute;

    private IAttribute predictedLabelAttribute;

    private IAttribute weightAttribute;

    private double exampleCount = 0;

    private double sumLabel;

    private double sumPredict;

    private double sumLabelPredict;

    private double sumLabelSqr;

    private double sumPredictSqr;

    public CorrelationCriterion() {
    }


    /** Updates all sums needed to compute the correlation coefficient. */
    @Override
    public void countExample(Example example) {
        double label = example.getValue(labelAttribute);
        double plabel = example.getValue(predictedLabelAttribute);
        if (labelAttribute.isNominal()) {
            String predLabelString = predictedLabelAttribute.getMapping().mapIndex((int) plabel);
            plabel = labelAttribute.getMapping().getIndex(predLabelString);
        }

        double weight = 1.0d;
        if (weightAttribute != null) {
            weight = example.getValue(weightAttribute);
        }

        double prod = label * plabel * weight;
        if (!Double.isNaN(prod)) {
            sumLabelPredict += prod;
            sumLabel += label * weight;
            sumLabelSqr += label * label * weight;
            sumPredict += plabel * weight;
            sumPredictSqr += plabel * plabel * weight;
            exampleCount += weight;
        }
    }

    @Override
    public double getAverage() {
        double divider = Math.sqrt(exampleCount * sumLabelSqr - sumLabel * sumLabel)
                * Math.sqrt(exampleCount * sumPredictSqr - sumPredict * sumPredict);
        double r = (exampleCount * sumLabelPredict - sumLabel * sumPredict) / divider;
        if (r < 0 || Double.isNaN(r)) {
            return 0; // possible due to rounding errors
        }
        if (r > 1) {
            return 1;
        }
        return r;
    }

    @Override
    public void startCounting(IExampleSet eset) {
        exampleCount = 0;
        sumLabelPredict = sumLabel = sumPredict = sumLabelSqr = sumPredictSqr = 0.0d;
        this.labelAttribute = eset.getAttributes().getLabel();
        this.predictedLabelAttribute = eset.getAttributes().getPredictedLabel();
        this.weightAttribute = eset.getAttributes().getWeight();
    }

    @Override
    public String getName() {
        return "correlation";
    }
}
