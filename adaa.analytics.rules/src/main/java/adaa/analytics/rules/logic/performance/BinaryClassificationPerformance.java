package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.logic.performance.AbstractPerformanceCounter;
import adaa.analytics.rules.logic.performance.PerformanceResult;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * This class encapsulates the well known binary classification criteria precision and recall.
 * Furthermore it can be used to calculate the fallout, the equally weighted f-measure (f1-measure),
 * the lift, and the values for TRUE_POSITIVE, FALSE_POSITIVE, TRUE_NEGATIVE, and FALSE_NEGATIVE.
 * With &quot;positive&quot; we refer to the first class and with &quot;negative&quot; we refer to
 * the second.
 *
 */
public class BinaryClassificationPerformance extends AbstractPerformanceCounter {


    public static final int PRECISION = 0;


    public static final int LIFT = 2;

    public static final int FALLOUT = 3;

    public static final int F_MEASURE = 4;

    public static final int FALSE_POSITIVE = 5;

    public static final int FALSE_NEGATIVE = 6;

    public static final int TRUE_POSITIVE = 7;

    public static final int TRUE_NEGATIVE = 8;

    public static final int SENSITIVITY = 9;

    public static final int SPECIFICITY = 10;

    public static final int YOUDEN = 11;

    public static final int NEGATIVE_PREDICTIVE_VALUE = 13;

    public static final int PSEP = 14;

    public static final int GEOMETRIC_MEAN = 15;

    private static final int N = 0;

    private static final int P = 1;

    public static final String[] NAMES = {"precision", "recall", "lift", "fallout", "f_measure", "false_positive",
            "false_negative", "true_positive", "true_negative", "sensitivity", "specificity", "youden",
            "positive_predictive_value", "negative_predictive_value", "psep", "geometric_mean"};

    private int type = 0;

    /** true label, predicted label. PP = TP, PN = FN, NP = FP, NN = TN. */
    private double[][] counter = new double[2][2];


    public BinaryClassificationPerformance() {
        type = -1;
    }

    public BinaryClassificationPerformance(int type) {
        this.type = type;
    }


    // ================================================================================

    @Override
    public PerformanceResult countExample(IExampleSet eSet) {
        /** The predicted label attribute. */
        IAttribute predictedLabelAttribute = eSet.getAttributes().getPredictedLabel();
        /** The label attribute. */
        IAttribute labelAttribute = eSet.getAttributes().getLabel();
        if (!labelAttribute.isNominal()) {
            throw new IllegalStateException();
        }
        if (!predictedLabelAttribute.isNominal()) {
            throw new IllegalStateException();
        }
        if (labelAttribute.getMapping().size() != 2) {
            throw new IllegalStateException();
        }
        if (predictedLabelAttribute.getMapping().size() != 2) {
            throw new IllegalStateException();
        }
        if (!labelAttribute.getMapping().equals(predictedLabelAttribute.getMapping())) {
            throw new IllegalStateException();
        }
        /** Name of the positive class. */
        String positiveClassName = predictedLabelAttribute.getMapping().getPositiveString();

        /** The weight attribute. Might be null. */
        IAttribute weightAttribute = eSet.getAttributes().getWeight();
        this.counter = new double[2][2];

        Iterator<Example> exampleIterator = eSet.iterator();
        while (exampleIterator.hasNext()) {
            Example example = exampleIterator.next();

            if ((Double.isNaN(example.getLabel()) || Double.isNaN(example.getPredictedLabel()))) {
                continue;
            }
            String labelString = example.getNominalValue(labelAttribute);
            int label = positiveClassName.equals(labelString) ? P : N;
            String predString = example.getNominalValue(predictedLabelAttribute);
            int plabel = positiveClassName.equals(predString) ? P : N;

            double weight = 1.0d;
            if (weightAttribute != null) {
                weight = example.getValue(weightAttribute);
            }
            counter[label][plabel] += weight;

        }
        return new PerformanceResult(NAMES[type], countResultValue());
    }

    public double countResultValue() {
        double x = 0.0d, y = 0.0d;
        switch (type) {
            case PRECISION:
                x = counter[P][P];
                y = counter[P][P] + counter[N][P];
                break;
            case LIFT:
                x = counter[P][P] / (counter[P][P] + counter[P][N]);
                y = (counter[P][P] + counter[N][P]) / (counter[P][P] + counter[P][N] + counter[N][P] + counter[N][N]);
                break;
            case FALLOUT:
                x = counter[N][P];
                y = counter[N][P] + counter[N][N];
                break;

            case F_MEASURE:
                x = counter[P][P];
                x *= x;
                x *= 2;
                y = x + counter[P][P] * counter[P][N] + counter[P][P] * counter[N][P];
                break;

            case FALSE_NEGATIVE:
                x = counter[P][N];
                y = 1;
                break;
            case FALSE_POSITIVE:
                x = counter[N][P];
                y = 1;
                break;
            case TRUE_NEGATIVE:
                x = counter[N][N];
                y = 1;
                break;
            case TRUE_POSITIVE:
                x = counter[P][P];
                y = 1;
                break;
            case SENSITIVITY:
                x = counter[P][P];
                y = counter[P][P] + counter[P][N];
                break;
            case SPECIFICITY:
                x = counter[N][N];
                y = counter[N][N] + counter[N][P];
                break;
            case YOUDEN:
                x = counter[N][N] * counter[P][P] - counter[P][N] * counter[N][P];
                y = (counter[P][P] + counter[P][N]) * (counter[N][P] + counter[N][N]);
                break;
            case NEGATIVE_PREDICTIVE_VALUE:
                x = counter[N][N];
                y = counter[N][N] + counter[P][N];
                break;
            case PSEP:
                x = counter[N][N] * counter[P][P] + counter[N][N] * counter[N][P] - counter[N][P] * counter[N][N]
                        - counter[N][P] * counter[P][N];
                y = counter[P][P] * counter[N][N] + counter[P][P] * counter[P][N] + counter[N][P] * counter[N][N]
                        + counter[N][P] * counter[P][N];
                break;
            case GEOMETRIC_MEAN:
                return countGeometricMean();
            default:
                throw new RuntimeException("Illegal value for type in BinaryClassificationPerformance: " + type);
        }
        if (y == 0) {
            return Double.NaN;
        }
        return x / y;
    }



    private double countGeometricMean() {
        double x = 0.0d, y = 0.0d;


        x = counter[P][P];
        y = counter[P][P] + counter[P][N];

        if (y == 0) {
            return Double.NaN;
        }

        double se = x / y;

        x = counter[N][N];
        y = counter[N][N] + counter[N][P];

        if (y == 0) {
            return Double.NaN;
        }

        double sp = x / y;

        return Math.sqrt(se * sp);

    }
}
