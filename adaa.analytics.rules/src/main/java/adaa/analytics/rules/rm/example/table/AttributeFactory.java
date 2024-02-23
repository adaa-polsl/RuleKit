package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.tools.Ontology;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AttributeFactory {
    private static Map<String, AtomicInteger> nameCounters = new HashMap();

    public AttributeFactory() {
    }

    public static IAttribute createAttribute(String name, int valueType) {
        String attributeName = name != null ? new String(name) : createName();
        if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, 9)) {
            return new DateAttribute(attributeName, valueType);
        } else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, 6)) {
            return new BinominalAttribute(attributeName);
        } else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, 1)) {
            return new PolynominalAttribute(attributeName, valueType);
        } else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, 2)) {
            return new NumericalAttribute(attributeName, valueType);
        } else {
            throw new RuntimeException("AttributeFactory: cannot create attribute with value type '" + Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(valueType) + "' (" + valueType + ")!");
        }
    }


    public static IAttribute createAttribute(IAttribute attribute) {
        return createAttribute(attribute, (String)null);
    }

    public static IAttribute createAttribute(IAttribute attribute, String functionName) {
        IAttribute result = (IAttribute)attribute.clone();
        if (functionName == null) {
            result.setName(attribute.getName());
        } else {
            result.setName(functionName + "(" + attribute.getName() + ")");
            result.setConstruction(functionName + "(" + attribute.getName() + ")");
        }

        return result;
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
