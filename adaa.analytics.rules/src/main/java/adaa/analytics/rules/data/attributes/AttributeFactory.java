package adaa.analytics.rules.data.attributes;

import adaa.analytics.rules.data.metadata.ColumnMetaData;
import adaa.analytics.rules.data.metadata.Converter;
import adaa.analytics.rules.data.metadata.EColumnRole;
import adaa.analytics.rules.data.IAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AttributeFactory {
    private static Map<String, AtomicInteger> nameCounters = new HashMap();

    public AttributeFactory() {
    }

    public static IAttribute createAttribute(String name, int valueType) {
        String attributeName = name != null ? new String(name) : createName();

        return new ColumnMetaData(attributeName, Converter.RmOntologyToEColumnType(valueType));
    }


    public static IAttribute createAttribute(IAttribute attribute) {
        return createAttribute(attribute, (String)null);
    }

    public static IAttribute createAttribute(IAttribute attribute, String functionName) {
        IAttribute cmd = (IAttribute) attribute.clone();
        cmd.setRole(EColumnRole.regular.name());
        if (functionName == null) {
            cmd.setName(attribute.getName());
        } else {
            cmd.setName(functionName + "(" + attribute.getName() + ")");
//            result.setConstruction(functionName + "(" + attribute.getName() + ")");
        }

        return cmd;
    }


    public static void resetNameCounters() {
        nameCounters.clear();
    }

    public static String createName() {
        return createName("gensym");
    }

    public static String createName(String prefix) {
        AtomicInteger counter = (AtomicInteger)nameCounters.get(prefix);
        if (counter == null) {
            nameCounters.put(prefix, new AtomicInteger(1));
            return prefix;
        } else {
            return prefix + counter.getAndIncrement();
        }
    }

    static {
        resetNameCounters();
    }
}
