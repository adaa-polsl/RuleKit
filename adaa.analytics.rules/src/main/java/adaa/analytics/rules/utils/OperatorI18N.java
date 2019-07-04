/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
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
