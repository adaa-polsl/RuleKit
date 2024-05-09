package adaa.analytics.rules.utils;

public class Tools {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    public static boolean isEqual(double d1, double d2) {
        if (Double.isNaN(d1) && Double.isNaN(d2)) {
            return true;
        } else if (!Double.isNaN(d1) && !Double.isNaN(d2)) {
            return Math.abs(d1 - d2) < 1.0E-6;
        } else {
            return false;
        }
    }

    public static boolean isZero(double d) {
        return isEqual(d, 0.0);
    }

    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }

    public static Double convertToDouble(Object variable) {
        if (variable instanceof Boolean) {
            return (Boolean) variable ? 1.0 : 0.0;
        } else if (variable instanceof Double) {
            return (Double) variable;
        } else if (variable instanceof Float) {
            return ((Float) variable).doubleValue();
        } else if (variable instanceof Integer) {
            return ((Integer) variable).doubleValue();
        } else if (variable instanceof Long) {
            return ((Long) variable).doubleValue();
        } else {
            return Double.NaN;
        }
    }

}
