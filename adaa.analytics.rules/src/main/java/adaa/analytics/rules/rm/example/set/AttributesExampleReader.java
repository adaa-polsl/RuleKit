package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;

public class AttributesExampleReader extends AbstractExampleReader {
    private Iterator<Example> parent;
    private IExampleSet exampleSet;

    public AttributesExampleReader(Iterator<Example> parent, IExampleSet exampleSet) {
        this.parent = parent;
        this.exampleSet = exampleSet;
    }

    public boolean hasNext() {
        return this.parent.hasNext();
    }

    public Example next() {
        if (!this.hasNext()) {
            return null;
        } else {
            Example example = (Example)this.parent.next();
            return example == null ? null : new Example(example.getDataRow(), this.exampleSet);
        }
    }
}
