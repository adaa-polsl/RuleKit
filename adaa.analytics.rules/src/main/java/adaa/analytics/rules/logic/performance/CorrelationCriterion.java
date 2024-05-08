package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.IExampleSet;

import java.util.Iterator;


/**
 * Computes the empirical corelation coefficient 'r' between label and prediction. For
 * <code>P=prediction, L=label, V=Variance, Cov=Covariance</code> we calculate r by: <br>
 * <code>Cov(L,P) / sqrt(V(L)*V(P))</code>.
 * <p>
 * Implementation hint: this implementation intensionally recomputes the mean and variance of
 * prediction and label despite the fact that they are available by the Attribute objects. The
 * reason: it can happen, that there are some examples which have a NaN as prediction or label, but
 * not both. In this case, mean and variance stored in tie Attributes and computed here can differ.
 */
public class CorrelationCriterion extends AbstractPerformanceCounter {


    public CorrelationCriterion() {
    }


    /**
     * Updates all sums needed to compute the correlation coefficient.
     */
    @Override
    public PerformanceResult countExample(IExampleSet eset) {
        double exampleCount = 0;
        double sumLabel;
        double sumPredict;
        double sumLabelSqr;
        double sumPredictSqr;
        double sumLabelPredict = sumLabel = sumPredict = sumLabelSqr = sumPredictSqr = 0.0d;
        IAttribute labelAttribute = eset.getAttributes().getLabel();
        IAttribute predictedLabelAttribute = eset.getAttributes().getPredictedLabel();
        IAttribute weightAttribute = eset.getAttributes().getWeight();

        Iterator<Example> exampleIterator = eset.iterator();
        while (exampleIterator.hasNext()) {
            Example example = exampleIterator.next();

            if ((Double.isNaN(example.getLabel()) || Double.isNaN(example.getPredictedLabel()))) {
                continue;
            }

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

        double divider = Math.sqrt(exampleCount * sumLabelSqr - sumLabel * sumLabel)
                * Math.sqrt(exampleCount * sumPredictSqr - sumPredict * sumPredict);
        double r = (exampleCount * sumLabelPredict - sumLabel * sumPredict) / divider;
        if (r < 0 || Double.isNaN(r)) {
            return new PerformanceResult("correlation",0);
        }
        if (r > 1) {
            return new PerformanceResult("correlation",1 );
        }
        return new PerformanceResult("correlation",r);
    }
}
