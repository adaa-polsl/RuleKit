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
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;

public final class RapidMiner5 {

    public static <T extends Operator> T createOperator(Class<T> clazz) throws OperatorCreationException {

        OperatorDescription description = new OperatorDescription(
                "", "", clazz, null, "", null);

        try {
            java.lang.reflect.Constructor<? extends Operator> constructor =
                    clazz.getConstructor(new Class[] { OperatorDescription.class });
            return (T) constructor.newInstance(new Object[] { description });
        } catch (InstantiationException e) {
            throw new OperatorCreationException(
                    OperatorCreationException.INSTANTIATION_ERROR, clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new OperatorCreationException(
                    OperatorCreationException.ILLEGAL_ACCESS_ERROR, clazz.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new OperatorCreationException(
                    OperatorCreationException.NO_CONSTRUCTOR_ERROR, clazz.getName(), e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new OperatorCreationException(
                    OperatorCreationException.CONSTRUCTION_ERROR, clazz.getName(), e);
        } catch (Throwable t) {
            throw new OperatorCreationException(
                    OperatorCreationException.INSTANTIATION_ERROR, clazz.getName(), t);
        }
    }
}
