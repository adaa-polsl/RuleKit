package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.*;
import adaa.analytics.rules.rm.tools.Ontology;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractAttribute implements IAttribute {
    private static final long serialVersionUID = -9167755945651618227L;
    private transient List<IAttributes> owners = new LinkedList();
    private AttributeDescription attributeDescription;
    private final List<IAttributeTransformation> transformations = new ArrayList();
    private List<IStatistics> statistics = new LinkedList();
    private String constructionDescription = null;
//    private Annotations annotations = new Annotations();

    protected AbstractAttribute(AbstractAttribute attribute) {
        this.attributeDescription = attribute.attributeDescription;
        this.statistics = new LinkedList();
        Iterator var2 = attribute.statistics.iterator();

        while(var2.hasNext()) {
            IStatistics statistics = (IStatistics)var2.next();
            this.statistics.add((IStatistics)statistics.clone());
        }

        int counter = 0;

        for(Iterator var6 = attribute.transformations.iterator(); var6.hasNext(); ++counter) {
            IAttributeTransformation transformation = (IAttributeTransformation)var6.next();
            if (counter < attribute.transformations.size() - 1) {
                this.addTransformation(transformation);
            } else {
                this.addTransformation((IAttributeTransformation)transformation.clone());
            }
        }

        this.constructionDescription = attribute.constructionDescription;
//        this.annotations.putAll(attribute.getAnnotations());
    }

    protected AbstractAttribute(String name, int valueType) {
        this.attributeDescription = new AttributeDescription(this, name, valueType, 1, 0.0, -1);
        this.constructionDescription = name;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.owners == null) {
            this.owners = new LinkedList();
        }

//        if (this.annotations == null) {
//            this.annotations = new Annotations();
//        }

    }

    public void addOwner(IAttributes attributes) {
        this.owners.add(attributes);
    }

    public void removeOwner(IAttributes attributes) {
        this.owners.remove(attributes);
    }

    public abstract Object clone();

    public boolean equals(Object o) {
        if (!(o instanceof AbstractAttribute)) {
            return false;
        } else {
            AbstractAttribute a = (AbstractAttribute)o;
            return this.attributeDescription.equals(a.attributeDescription);
        }
    }

    public int hashCode() {
        return this.attributeDescription.hashCode();
    }

    public void addTransformation(IAttributeTransformation transformation) {
        this.transformations.add(transformation);
    }

    public void clearTransformations() {
        this.transformations.clear();
    }

    public IAttributeTransformation getLastTransformation() {
        int size = this.transformations.size();
        return size > 0 ? (IAttributeTransformation)this.transformations.get(size - 1) : null;
    }

    public double getValue(DataRow row) {
        double result = row.get(this.getTableIndex(), this.getDefault());
        IAttributeTransformation transformation;
        if (!this.transformations.isEmpty()) {
            for(Iterator var4 = this.transformations.iterator(); var4.hasNext(); result = transformation.transform(this, result)) {
                transformation = (IAttributeTransformation)var4.next();
            }
        }

        return result;
    }

    public void setValue(DataRow row, double value) {
        double newValue = value;

        IAttributeTransformation transformation;
        for(Iterator var6 = this.transformations.iterator(); var6.hasNext(); newValue = transformation.inverseTransform(this, newValue)) {
            transformation = (IAttributeTransformation)var6.next();
            if (!transformation.isReversable()) {
                throw new RuntimeException("Cannot set value for attribute using irreversible transformations. This process will probably work if you deactivate create_view in preprocessing operators.");
            }
        }

        row.set(this.getTableIndex(), newValue, this.getDefault());
    }

    public String getName() {
        return this.attributeDescription.getName();
    }

    public void setName(String v) {
        if (!v.equals(this.attributeDescription.getName())) {
            Iterator var2 = this.owners.iterator();

            while(var2.hasNext()) {
                IAttributes attributes = (IAttributes)var2.next();
                attributes.rename(this, v);
            }

            this.attributeDescription = (AttributeDescription)this.attributeDescription.clone();
            this.attributeDescription.setName(v);
        }
    }

    public int getTableIndex() {
        return this.attributeDescription.getTableIndex();
    }

    public void setTableIndex(int i) {
        this.attributeDescription = (AttributeDescription)this.attributeDescription.clone();
        this.attributeDescription.setTableIndex(i);
    }

    public int getBlockType() {
        return this.attributeDescription.getBlockType();
    }

    public void setBlockType(int b) {
        this.attributeDescription = (AttributeDescription)this.attributeDescription.clone();
        this.attributeDescription.setBlockType(b);
    }

    public int getValueType() {
        return this.attributeDescription.getValueType();
    }

    public Iterator<IStatistics> getAllStatistics() {
        return this.statistics.iterator();
    }

    public void registerStatistics(IStatistics statistics) {
        this.statistics.add(statistics);
    }

    /** @deprecated */
    @Deprecated
    public double getStatistics(String name) {
        return this.getStatistics(name, (String)null);
    }

    /** @deprecated */
    @Deprecated
    public double getStatistics(String name, String parameter) {
        Iterator var3 = this.statistics.iterator();

        IStatistics statistics;
        do {
            if (!var3.hasNext()) {
                throw new RuntimeException("No statistics object was available for attribute statistics '" + name + "'!");
            }

            statistics = (IStatistics)var3.next();
        } while(!statistics.handleStatistics(name));

        return statistics.getStatistics(this, name, parameter);
    }

    public String getConstruction() {
        return this.constructionDescription;
    }

    public void setConstruction(String description) {
        this.constructionDescription = description;
    }

    public void setDefault(double value) {
        this.attributeDescription = (AttributeDescription)this.attributeDescription.clone();
        this.attributeDescription.setDefault(value);
    }

    public double getDefault() {
        return this.attributeDescription.getDefault();
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("#");
        result.append(this.attributeDescription.getTableIndex());
        result.append(": ");
        result.append(this.attributeDescription.getName());
        result.append(" (");
        result.append(Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(this.attributeDescription.getValueType()));
        result.append("/");
        result.append(Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(this.attributeDescription.getBlockType()));
        result.append(")");
        return result.toString();
    }

//    public Annotations getAnnotations() {
//        return this.annotations;
//    }
}
