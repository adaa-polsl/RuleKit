package adaa.analytics.rules.rm.example.table;

public class DoubleSparseArrayDataRow extends AbstractSparseArrayDataRow {
    private static final long serialVersionUID = 9137639592169696234L;
    private double[] values;

    public DoubleSparseArrayDataRow() {
        this(0);
    }

    public DoubleSparseArrayDataRow(int size) {
        super(size);
        this.values = new double[size];
    }

    protected void swapValues(int a, int b) {
        double tt = this.values[a];
        this.values[a] = this.values[b];
        this.values[b] = tt;
    }

    protected void resizeValues(int length) {
        double[] d = new double[length];
        System.arraycopy(this.values, 0, d, 0, Math.min(this.values.length, length));
        this.values = d;
    }

    protected void removeValue(int index) {
        System.arraycopy(this.values, index + 1, this.values, index, this.values.length - (index + 1));
    }

    protected double getValue(int index) {
        return this.values[index];
    }

    protected void setValue(int index, double v) {
        this.values[index] = v;
    }

    protected double[] getAllValues() {
        return this.values;
    }

    public int getType() {
        return 7;
    }
}
