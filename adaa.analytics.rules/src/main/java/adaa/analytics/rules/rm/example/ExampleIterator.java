package adaa.analytics.rules.rm.example;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.rm.example.table.DataRow;

import java.util.Iterator;

public class ExampleIterator implements Iterator<Example> {

//    private Iterator<Row> rowIterator;
    private DataTable dataTable;
    private IExampleSet es;

    private int index = 0;

    public ExampleIterator(DataTable dataTable, IExampleSet es) {
        this.es = es;
        this.dataTable = dataTable;
    }

    @Override
    public boolean hasNext() {
        return index < dataTable.rowCount() - 1;
    }

    @Override
    public Example next() {
        return new Example(new DataRow(dataTable, index++), this.es);
    }
}
