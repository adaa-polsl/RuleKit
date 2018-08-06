package adaa.analytics.rules.utils;

import com.rapidminer.operator.Operator;

/**
 * Interfejs dodajacy mozliwosc lokalizacji opisow operatorow.
 * NOTE: wykorzystuje mechanizm metod domyslnych (dostepny w JAVA 8 i pozniejszych).
 *
 * @author Lukasz Wrobel
 */
public interface OperatorI18N {

    /**
     * Zlokalizowany opis parametru.
     * @param parameterKey Klucz parametru
     * @return
     */
    public default String getParameterDescription(String parameterKey) {
        if (this instanceof Operator) {
            return OperatorI18NUtils.getParameterDescription((Operator)this, parameterKey);
        }

        return parameterKey;
    }
    
    public default String getParameterName(String parameterKey) {
        if (this instanceof Operator) {
            return OperatorI18NUtils.getParameterName((Operator)this, parameterKey);
        }
        return parameterKey;
    }
}
