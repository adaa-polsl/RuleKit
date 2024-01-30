package adaa.analytics.rules.rm.example.table;

public class LongArrayDataRow extends DataRow {
    private static final long serialVersionUID = 8652466671294511853L;
    private long[] data;

    public LongArrayDataRow(long[] data) {
        this.data = data;
    }

    protected double get(int index, double defaultValue) {
        return (double)this.data[index];
    }

    protected synchronized void set(int index, double value, double defaultValue) {
        this.data[index] = (long)value;
    }

    protected synchronized void ensureNumberOfColumns(int numberOfColumns) {
        if (this.data.length < numberOfColumns) {
            long[] newData = new long[numberOfColumns];
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
        return 2;
    }
}
