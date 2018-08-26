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
