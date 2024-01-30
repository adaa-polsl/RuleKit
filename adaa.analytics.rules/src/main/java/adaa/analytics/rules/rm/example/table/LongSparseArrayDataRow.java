package adaa.analytics.rules.rm.example.table;

public class LongSparseArrayDataRow extends AbstractSparseArrayDataRow {
    private static final long serialVersionUID = 7128381338958693751L;
    private long[] values;

    public LongSparseArrayDataRow() {
        this(0);
    }

    public LongSparseArrayDataRow(int size) {
        super(size);
        this.values = new long[size];
    }

    protected void swapValues(int a, int b) {
        long tt = this.values[a];
        this.values[a] = this.values[b];
        this.values[b] = tt;
    }

    protected void resizeValues(int length) {
        long[] d = new long[length];
        System.arraycopy(this.values, 0, d, 0, Math.min(this.values.length, length));
        this.values = d;
    }

    protected void removeValue(int index) {
        System.arraycopy(this.values, index + 1, this.values, index, this.values.length - (index + 1));
    }

    protected double getValue(int index) {
        return (double)this.values[index];
    }

    protected void setValue(int index, double v) {
        this.values[index] = (long)v;
    }

    protected double[] getAllValues() {
        double[] result = new double[this.values.length];

        for(int i = 0; i < result.length; ++i) {
            result[i] = (double)this.values[i];
        }

        return result;
    }

    public int getType() {
        return 9;
    }
}
