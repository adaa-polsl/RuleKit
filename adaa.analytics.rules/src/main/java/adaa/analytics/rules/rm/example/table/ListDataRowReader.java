package adaa.analytics.rules.rm.example.table;

import java.util.Iterator;

public class ListDataRowReader implements IDataRowReader {
    private Iterator<DataRow> iterator;

    public ListDataRowReader(Iterator<DataRow> i) {
        this.iterator = i;
    }

    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    public DataRow next() {
        return (DataRow)this.iterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("The method 'remove' is not supported by DataRowReaders!");
    }
}
