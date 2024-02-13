package adaa.analytics.rules.rm.tools.math;

import Jama.Matrix;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Collection;
import java.util.Iterator;

public class MathFunctions {
    protected static final double log2 = Math.log(2.0);
    protected static final double[] DIVISOR_COEFFICIENTS_0 = new double[]{-59.96335010141079, 98.00107541859997, -56.67628574690703, 13.931260938727968, -1.2391658386738125};
    protected static final double[] DIVIDER_COEFFICIENTS_0 = new double[]{1.0, 1.9544885833814176, 4.676279128988815, 86.36024213908905, -225.46268785411937, 200.26021238006066, -82.03722561683334, 15.90562251262117, -1.1833162112133};
    protected static final double[] DIVISOR_COEFFICIENTS_1 = new double[]{4.0554489230596245, 31.525109459989388, 57.16281922464213, 44.08050738932008, 14.684956192885803, 2.1866330685079025, -0.1402560791713545, -0.03504246268278482, -8.574567851546854E-4};
    protected static final double[] DIVIDER_COEFFICIENTS_1 = new double[]{1.0, 15.779988325646675, 45.39076351288792, 41.3172038254672, 15.04253856929075, 2.504649462083094, -0.14218292285478779, -0.03808064076915783, -9.332594808954574E-4};
    protected static final double[] DIVISOR_COEFFICIENTS_3 = new double[]{3.2377489177694603, 6.915228890689842, 3.9388102529247444, 1.3330346081580755, 0.20148538954917908, 0.012371663481782003, 3.0158155350823543E-4, 2.6580697468673755E-6, 6.239745391849833E-9};
    protected static final double[] DIVIDER_COEFFICIENTS_3 = new double[]{1.0, 6.02427039364742, 3.6798356385616087, 1.3770209948908132, 0.21623699359449663, 0.013420400608854318, 3.2801446468212774E-4, 2.8924786474538068E-6, 6.790194080099813E-9};
    private static final int INVERSE_ITERATIONS = 5;
    private static final double MINIMUM_ADDITION = Double.MIN_VALUE * Math.pow(10.0, 5.0);
    private static final double MINIMUM_THRESHOLD = 4.94E-322;
    private static final double ADDITION_FACTOR = 1.01;

    public MathFunctions() {
    }

    public static double tanh(double x) {
        return (Math.exp(x) - Math.exp(-x)) / (Math.exp(x) + Math.exp(-x));
    }

    public static double normalInverse(double probability) {
        double smallArgumentEnd = Math.exp(-2.0);
        double rootedPi = Math.sqrt(6.283185307179586);
        if (probability <= 0.0) {
            throw new IllegalArgumentException();
        } else if (probability >= 1.0) {
            throw new IllegalArgumentException();
        } else {
            boolean wrappedArround = false;
            if (probability > 1.0 - smallArgumentEnd) {
                probability = 1.0 - probability;
                wrappedArround = true;
            }

            double x;
            double inversedX;
            if (probability > smallArgumentEnd) {
                probability -= 0.5;
                x = probability * probability;
                inversedX = probability + probability * (x * solvePolynomial(x, DIVISOR_COEFFICIENTS_0) / solvePolynomial(x, DIVIDER_COEFFICIENTS_0));
                inversedX *= rootedPi;
                return inversedX;
            } else {
                x = Math.sqrt(-2.0 * Math.log(probability));
                inversedX = 1.0 / x;
                if (x < 8.0) {
                    x = x - Math.log(x) / x - inversedX * solvePolynomial(inversedX, DIVISOR_COEFFICIENTS_1) / solvePolynomial(inversedX, DIVIDER_COEFFICIENTS_1);
                } else {
                    x = x - Math.log(x) / x - inversedX * solvePolynomial(inversedX, DIVISOR_COEFFICIENTS_3) / solvePolynomial(inversedX, DIVIDER_COEFFICIENTS_3);
                }

                if (!wrappedArround) {
                    x = -x;
                }

                return x;
            }
        }
    }

    public static double solvePolynomial(double x, double[] coefficients) {
        double value = coefficients[0];

        for(int i = 1; i < coefficients.length; ++i) {
            value += coefficients[i] * Math.pow(x, (double)i);
        }

        return value;
    }

    public static double variance(double[] v, double a) {
        double sum = 0.0;
        int counter = 0;

        for(int i = 0; i < v.length; ++i) {
            if (v[i] >= a) {
                sum += v[i];
                ++counter;
            }
        }

        double mean = sum / (double)counter;
        sum = 0.0;
        counter = 0;

        for(int i = 0; i < v.length; ++i) {
            if (v[i] >= a) {
                sum += (v[i] - mean) * (v[i] - mean);
                ++counter;
            }
        }

        double variance = sum / (double)counter;
        return variance;
    }

