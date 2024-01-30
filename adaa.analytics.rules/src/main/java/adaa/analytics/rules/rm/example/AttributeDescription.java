package adaa.analytics.rules.rm.example;

import java.io.Serializable;

public class AttributeDescription implements Serializable, Cloneable {
    private static final long serialVersionUID = 8641898727515830321L;
    private String name;
    private int valueType = 0;
    private int blockType = 0;
    private double defaultValue = 0.0;
    private int index = -1;

    public AttributeDescription(IAttribute attribute, String name, int valueType, int blockType, double defaultValue, int tableIndex) {
        this.name = name;
        this.valueType = valueType;
        this.blockType = blockType;
        this.defaultValue = defaultValue;
        this.index = tableIndex;
    }

    private AttributeDescription(AttributeDescription other) {
        this.name = other.name;
        this.valueType = other.valueType;
        this.blockType = other.blockType;
        this.defaultValue = other.defaultValue;
        this.index = other.index;
    }

    public Object clone() {
        return new AttributeDescription(this);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public int getValueType() {
        return this.valueType;
    }

    public int getBlockType() {
        return this.blockType;
    }

    public void setBlockType(int b) {
        this.blockType = b;
    }

    public double getDefault() {
        return this.defaultValue;
    }

    public void setDefault(double defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getTableIndex() {
        return this.index;
    }

    public void setTableIndex(int i) {
        this.index = i;
    }

    public boolean equals(Object o) {
        if (!(o instanceof AttributeDescription)) {
            return false;
        } else {
            AttributeDescription a = (AttributeDescription)o;
            if (this.index != a.getTableIndex()) {
                return false;
            } else {
                return this.name.equals(a.getName());
            }
        }
    }

    public int hashCode() {
        return this.name.hashCode() ^ this.index;
    }
}
