package adaa.analytics.rules.data.metadata;

import adaa.analytics.rules.data.IAttribute;

public class AttributeFactory {

    public static IAttribute createAttribute(String name, int valueType) {
       return new ColumnMetaData(name, EColumnType.RmOntologyToEColumnType(valueType));
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
        }

        return cmd;
    }

}
