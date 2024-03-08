package adaa.analytics.rules.rm.tools;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.text.*;
import java.util.*;

public class Tools {

    private static final String[] MEMORY_UNITS = new String[]{"b", "kB", "MB", "GB", "TB"};
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final double IS_ZERO = 1.0E-6;
    private static final ThreadLocal<DateFormat> TIME_FORMAT = ThreadLocal.withInitial(() -> {
        return (DateFormat)DateFormat.getTimeInstance(1, Locale.getDefault()).clone();
    });
    private static final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> {
        return (DateFormat)DateFormat.getDateInstance(2, Locale.getDefault()).clone();
    });
    public static final ThreadLocal<DateFormat> DATE_TIME_FORMAT = ThreadLocal.withInitial(() -> {
        return (DateFormat)DateFormat.getDateTimeInstance(2, 1, Locale.getDefault()).clone();
    });

    private static Locale FORMAT_LOCALE;
    private static NumberFormat NUMBER_FORMAT;
    private static NumberFormat INTEGER_FORMAT;
    private static NumberFormat PERCENT_FORMAT;
    public static DecimalFormatSymbols FORMAT_SYMBOLS;
    private static final double SMALLEST_MACHINE_EPSILON = 1.11E-16;
    private static double epsilonDisplayValue;
    private static final String MISSING_DATE = "Missing";
    private static final String MISSING_TIME = "Missing";
    private static int numberOfFractionDigits;
    public static String[] availableTimeZoneNames;
    public static final int SYSTEM_TIME_ZONE = 0;
    public static final String[] TRUE_STRINGS;
    public static final String[] FALSE_STRINGS;

    public static String unescape(String escaped) {
        StringBuilder result = new StringBuilder();

        for(int index = 0; index < escaped.length(); ++index) {
            char c = escaped.charAt(index);
            switch (c) {
                case '\\':
                    if (index < escaped.length() - 1) {
                        ++index;
                        char next = escaped.charAt(index);
                        switch (next) {
                            case '"':
                                result.append('"');
                                break;
                            case '\\':
                                result.append('\\');
                                break;
                            case 'n':
                                result.append('\n');
                                break;
                            default:
                                result.append('\\').append(next);
                        }
                    } else {
                        result.append('\\');
                    }
                    break;
                default:
                    result.append(c);
            }
        }

        return result.toString();
    }

    public static String escape(String unescaped) {
        StringBuilder result = new StringBuilder();
        char[] var2 = unescaped.toCharArray();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            char c = var2[var4];
            switch (c) {
                case '\n':
                    result.append("\\n");
                    break;
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                default:
                    result.append(c);
            }
        }

        return result.toString();
    }

    public static TimeZone getTimeZone(int index) {
        return index == 0 ? TimeZone.getDefault() : TimeZone.getTimeZone(availableTimeZoneNames[index]);
    }

    public static String formatTime(Date date) {
        ((DateFormat)TIME_FORMAT.get()).setTimeZone(getPreferredTimeZone());
        return ((DateFormat)TIME_FORMAT.get()).format(date);
    }

    public static String formatDateTime(Date date) {
        ((DateFormat)DATE_TIME_FORMAT.get()).setTimeZone(getPreferredTimeZone());
        return ((DateFormat)DATE_TIME_FORMAT.get()).format(date);
    }

    public static String formatDateTime(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(getPreferredTimeZone());
        return format.format(date);
    }

    public static int getPreferredTimeZoneIndex() {
        String timeZoneString = null;
        int preferredTimeZone = 0;

        try {
            if (timeZoneString != null) {
                preferredTimeZone = Integer.parseInt(timeZoneString);
            }
        } catch (NumberFormatException var9) {
            int index = 0;
            boolean found = false;
            String[] var5 = availableTimeZoneNames;
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String id = var5[var7];
                if (id.equals(timeZoneString)) {
                    found = true;
                    break;
                }

                ++index;
            }

            if (found) {
                preferredTimeZone = index;
            }
        }

        return preferredTimeZone;
    }

    public static TimeZone getPreferredTimeZone() {
        return getTimeZone(getPreferredTimeZoneIndex());
    }

    public static String formatDate(Date date) {
        ((DateFormat)DATE_FORMAT.get()).setTimeZone(getPreferredTimeZone());
        return ((DateFormat)DATE_FORMAT.get()).format(date);
    }

    public static String formatIntegerIfPossible(double value) {
        return formatIntegerIfPossible(value, numberOfFractionDigits);
    }

    public static String formatIntegerIfPossible(double value, int numberOfDigits) {
        return formatIntegerIfPossible(value, numberOfDigits, false);
    }

    public static String formatIntegerIfPossible(double value, int numberOfDigits, boolean groupingCharacter) {
        if (Double.isNaN(value)) {
            return "?";
        } else if (Double.isInfinite(value)) {
            return value < 0.0 ? "-" + FORMAT_SYMBOLS.getInfinity() : FORMAT_SYMBOLS.getInfinity();
        } else {
            long longValue = Math.round(value);
            if (Math.abs((double)longValue - value) < epsilonDisplayValue) {
                INTEGER_FORMAT.setGroupingUsed(groupingCharacter);
                return INTEGER_FORMAT.format(value);
            } else {
                return formatNumber(value, numberOfDigits, groupingCharacter);
            }
        }
    }

    public static String formatNumber(double value, int numberOfDigits, boolean groupingCharacters) {
        if (Double.isNaN(value)) {
            return "?";
        } else {
            int numberDigits = numberOfDigits;
            if (numberOfDigits < 0) {
                numberDigits = numberOfFractionDigits;
            }

            NUMBER_FORMAT.setMinimumFractionDigits(numberDigits);
            NUMBER_FORMAT.setMaximumFractionDigits(numberDigits);
            NUMBER_FORMAT.setGroupingUsed(groupingCharacters);
            return NUMBER_FORMAT.format(value);
        }
    }

    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }

    static {
        FORMAT_LOCALE = Locale.US;
        INTEGER_FORMAT = NumberFormat.getIntegerInstance(FORMAT_LOCALE);
        PERCENT_FORMAT = NumberFormat.getPercentInstance(FORMAT_LOCALE);
        FORMAT_SYMBOLS = new DecimalFormatSymbols(FORMAT_LOCALE);
//        ALL_RESOURCE_SOURCES = Collections.synchronizedList(new LinkedList());
//        PLUGIN_RESOURCE_SOURCES = Collections.synchronizedMap(new HashMap());
//        ALL_RESOURCE_SOURCES.add(new ResourceSource(adaa.analytics.rules.rm.tools.Tools.class.getClassLoader()));
        int numberDigits = 3;
        numberOfFractionDigits = numberDigits;
        epsilonDisplayValue = Math.min(1.11E-16, 1.0 / Math.pow(10.0, (double)numberOfFractionDigits));
        String[] allTimeZoneNames = TimeZone.getAvailableIDs();
        Arrays.sort(allTimeZoneNames);
        availableTimeZoneNames = new String[allTimeZoneNames.length + 1];
        availableTimeZoneNames[0] = "SYSTEM";
        System.arraycopy(allTimeZoneNames, 0, availableTimeZoneNames, 1, allTimeZoneNames.length);
        TRUE_STRINGS = new String[]{"true", "on", "yes", "y"};
        FALSE_STRINGS = new String[]{"false", "off", "no", "n"};
    }

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

    public static boolean isNotEqual(double d1, double d2) {
        return !isEqual(d1, d2);
    }

    public static boolean isGreater(double d1, double d2) {
        if (!Double.isNaN(d1) && !Double.isNaN(d2)) {
            return Double.compare(d1, d2) > 0;
        } else {
            return false;
        }
    }

    public static boolean isGreaterEqual(double d1, double d2) {
        if (!Double.isNaN(d1) && !Double.isNaN(d2)) {
            return Double.compare(d1, d2) > 0 || isEqual(d1, d2);
        } else {
            return false;
        }
    }

    public static boolean isLess(double d1, double d2) {
        if (!Double.isNaN(d1) && !Double.isNaN(d2)) {
            return Double.compare(d1, d2) < 0;
        } else {
            return false;
        }
    }

    public static boolean isLessEqual(double d1, double d2) {
        if (!Double.isNaN(d1) && !Double.isNaN(d2)) {
            return Double.compare(d1, d2) < 0 || isEqual(d1, d2);
        } else {
            return false;
        }
    }

    public static boolean isEqual(Date d1, Date d2) {
        if (d1 == d2) {
            return true;
        } else if (d1 != null && d2 != null) {
            return d1.compareTo(d2) == 0;
        } else {
            return false;
        }
    }

    public static boolean isNotEqual(Date d1, Date d2) {
        return !isEqual(d1, d2);
    }

    public static boolean isGreater(Date d1, Date d2) {
        if (d1 != null && d2 != null) {
            return d1.compareTo(d2) > 0;
        } else {
            return false;
        }
    }

    public static boolean isGreaterEqual(Date d1, Date d2) {
        return isEqual(d1, d2) || d1 != null && d1.compareTo(d2) > 0;
    }

    public static boolean isLess(Date d1, Date d2) {
        if (d1 != null && d2 != null) {
            return d1.compareTo(d2) < 0;
        } else {
            return false;
        }
    }

    public static boolean isLessEqual(Date d1, Date d2) {
        return isEqual(d1, d2) || d1 != null && d1.compareTo(d2) < 0;
    }

    public static boolean isDefault(double defaultValue, double value) {
        if (Double.isNaN(defaultValue)) {
            return Double.isNaN(value);
        } else {
            return defaultValue == value;
        }
    }

    public static Class<?> classForName(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException var6) {
            try {
                return ClassLoader.getSystemClassLoader().loadClass(className);
            } catch (ClassNotFoundException var5) {
                // TODO Plugin iteration
//                Iterator<Plugin> i = Plugin.getAllPlugins().iterator();
//
//                while(i.hasNext()) {
//                    Plugin p = (Plugin)i.next();
//
//                    try {
//                        return p.getClassLoader().loadClass(className);
//                    } catch (ClassNotFoundException var4) {
//                    }
//                }

                throw new ClassNotFoundException(className);
            }
        }
    }

    public static void isLabelled(IExampleSet es) {
        if (es.getAttributes().getLabel() == null) {
            // TODO throw
//            throw new UserError();
        }
    }

    public static void hasNominalLabels(IExampleSet es, String algorithm) {
        isLabelled(es);
        IAttribute a = es.getAttributes().getLabel();
        if (!Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(), 1)) {
            // TODO throw
//            throw new UserError((Operator)null, 101, new Object[]{algorithm, a.getName()});
        }
    }
}
