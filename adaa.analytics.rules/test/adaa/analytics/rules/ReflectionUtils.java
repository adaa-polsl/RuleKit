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
