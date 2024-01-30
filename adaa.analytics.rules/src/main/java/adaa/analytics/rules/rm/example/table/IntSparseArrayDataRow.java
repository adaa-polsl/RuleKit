package adaa.analytics.rules.rm.example.table;

public class IntSparseArrayDataRow extends AbstractSparseArrayDataRow {
    private static final long serialVersionUID = -4642005554786017231L;
    private int[] values;

    public IntSparseArrayDataRow() {
        this(0);
    }

    public IntSparseArrayDataRow(int size) {
        super(size);
        this.values = new int[size];
    }

    protected void swapValues(int a, int b) {
        int tt = this.values[a];
        this.values[a] = this.values[b];
        this.values[b] = tt;
    }

    protected void resizeValues(int length) {
        int[] d = new int[length];
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
        this.values[index] = (int)v;
    }

    protected double[] getAllValues() {
        double[] result = new double[this.values.length];

        for(int i = 0; i < result.length; ++i) {
            result[i] = (double)this.values[i];
        }

        return result;
    }

    public int getType() {
        return 10;
    }
}
