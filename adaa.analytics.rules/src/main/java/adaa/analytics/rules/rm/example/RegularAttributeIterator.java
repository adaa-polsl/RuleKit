package adaa.analytics.rules.rm.example;

import java.util.Iterator;

public class RegularAttributeIterator implements Iterator<IAttribute> {
    private Iterator<AttributeRole> parent;
    private IAttribute current = null;

    public RegularAttributeIterator(Iterator<AttributeRole> parent) {
        this.parent = parent;
    }

    public boolean hasNext() {
        while(true) {
            if (this.current == null && this.parent.hasNext()) {
                AttributeRole candidate = (AttributeRole)this.parent.next();
                if (candidate.isSpecial()) {
                    continue;
                }

                this.current = candidate.getAttribute();
            }

            return this.current != null;
        }
    }

    public IAttribute next() {
        if (this.current == null) {
            this.hasNext();
        }

        IAttribute returnValue = this.current;
        this.current = null;
        return returnValue;
    }

    public void remove() {
        this.parent.remove();
    }
}
