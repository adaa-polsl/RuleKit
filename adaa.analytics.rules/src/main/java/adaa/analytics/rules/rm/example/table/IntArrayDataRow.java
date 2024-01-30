package adaa.analytics.rules.rm.example.table;

public class IntArrayDataRow extends DataRow {
    private static final long serialVersionUID = -8089560930865510003L;
    private int[] data;

    public IntArrayDataRow(int[] data) {
        this.data = data;
    }

    protected double get(int index, double defaultValue) {
        return (double)this.data[index];
    }

    protected synchronized void set(int index, double value, double defaultValue) {
        this.data[index] = (int)value;
    }

    protected synchronized void ensureNumberOfColumns(int numberOfColumns) {
        if (this.data.length < numberOfColumns) {
            int[] newData = new int[numberOfColumns];
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
        return 3;
    }
}
