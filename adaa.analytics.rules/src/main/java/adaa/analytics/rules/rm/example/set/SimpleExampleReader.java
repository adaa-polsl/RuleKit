package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.table.DataRow;
import adaa.analytics.rules.rm.example.table.IDataRowReader;

public class SimpleExampleReader extends AbstractExampleReader {
    private IDataRowReader dataRowReader;
    private IExampleSet exampleSet;

    public SimpleExampleReader(IDataRowReader drr, IExampleSet exampleSet) {
        this.dataRowReader = drr;
        this.exampleSet = exampleSet;
    }

    public boolean hasNext() {
        return this.dataRowReader.hasNext();
    }

    public Example next() {
        if (!this.hasNext()) {
            return null;
        } else {
            DataRow data = (DataRow)this.dataRowReader.next();
            return data == null ? null : new Example(data, this.exampleSet);
        }
    }
}
