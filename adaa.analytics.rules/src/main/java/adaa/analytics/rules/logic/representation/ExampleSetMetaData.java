package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.data.metadata.EColumnRole;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;

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

    public IAttribute getAttributeByName(String name) {
        return es.getAttributes().get(name);
    }

    public IAttribute getAttributeByRole(String role) {
        Iterator<IAttribute> iAtts = es.getAttributes().allAttributes();
        while(iAtts.hasNext()) {
            IAttribute att = iAtts.next();
            if(att.getRole().equals(role)) {
                return att;
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


    public IAttribute getSpecial(String role) {
        return getAttributeByRole(role);
    }

    public IAttribute getLabelMetaData() {
        return getSpecial(EColumnRole.label.name());
    }

}
