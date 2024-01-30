package adaa.analytics.rules.rm.example.table;

public class ShortSparseArrayDataRow extends AbstractSparseArrayDataRow {
    private static final long serialVersionUID = 1688504268820756726L;
    private short[] values;

    public ShortSparseArrayDataRow() {
        this(0);
    }

    public ShortSparseArrayDataRow(int size) {
        super(size);
        this.values = new short[size];
    }

    protected void swapValues(int a, int b) {
        short tt = this.values[a];
        this.values[a] = this.values[b];
        this.values[b] = tt;
    }

    protected void resizeValues(int length) {
        short[] d = new short[length];
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
        this.values[index] = (short)((int)v);
    }

    protected double[] getAllValues() {
        double[] result = new double[this.values.length];

        for(int i = 0; i < result.length; ++i) {
            result[i] = (double)this.values[i];
        }

        return result;
    }

    public int getType() {
        return 11;
    }
}
