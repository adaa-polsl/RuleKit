package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.AttributeRole;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.SimpleExampleSet;
import adaa.analytics.rules.rm.operator.OperatorException;
import adaa.analytics.rules.rm.tools.Tools;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

public abstract class AbstractExampleTable implements IExampleTable {
    private static final long serialVersionUID = -6996954528182122684L;
    private List<IAttribute> attributes = new ArrayList();
    private List<Integer> unusedColumnList = new LinkedList();

    public AbstractExampleTable(List<IAttribute> attributes) {
        this.addAttributes(attributes);
    }

    protected AbstractExampleTable(AbstractExampleTable other) {
        this.attributes = new ArrayList(other.attributes);
        this.unusedColumnList = new LinkedList(other.unusedColumnList);
    }

    public IAttribute[] getAttributes() {
        IAttribute[] attribute = new IAttribute[this.attributes.size()];
        this.attributes.toArray(attribute);
        return attribute;
    }

    public IAttribute getAttribute(int i) {
        return (IAttribute)this.attributes.get(i);
    }


    public void addAttributes(Collection<IAttribute> newAttributes) {
        Iterator var2 = newAttributes.iterator();

        while(var2.hasNext()) {
            IAttribute att = (IAttribute)var2.next();
            this.addAttribute(att);
        }

    }

    public int addAttribute(IAttribute a) {
        if (a == null) {
            throw new IllegalArgumentException("Attribute must not be null");
        } else {
//            int index = true;
            IAttribute original = a;
            a = (IAttribute)a.clone();
            int index;
            if (this.unusedColumnList.size() > 0) {
                synchronized(this.unusedColumnList) {
                    if (this.unusedColumnList.size() > 0) {
                        index = (Integer)this.unusedColumnList.remove(0);
                        this.attributes.set(index, a);
                    } else {
                        index = this.attributes.size();
                        this.attributes.add(a);
                    }
                }
            } else {
                index = this.attributes.size();
                this.attributes.add(a);
            }

            a.setTableIndex(index);
            original.setTableIndex(index);
            return index;
        }
    }

    public void removeAttribute(IAttribute attribute) {
        this.removeAttribute(attribute.getTableIndex());
    }

    public synchronized void removeAttribute(int index) {
        IAttribute a = (IAttribute)this.attributes.get(index);
        if (a != null) {
            this.attributes.set(index, null);
            this.unusedColumnList.add(index);
        }
    }

    public int getNumberOfAttributes() {
        return this.attributes.size();
    }


    public IExampleSet createExampleSet(IAttribute labelAttribute, IAttribute weightAttribute, IAttribute idAttribute) {
        Map<IAttribute, String> specialAttributes = new LinkedHashMap();
        if (labelAttribute != null) {
            specialAttributes.put(labelAttribute, "label");
        }

        if (weightAttribute != null) {
            specialAttributes.put(weightAttribute, "weight");
        }

        if (idAttribute != null) {
            specialAttributes.put(idAttribute, "id");
        }

        return new SimpleExampleSet(this, specialAttributes);
    }

    public IExampleSet createExampleSet(Map<IAttribute, String> specialAttributes) {
        return new SimpleExampleSet(this, specialAttributes);
    }

    public String toString() {
        return "ExampleTable, " + this.attributes.size() + " attributes, " + this.size() + " data rows," + Tools.getLineSeparator() + "attributes: " + this.attributes;
    }

}
