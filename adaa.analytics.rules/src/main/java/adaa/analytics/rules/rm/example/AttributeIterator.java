package adaa.analytics.rules.rm.example;

import java.util.Iterator;

public class AttributeIterator implements Iterator<IAttribute> {
    private Iterator<AttributeRole> parent;
    private int type = 0;
    private IAttribute current = null;
    private boolean hasNextInvoked = false;
    private AttributeRole currentRole = null;

    public AttributeIterator(Iterator<AttributeRole> parent, int type) {
        this.parent = parent;
        this.type = type;
    }

    public boolean hasNext() {
        this.hasNextInvoked = true;
        if (!this.parent.hasNext() && this.currentRole == null) {
            this.current = null;
            return false;
        } else {
            AttributeRole role;
            if (this.currentRole == null) {
                role = (AttributeRole)this.parent.next();
            } else {
                role = this.currentRole;
            }

            switch (this.type) {
                case 0:
                    if (!role.isSpecial()) {
                        this.current = role.getAttribute();
                        this.currentRole = role;
                        return true;
                    }

                    return this.hasNext();
                case 1:
                    if (role.isSpecial()) {
                        this.current = role.getAttribute();
                        this.currentRole = role;
                        return true;
                    }

                    return this.hasNext();
                case 2:
                    this.current = role.getAttribute();
                    this.currentRole = role;
                    return true;
                default:
                    this.current = null;
                    return false;
            }
        }
    }

    public IAttribute next() {
        if (!this.hasNextInvoked) {
            this.hasNext();
        }

        this.hasNextInvoked = false;
        this.currentRole = null;
        return this.current;
    }

    public void remove() {
        this.parent.remove();
        this.currentRole = null;
        this.hasNextInvoked = false;
    }
}
