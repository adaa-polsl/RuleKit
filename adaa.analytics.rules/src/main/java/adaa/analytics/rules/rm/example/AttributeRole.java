package adaa.analytics.rules.rm.example;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AttributeRole  implements Serializable, Cloneable {
    private static final long serialVersionUID = -4855352048163007173L;
    private boolean special = false;
    private String specialName = null;
    private IAttribute attribute;
    private transient List<IAttributes> owners = new LinkedList();

    public AttributeRole(IAttribute attribute) {
        this.attribute = attribute;
    }

    private AttributeRole(AttributeRole other) {
        this.attribute = (IAttribute)other.attribute.clone();
        this.special = other.special;
        this.specialName = other.specialName;
    }

    public Object readResolve() {
        if (this.owners == null) {
            this.owners = new LinkedList();
        }

        return this;
    }

    public Object clone() {
        return new AttributeRole(this);
    }

    protected void addOwner(IAttributes attributes) {
        this.owners.add(attributes);
    }

    protected void removeOwner(IAttributes attributes) {
        this.owners.remove(attributes);
    }

    public IAttribute getAttribute() {
        return this.attribute;
    }

    public void setAttribute(IAttribute attribute) {
        this.attribute = attribute;
    }

    public boolean isSpecial() {
        return this.special;
    }

    public String getSpecialName() {
        return this.specialName;
    }

    public void setSpecial(String specialName) {
        Iterator var2 = this.owners.iterator();

        while(var2.hasNext()) {
            IAttributes attributes = (IAttributes)var2.next();
            attributes.rename(this, specialName);
        }

        this.specialName = specialName;
        if (specialName != null) {
            this.special = true;
        } else {
            this.special = false;
        }

    }

    public String toString() {
        return this.isSpecial() ? this.specialName + " := " + this.attribute.getName() : this.attribute.getName();
    }
}
