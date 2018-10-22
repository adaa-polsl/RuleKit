package adaa.analytics.rules.example;

import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;

public class DataFrameRowReader implements DataRowReader {


    public DataFrameRowReader(ExampleTableAdapter adaptor) {
        this.adaptor = adaptor;
        this.index = 0;
        this.size = adaptor.size();
    }

    private int index;
    private int size;
    private ExampleTableAdapter adaptor;

    @Override
    public boolean hasNext() {
        if (index < size) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DataRow next() {
        index++;
        return adaptor.getDataRow(index -1);
    }
}
