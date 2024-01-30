package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.tools.Tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SparseMapDataRow extends DataRow {
    private static final long serialVersionUID = -7452459295368606029L;
    private Map<Integer, Double> data = new ConcurrentHashMap();

    public SparseMapDataRow() {
    }

    protected double get(int index, double defaultValue) {
        Double value = (Double)this.data.get(index);
        return value != null ? value : defaultValue;
    }

    protected void set(int index, double value, double defaultValue) {
        if (Tools.isDefault(defaultValue, value)) {
            this.data.remove(index);
        } else {
            this.data.put(index, value);
        }
    }

    protected void ensureNumberOfColumns(int numberOfColumns) {
    }

    public String toString() {
        return this.data.toString();
    }

    public int getType() {
        return 14;
    }
}
