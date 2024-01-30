package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.IAttribute;

import java.io.Serializable;

public abstract class DataRow implements Serializable {
    private static final long serialVersionUID = -3482048832637144523L;

    public DataRow() {
    }

    protected abstract double get(int var1, double var2);

    protected abstract void set(int var1, double var2, double var4);

    protected abstract void ensureNumberOfColumns(int var1);

    public void trim() {
    }

    public abstract int getType();

    public abstract String toString();

    public double get(IAttribute attribute) {
        if (attribute == null) {
            return Double.NaN;
        } else {
            try {
                return attribute.getValue(this);
            } catch (ArrayIndexOutOfBoundsException var3) {
                throw new ArrayIndexOutOfBoundsException("DataRow: table index " + attribute.getTableIndex() + " of Attribute " + attribute.getName() + " is out of bounds.");
            }
        }
    }

    public void set(IAttribute attribute, double value) {
        attribute.setValue(this, value);
    }
}
