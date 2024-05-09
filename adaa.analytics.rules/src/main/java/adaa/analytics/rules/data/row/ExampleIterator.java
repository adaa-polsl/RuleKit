package adaa.analytics.rules.data.row;

import adaa.analytics.rules.data.DataTable;

import java.util.Iterator;

public class ExampleIterator implements Iterator<Example> {

    private DataTable dataTable;

    private int index = 0;

    public ExampleIterator(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    @Override
    public boolean hasNext() {
        return index < dataTable.size();
    }

    @Override
    public Example next() {
        return new Example(new DataRow(dataTable, index++), this.dataTable);
    }
}
