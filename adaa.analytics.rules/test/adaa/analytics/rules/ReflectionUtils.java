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
package adaa.analytics.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReflectionUtils {

    private static final Logger LOG = Logger.getLogger(ReflectionUtils.class.getName());

    private ReflectionUtils() {}

    /**
     * Zwraca liste pol podanego obiektu, ktorych nazwy rozpoczynaja sie od podanych prefiksow.
     * @param object
     * @param prefixes
     * @return
     */
    public static List<Field> getFieldsStartingWith(Object object, List<String> prefixes) {

        ArrayList<Field> fields = new ArrayList<Field>();

        Class<?> clazz = object.getClass();
        while (clazz != null) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                for (String prefix : prefixes) {
                    if (field.getName().startsWith(prefix)) {
                        fields.add(field);
                        continue;
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    /**
     * Zwraca wartosc pola dla podanego obiektu.
     * @param object
     * @param fieldName
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <E> E getFieldValue(Object object, String fieldName) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (E) field.get(object);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOG.log(Level.WARNING, "Unable to get value of " + fieldName, e);
                return null;
            }
        }

        return null;
    }

    /**
     * Ustawia wartosc pola podanego obiektu.
     * @param object
     * @param field
     * @param fieldValue
     * @return
     */
    public static boolean setFieldValue(Object object, Field field, Object fieldValue) {
        try {
            field.setAccessible(true);
            field.set(object, fieldValue);
            return true;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOG.log(Level.WARNING, "Unable to set value of " + field.getName(), e);
            return false;
        }
    }

    /**
     * Ustawia wartosc pola podanego obiektu.
     * @param object
     * @param fieldName
     * @param fieldValue
     * @return
     */
    public static boolean setFieldValue(Object object, String fieldName, Object fieldValue) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, fieldValue);
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOG.log(Level.WARNING, "Unable to set value of " + fieldName, e);
                return false;
            }
        }

        return false;
    }

    /**
     * Wywoluje bezparametryczna metode statyczna dla podanej klasy.
     * @param clazz
     * @param methodName
     * @return Zwraca <code>true</code> jezeli udalo sie wywolac metode.
     */
    public static boolean callMethod(Class<?> clazz, String methodName) {
        return callMethod(clazz, methodName, new Class[] { }, new Object[] { });
    }

    /**
     * Wywoluje dla podanej klasy podana metoda statyczna z parametrami.
     * @param clazz Typ
     * @param methodName Nazwa metody
     * @param arguments Typy argumentow
     * @param argumentValues Wartosci argumentow
     * @return Zwraca <code>true</code> jezeli udalo sie wywolac metode.
     */
    public static boolean callMethod(
    		Class<?> clazz, String methodName, Class<?>[] arguments, Object[] argumentValues) {
    	try {
            Method initMethod = clazz.getMethod(methodName, arguments);
            initMethod.invoke(null, argumentValues);
            return true;
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Unable to invoke " + methodName + " for " + clazz.getName(), e);
            return false;
        }
    }
}
