package adaa.analytics.rules.utils;

import com.rapidminer.operator.Operator;
import com.rapidminer.tools.I18N;

/**
 * Metody wspierajace lokalizacje operatorow.
 *
 * @author Lukasz Wrobel
 *
 */
public final class OperatorI18NUtils {

    public static final String I18N_KEY_PARAMETER_NAME_SUFFIX = "name";

    public static final String I18N_KEY_PARAMETER_DESCRIPTION_SUFFIX = "info";

    private OperatorI18NUtils() {
    }

    /**
     * Zwraca zlokalizowana nazwe parametru lub parameterKey jezeli niedostepna.
     * @param operator
     * @param parameterKey
     * @return
     */
    public static String getParameterName(Operator operator, String parameterKey) {
        String t = getTranslationOrNull(operator, parameterKey, I18N_KEY_PARAMETER_NAME_SUFFIX);
        return t == null ? parameterKey : t;
    }

    /**
     * Zwraca zlokalizowana nazwe parametru lub null jezeli niedostepna.
     * @param operator
     * @param parameterKey
     * @return
     */
    public static String getParameterNameOrNull(Operator operator, String parameterKey) {
        return getTranslationOrNull(operator, parameterKey, I18N_KEY_PARAMETER_NAME_SUFFIX);
    }

    /**
     * Zwraca zlokalizowany opis parametru lub parameterKey jezeli niedostepny.
     *
     * @param operator
     * @param parameterKey
     * @return
     */
    public static String getParameterDescription(Operator operator, String parameterKey) {
        String t = getTranslationOrNull(operator, parameterKey, I18N_KEY_PARAMETER_DESCRIPTION_SUFFIX);
        return t == null ? parameterKey : t;
    }

    public static String getParameterDescriptionOrNull(Operator operator, String parameterKey) {
        return getTranslationOrNull(operator, parameterKey, I18N_KEY_PARAMETER_DESCRIPTION_SUFFIX);
    }

    private static String getTranslationOrNull(
            Operator operator, String parameterKey, String i18nKeySuffix) {

//        OperatorDescription operatorDescription = operator.getOperatorDescription();
//
//        // w sklad nazwy klucza i18n wchodzi klucz nazwy operatora np.fast_csv_reader
//        String operatorKey = operatorDescription.getKey();
//
//        Plugin provider = operatorDescription.getProvider();
//        if (provider != null) {
//            // jezeli operator jest wczytany z pluginu to klucz nawy jest poprzedzany namespace
//            // usuwamy namespace z nazwy
//            String providerNamespace = provider.getPrefix() + ":";
//            if (operatorKey.startsWith(providerNamespace)) {
//                operatorKey = operatorKey.substring(providerNamespace.length());
//            }
//        }

        String completeKey =
            operator.getClass().getCanonicalName() + "." +
            parameterKey + "." +
            i18nKeySuffix;

        return I18N.getMessageOrNull(I18N.getGUIBundle(), completeKey);
    }
}
