package adaa.analytics.rules.rm.example.table;

public class BooleanArrayDataRow extends DataRow {
    private static final long serialVersionUID = -432265332489304646L;
    private boolean[] data;

    public BooleanArrayDataRow(boolean[] data) {
        this.data = data;
    }

    protected double get(int index, double defaultValue) {
        return this.data[index] ? 1.0 : 0.0;
    }

    protected synchronized void set(int index, double value, double defaultValue) {
        this.data[index] = value != 0.0;
    }

    protected synchronized void ensureNumberOfColumns(int numberOfColumns) {
        if (this.data.length < numberOfColumns) {
            boolean[] newData = new boolean[numberOfColumns];
            System.arraycopy(this.data, 0, newData, 0, this.data.length);
            this.data = newData;
        }
    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < this.data.length; ++i) {
            result.append((i == 0 ? "" : ",") + this.data[i]);
        }

        return result.toString();
    }

    public int getType() {
        return 6;
    }
}
