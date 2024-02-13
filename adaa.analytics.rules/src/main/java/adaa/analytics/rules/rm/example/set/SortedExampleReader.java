package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

public class SortedExampleReader extends AbstractExampleReader {
    private IExampleSet parent;
    private int currentIndex;
    private boolean nextInvoked = true;
    private Example currentExample = null;

    public SortedExampleReader(IExampleSet parent) {
        this.parent = parent;
        this.currentIndex = -1;
    }

    public boolean hasNext() {
        if (this.nextInvoked) {
            this.nextInvoked = false;
            ++this.currentIndex;
            if (this.currentIndex < this.parent.size()) {
                this.currentExample = this.parent.getExample(this.currentIndex);
                return true;
            } else {
                return false;
            }
        } else {
            return this.currentIndex < this.parent.size();
        }
    }

    public Example next() {
        if (this.hasNext()) {
            this.nextInvoked = true;
            return this.currentExample;
        } else {
            return null;
        }
    }
}
