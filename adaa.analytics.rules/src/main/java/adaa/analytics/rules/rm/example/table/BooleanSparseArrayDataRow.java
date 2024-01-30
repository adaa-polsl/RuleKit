package adaa.analytics.rules.rm.example.table;

public class BooleanSparseArrayDataRow extends AbstractSparseArrayDataRow {
    private static final long serialVersionUID = -4373978993763834478L;
    private boolean[] values;

    public BooleanSparseArrayDataRow() {
        this(0);
    }

    public BooleanSparseArrayDataRow(int size) {
        super(size);
        this.values = new boolean[size];
    }

    protected void swapValues(int a, int b) {
        boolean tt = this.values[a];
        this.values[a] = this.values[b];
        this.values[b] = tt;
    }

    protected void resizeValues(int length) {
        boolean[] d = new boolean[length];
        System.arraycopy(this.values, 0, d, 0, Math.min(this.values.length, length));
        this.values = d;
    }

    protected void removeValue(int index) {
        System.arraycopy(this.values, index + 1, this.values, index, this.values.length - (index + 1));
    }

    protected double getValue(int index) {
        return this.values[index] ? 1.0 : 0.0;
    }

    protected void setValue(int index, double v) {
        this.values[index] = v != 0.0;
    }

    protected double[] getAllValues() {
        double[] result = new double[this.values.length];

        for(int i = 0; i < result.length; ++i) {
            result[i] = this.values[i] ? 1.0 : 0.0;
        }

        return result;
    }

    public int getType() {
        return 13;
    }
}