    public static double correlation(IExampleSet exampleSet, IAttribute firstAttribute, IAttribute secondAttribute, boolean squared) {
        double sumProd = 0.0;
        double sumFirst = 0.0;
        double sumSecond = 0.0;
        double sumFirstSquared = 0.0;
        double sumSecondSquared = 0.0;
        int counter = 0;
        Iterator<Example> reader = exampleSet.iterator();

        while(reader.hasNext()) {
            Example example = (Example)reader.next();
            double first = example.getValue(firstAttribute);
            double second = example.getValue(secondAttribute);
            double prod = first * second;
            if (!Double.isNaN(prod)) {
                sumProd += prod;
                sumFirst += first;
                sumFirstSquared += first * first;
                sumSecond += second;
                sumSecondSquared += second * second;
                ++counter;
            }
        }

        double divisor = Math.sqrt(((double)counter * sumFirstSquared - sumFirst * sumFirst) * ((double)counter * sumSecondSquared - sumSecond * sumSecond));
        double r;
        if (divisor == 0.0) {
            r = Double.NaN;
        } else {
            r = ((double)counter * sumProd - sumFirst * sumSecond) / divisor;
        }

        if (squared) {
            return r * r;
        } else {
            return r;
        }
    }

    public static double correlation(double[] x1, double[] x2) {
        int counter = 0;
        double sum1 = 0.0;
        double sum2 = 0.0;
        double sumS1 = 0.0;
        double sumS2 = 0.0;

        for(int i = 0; i < x1.length; ++i) {
            sum1 += x1[i];
            sum2 += x2[i];
            ++counter;
        }

        double mean1 = sum1 / (double)counter;
        double mean2 = sum2 / (double)counter;
        double sum = 0.0;
        counter = 0;

        for(int i = 0; i < x1.length; ++i) {
            sum += (x1[i] - mean1) * (x2[i] - mean2);
            sumS1 += (x1[i] - mean1) * (x1[i] - mean1);
            sumS2 += (x2[i] - mean2) * (x2[i] - mean2);
            ++counter;
        }

        return sum / Math.sqrt(sumS1 * sumS2);
    }

    public static double robustMin(double m1, double m2) {
        double min = Math.min(m1, m2);
        if (!Double.isNaN(min)) {
            return min;
        } else {
            return Double.isNaN(m1) ? m2 : m1;
        }
    }

    public static double robustMax(double m1, double m2) {
        double max = Math.max(m1, m2);
        if (!Double.isNaN(max)) {
            return max;
        } else {
            return Double.isNaN(m1) ? m2 : m1;
        }
    }

    public static double ld(double value) {
        return Math.log(value) / log2;
    }

    public static long getGCD(long a, long b) {
        while(b != 0L) {
            long c = a % b;
            a = b;
            b = c;
        }

        return a;
    }

    public static long getGCD(Collection<Long> collection) {
        boolean first = true;
        long currentGCD = 1L;
        Iterator<Long> i = collection.iterator();

        while(i.hasNext()) {
            long value = (Long)i.next();
            if (first) {
                currentGCD = value;
                first = false;
            } else {
                currentGCD = getGCD(currentGCD, value);
            }
        }

        return currentGCD;
    }

    public static int factorial(int k) {
        int result = 1;

        for(int i = k; i > 1; --i) {
            result += i;
        }

        return result;
    }

    /** @deprecated */
    @Deprecated
    public static Matrix invertMatrix(Matrix m) {
        double startFactor = 0.1;

        while(true) {
            try {
                Matrix inverse = m.inverse();
                return inverse;
            } catch (Exception var6) {
                for(int x = 0; x < m.getColumnDimension(); ++x) {
                    for(int y = 0; y < m.getRowDimension(); ++y) {
                        m.set(x, y, m.get(x, y) + startFactor);
                    }
                }

                startFactor *= 10.0;
            }
        }
    }

    public static Matrix invertMatrix(Matrix m, boolean approximate) {
        int dimension = Math.min(m.getRowDimension(), m.getColumnDimension());
        int i = 0;

        while(i < 5) {
            try {
                return m.inverse();
            } catch (Exception var8) {
                if (!approximate) {
                    return null;
                }

                for(int x = 0; x < dimension; ++x) {
                    double value = m.get(x, x);
                    value = Math.abs(value) <= 4.94E-322 ? MINIMUM_ADDITION : value * 1.01;
                    m.set(x, x, value);
                }

                ++i;
            }
        }

        return null;
    }
}
