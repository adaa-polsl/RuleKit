package adaa.analytics.rules.logic.representation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;


public class DoubleFormatter {

    private static final double THRESHOLD = 0.1;
    private static final int DEFAULT_NUMBER_OF_SIGNIFICANT_FIGURES = 2;

    private static int numberOfSignificantFigures = DEFAULT_NUMBER_OF_SIGNIFICANT_FIGURES;

    public static void configure(int numberOfSignificantFigures) {
        if (numberOfSignificantFigures < 0) throw new IllegalArgumentException();
        DoubleFormatter.numberOfSignificantFigures = numberOfSignificantFigures;
    }

    public static void defaultConfigure() {
        numberOfSignificantFigures = DEFAULT_NUMBER_OF_SIGNIFICANT_FIGURES;
    }

    public static String format(double value) {
        if(value < THRESHOLD) {
            BigDecimal bigDecimal = new BigDecimal(value);
            bigDecimal = bigDecimal.round(new MathContext(DoubleFormatter.numberOfSignificantFigures));
            double rounded = bigDecimal.doubleValue();

            return String.valueOf(rounded);
        } else {
            // Locale.US zapewnia że użyta zostanie kropka zamiast przecinka
            return String.format(Locale.US, "%.2f", value);
        }
    }
}
