package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.*;

import adaa.analytics.rules.rm.example.table.DataRow;
import adaa.analytics.rules.rm.example.table.IExampleTable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleExampleSet extends AbstractExampleSet {
    private static final long serialVersionUID = 9163340881176421801L;
    private IExampleTable exampleTable;
    private IAttributes attributes;

    public SimpleExampleSet(IExampleTable exampleTable) {
        this(exampleTable, (List)null, (Map)null);
    }

    public SimpleExampleSet(IExampleTable exampleTable, List<IAttribute> regularAttributes) {
        this(exampleTable, regularAttributes, (Map)null);
    }

    public SimpleExampleSet(IExampleTable exampleTable, Map<IAttribute, String> specialAttributes) {
        this(exampleTable, (List)null, specialAttributes);
    }

    public SimpleExampleSet(IExampleTable exampleTable, List<IAttribute> regularAttributes, Map<IAttribute, String> specialAttributes) {
        this.attributes = new SimpleAttributes();
        this.exampleTable = exampleTable;
        List<IAttribute> regularList = regularAttributes;
        IAttribute attribute;
        if (regularAttributes == null) {
            regularList = new LinkedList();

            for(int a = 0; a < exampleTable.getNumberOfAttributes(); ++a) {
                attribute = exampleTable.getAttribute(a);
                if (attribute != null) {
                    ((List)regularList).add(attribute);
                }
            }
        }

        Iterator s = ((List)regularList).iterator();

        while(true) {
            do {
                if (!s.hasNext()) {
                    if (specialAttributes != null) {
                        s = specialAttributes.entrySet().iterator();

                        while(s.hasNext()) {
                            Map.Entry<IAttribute, String> entry = (Map.Entry)s.next();
                            this.getAttributes().setSpecialAttribute((IAttribute)((IAttribute)entry.getKey()).clone(), (String)entry.getValue());
                        }
                    }

                    return;
                }

                attribute = (IAttribute)s.next();
            } while(specialAttributes != null && specialAttributes.get(attribute) != null);

            this.getAttributes().add(new AttributeRole((IAttribute)attribute.clone()));
        }
    }

    public SimpleExampleSet(SimpleExampleSet exampleSet) {
        this.attributes = new SimpleAttributes();
//        this.cloneAnnotationsFrom(exampleSet);
        this.exampleTable = exampleSet.exampleTable;
        this.attributes = (IAttributes)exampleSet.getAttributes().clone();
    }

    public IAttributes getAttributes() {
        return this.attributes;
    }

    public IExampleTable getExampleTable() {
        return this.exampleTable;
    }

    public int size() {
        return this.exampleTable.size();
    }

    public Example getExample(int index) {
        DataRow dataRow = this.getExampleTable().getDataRow(index);
        return dataRow == null ? null : new Example(dataRow, this);
    }

    public Iterator<Example> iterator() {
        return new SimpleExampleReader(this.getExampleTable().getDataRowReader(), this);
    }

    // @TODO Czy cleanup jest potrzebny
//    public void cleanup() {
//        if (this.exampleTable instanceof ColumnarExampleTable) {
//            ColumnarExampleTable table = (ColumnarExampleTable)this.exampleTable;
//            this.exampleTable = table.columnCleanupClone(this.attributes);
//        }
//
//    }

    public boolean isThreadSafeView() {
        return true;
    }
}
