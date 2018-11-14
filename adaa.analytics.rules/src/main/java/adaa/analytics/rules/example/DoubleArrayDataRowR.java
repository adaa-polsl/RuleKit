package adaa.analytics.rules.example;

import com.rapidminer.example.table.DoubleArrayDataRow;

public class DoubleArrayDataRowR extends DoubleArrayDataRow {

    private ExampleTableAdapter adapter;

    public DoubleArrayDataRowR(double[] data, ExampleTableAdapter adapter) {
        super(data);
        this.adapter=adapter;
    }

    @Override
    protected synchronized void set(int index, double value, double defaultValue) {
        adapter.setDataRowValue(index, value, defaultValue);
    }
}
