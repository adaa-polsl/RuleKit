package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.data.ColumnMetaData;
import adaa.analytics.rules.data.EColumnRole;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.*;


/**
 * This class stores detailed meta data information about ExampleSets.
 *
 */
public class ExampleSetMetaData {

//    private Map<String, AttributeMetaData> attributeMetaData = new LinkedHashMap<String, AttributeMetaData>();

    private IExampleSet es;

    public ExampleSetMetaData(IExampleSet exampleSet) {
        es = exampleSet;
    }

    public ColumnMetaData getAttributeByName(String name) {
        return es.getAttributes().get(name).getColumnMetaData();
    }

    public ColumnMetaData getAttributeByRole(String role) {
        Iterator<IAttribute> iAtts = es.getAttributes().allAttributes();
        while(iAtts.hasNext()) {
            IAttribute att = iAtts.next();
            if(att.getColumnMetaData().getRole().equals(role)) {
                return att.getColumnMetaData();
            }
        }
        return null;
    }

//    public void addAttribute(ColumnMetaData attribute) {
//        if (attributeMetaData == null) {
//            attributeMetaData = new LinkedHashMap<String, AttributeMetaData>();
//        }
//        // registering this exampleSetmetaData as owner of the attribute.
//        attribute = attribute.registerOwner(this);
//        attributeMetaData.put(attribute.getName(), attribute);
//    }


    public ColumnMetaData getSpecial(String role) {
        return getAttributeByRole(role);
    }

    public ColumnMetaData getLabelMetaData() {
        return getSpecial(EColumnRole.label.name());
    }

}
