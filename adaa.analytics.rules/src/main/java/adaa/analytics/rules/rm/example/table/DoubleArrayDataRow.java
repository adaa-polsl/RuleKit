package adaa.analytics.rules.rm.example.table;

public class DoubleArrayDataRow extends DataRow {
    private static final long serialVersionUID = -6335785895337884919L;
    private double[] data;

    public DoubleArrayDataRow(double[] data) {
        this.data = data;
    }

    public DoubleArrayDataRow(DoubleArrayDataRow dataRow) {
        this.data = dataRow.getData();
    }

    public double[] getData() {
        return this.data;
    }

    protected double get(int index, double defaultValue) {
        return this.data[index];
    }

    protected synchronized void set(int index, double value, double defaultValue) {
        this.data[index] = value;
    }

    protected synchronized void ensureNumberOfColumns(int numberOfColumns) {
        if (this.data.length < numberOfColumns) {
            double[] newData = new double[numberOfColumns];
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
        return 0;
    }
}
