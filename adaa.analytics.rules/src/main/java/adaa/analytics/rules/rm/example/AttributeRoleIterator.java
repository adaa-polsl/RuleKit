package adaa.analytics.rules.rm.example;

import java.util.Iterator;

public class AttributeRoleIterator implements Iterator<AttributeRole> {
    private Iterator<AttributeRole> parent;
    private int type = 0;
    private AttributeRole current = null;

    public AttributeRoleIterator(Iterator<AttributeRole> parent, int type) {
        this.parent = parent;
        this.type = type;
    }

    public boolean hasNext() {
        while(this.current == null && this.parent.hasNext()) {
            AttributeRole candidate = (AttributeRole)this.parent.next();
            switch (this.type) {
                case 0:
                    if (!candidate.isSpecial()) {
                        this.current = candidate;
                    }
                    break;
                case 1:
                    if (candidate.isSpecial()) {
                        this.current = candidate;
                    }
                    break;
                case 2:
                    this.current = candidate;
            }
        }

        return this.current != null;
    }

    public AttributeRole next() {
        if (this.current == null) {
            this.hasNext();
        }

        AttributeRole returnValue = this.current;
        this.current = null;
        return returnValue;
    }

    public void remove() {
        this.parent.remove();
        this.current = null;
    }
}
