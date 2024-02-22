package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

public class IndexBasedExampleSetReader extends AbstractExampleReader {
    private int current;
    private IExampleSet parent;
    private Example next;
    private int size;

    public IndexBasedExampleSetReader(IExampleSet parent) {
        this.parent = parent;
        this.size = parent.size();
        this.current = -1;
        this.hasNext();
    }

    public boolean hasNext() {
        while(this.next == null) {
            ++this.current;
            if (this.current >= this.size) {
                return false;
            }

            this.next = this.parent.getExample(this.current);
        }

        return true;
    }

    public Example next() {
        if (!this.hasNext()) {
            return null;
        } else {
            Example dummy = this.next;
            this.next = null;
            return dummy;
        }
    }
}
