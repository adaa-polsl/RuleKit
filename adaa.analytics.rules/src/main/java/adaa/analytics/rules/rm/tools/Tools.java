package adaa.analytics.rules.rm.tools;

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
    public static final String RESOURCE_PREFIX = "com/rapidminer/resources/";
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
//        String timeZoneString = ParameterService.getParameterValue("rapidminer.general.timezone");
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
//        ALL_RESOURCE_SOURCES.add(new ResourceSource(com.rapidminer.tools.Tools.class.getClassLoader()));
        int numberDigits = 3;
//
//        try {
//            String numberDigitsString = ParameterService.getParameterValue("rapidminer.general.fractiondigits.numbers");
//            numberDigits = Integer.parseInt(numberDigitsString);
//        } catch (NumberFormatException var2) {
//        }

        numberOfFractionDigits = numberDigits;
        epsilonDisplayValue = Math.min(1.11E-16, 1.0 / Math.pow(10.0, (double)numberOfFractionDigits));
//        NUMBER_FORMAT = new DecimalFormat(getDecimalFormatPattern(numberDigits), DecimalFormatSymbols.getInstance(FORMAT_LOCALE));
//        ParameterService.registerParameterChangeListener(new ParameterChangeListener() {
//            public void informParameterSaved() {
//            }
//
//            public void informParameterChanged(String key, String value) {
//                if ("rapidminer.general.fractiondigits.numbers".equals(key)) {
//                    int numberDigits = 3;
//
//                    try {
//                        String numberDigitsString = ParameterService.getParameterValue("rapidminer.general.fractiondigits.numbers");
//                        numberDigits = Integer.parseInt(numberDigitsString);
//                    } catch (NumberFormatException var5) {
//                    }
//
//                    com.rapidminer.tools.Tools.numberOfFractionDigits = numberDigits;
//                    com.rapidminer.tools.Tools.epsilonDisplayValue = Math.min(1.11E-16, 1.0 / Math.pow(10.0, (double) com.rapidminer.tools.Tools.numberOfFractionDigits));
//                    com.rapidminer.tools.Tools.NUMBER_FORMAT = new DecimalFormat(com.rapidminer.tools.Tools.getDecimalFormatPattern(numberDigits), DecimalFormatSymbols.getInstance(com.rapidminer.tools.Tools.FORMAT_LOCALE));
//                }
//
//            }
//        });
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

    public static boolean isDefault(double defaultValue, double value) {
        if (Double.isNaN(defaultValue)) {
            return Double.isNaN(value);
        } else {
            return defaultValue == value;
        }
    }
}
