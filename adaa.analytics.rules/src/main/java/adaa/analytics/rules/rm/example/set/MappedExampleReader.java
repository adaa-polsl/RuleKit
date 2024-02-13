package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;

import java.util.Iterator;

public class MappedExampleReader extends AbstractExampleReader {
    private Iterator<Example> parent;
    private int[] mapping;
    private Example currentExample;
    private boolean nextInvoked = true;
    private int index = -1;

    public MappedExampleReader(Iterator<Example> parent, int[] mapping) {
        this.parent = parent;
        this.currentExample = null;
        this.mapping = mapping;
    }

    public boolean hasNext() {
        if (this.nextInvoked) {
            this.nextInvoked = false;
            int oldMapping = -1;
            if (this.index >= this.mapping.length - 1) {
                return false;
            }

            if (this.index != -1) {
                oldMapping = this.mapping[this.index];
            }

            ++this.index;
            int newMapping = this.mapping[this.index];
            if (newMapping == oldMapping) {
                return true;
            }

            do {
                if (!this.parent.hasNext()) {
                    return false;
                }

                this.currentExample = (Example)this.parent.next();
                ++oldMapping;
            } while(oldMapping < newMapping);
        }

        return true;
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
