package adaa.analytics.rules.rm.example;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

public abstract class AbstractAttributes implements IAttributes {
    private static final long serialVersionUID = -3419958538074776957L;

    public AbstractAttributes() {
    }

    public abstract Object clone();

    public Iterator<IAttribute> iterator() {
        return new RegularAttributeIterator(this.allAttributeRoles());
    }

    public Iterator<IAttribute> allAttributes() {
        return new AttributeIterator(this.allAttributeRoles(), 2);
    }

    public Iterator<AttributeRole> specialAttributes() {
        return new AttributeRoleIterator(this.allAttributeRoles(), 1);
    }

    public Iterator<AttributeRole> regularAttributes() {
        return new AttributeRoleIterator(this.allAttributeRoles(), 0);
    }

    public boolean contains(IAttribute attribute) {
        return this.findAttributeRole(attribute.getName()) != null;
    }

    public int allSize() {
        return this.calculateSize(this.allAttributes());
    }

    public int size() {
        return this.calculateSize(this.iterator());
    }

    public int specialSize() {
        return this.calculateSize(this.specialAttributes());
    }

    private int calculateSize(Iterator<?> i) {
        int counter;
        for(counter = 0; i.hasNext(); ++counter) {
            i.next();
        }

        return counter;
    }

    public void addRegular(IAttribute attribute) {
        this.add(new AttributeRole(attribute));
    }

    public boolean remove(IAttribute attribute) {
        AttributeRole role = this.getRole(attribute);
        return role != null ? this.remove(role) : false;
    }

    public void clearRegular() {
        List<AttributeRole> toRemove = new LinkedList();
        Iterator<AttributeRole> i = this.allAttributeRoles();

        while(i.hasNext()) {
            AttributeRole role = (AttributeRole)i.next();
            if (!role.isSpecial()) {
                toRemove.add(role);
            }
        }

        Iterator var5 = toRemove.iterator();

        while(var5.hasNext()) {
            AttributeRole role = (AttributeRole)var5.next();
            this.remove(role);
        }

    }

    public void clearSpecial() {
        List<AttributeRole> toRemove = new LinkedList();
        Iterator<AttributeRole> i = this.allAttributeRoles();

        while(i.hasNext()) {
            AttributeRole role = (AttributeRole)i.next();
            if (role.isSpecial()) {
                toRemove.add(role);
            }
        }

        Iterator var5 = toRemove.iterator();

        while(var5.hasNext()) {
            AttributeRole role = (AttributeRole)var5.next();
            this.remove(role);
        }

    }

    public IAttribute replace(IAttribute first, IAttribute second) {
        AttributeRole role = this.getRole(first);
        if (role != null) {
            role.setAttribute(second);
            return second;
        } else {
            throw new NoSuchElementException("Attribute " + first + " cannot be replaced by attribute " + second + ": " + first + " is not part of the example set!");
        }
    }

    public IAttribute get(String name) {
        return this.get(name, true);
    }

    public IAttribute get(String name, boolean caseSensitive) {
        AttributeRole result = this.findRoleByName(name, caseSensitive);
        if (result == null) {
            result = this.findRoleBySpecialName(name, caseSensitive);
        }

        return result != null ? result.getAttribute() : null;
    }

    public AttributeRole findRoleByName(String name) {
        return this.findRoleByName(name, true);
    }

    public AttributeRole findRoleBySpecialName(String specialName) {
        return this.findRoleBySpecialName(specialName, true);
    }

    public IAttribute getRegular(String name) {
        AttributeRole role = this.findRoleByName(name);
        if (role != null) {
            if (!role.isSpecial()) {
                return role.getAttribute();
            } else {
                // @TODO log service
//                LogService.getRoot().log(Level.WARNING, "AbstractAttributes.no_regular_attribute_found", name);
                return null;
            }
        } else {
            return null;
        }
    }

    public IAttribute getSpecial(String name) {
        AttributeRole role = this.findRoleBySpecialName(name);
        return role == null ? null : role.getAttribute();
    }

    public AttributeRole getRole(IAttribute attribute) {
        return this.getRole(attribute.getName());
    }

    public AttributeRole getRole(String name) {
        return this.findAttributeRole(name);
    }

    public IAttribute getLabel() {
        return this.getSpecial("label");
    }

    public void setLabel(IAttribute label) {
        this.setSpecialAttribute(label, "label");
    }

    public IAttribute getPredictedLabel() {
        return this.getSpecial("prediction");
    }

    public IAttribute getConfidence(String classLabel) {
        return this.getSpecial("confidence_" + classLabel);
    }

    public void setPredictedLabel(IAttribute predictedLabel) {
        this.setSpecialAttribute(predictedLabel, "prediction");
    }

    public IAttribute getId() {
        return this.getSpecial("id");
    }

    public void setId(IAttribute id) {
        this.setSpecialAttribute(id, "id");
    }

    public IAttribute getWeight() {
        return this.getSpecial("weight");
    }

    public void setWeight(IAttribute weight) {
        this.setSpecialAttribute(weight, "weight");
    }

    public IAttribute getCluster() {
        return this.getSpecial("cluster");
    }

    public void setCluster(IAttribute cluster) {
        this.setSpecialAttribute(cluster, "cluster");
    }

    public IAttribute getOutlier() {
        return this.getSpecial("outlier");
    }

    public void setOutlier(IAttribute outlier) {
        this.setSpecialAttribute(outlier, "outlier");
    }

    public IAttribute getCost() {
        return this.getSpecial("cost");
    }

    public void setCost(IAttribute cost) {
        this.setSpecialAttribute(cost, "cost");
    }

    public void setSpecialAttribute(IAttribute attribute, String specialName) {
        AttributeRole oldRole = this.findRoleBySpecialName(specialName);
        if (oldRole != null) {
            this.remove(oldRole);
        }

        if (attribute != null) {
            this.remove(attribute);
            AttributeRole role = new AttributeRole(attribute);
            role.setSpecial(specialName);
            this.add(role);
        }

    }

    public IAttribute[] createRegularAttributeArray() {
        int index = 0;
        IAttribute[] result = new IAttribute[this.size()];

        IAttribute attribute;
        for(Iterator var3 = this.iterator(); var3.hasNext(); result[index++] = attribute) {
            attribute = (IAttribute)var3.next();
        }

        return result;
    }

    public String toString() {
        StringBuffer result = new StringBuffer(this.getClass().getSimpleName() + ": ");
        Iterator<AttributeRole> r = this.allAttributeRoles();

        for(boolean first = true; r.hasNext(); first = false) {
            if (!first) {
                result.append(", ");
            }

            result.append(r.next());
        }

        return result.toString();
    }

    private AttributeRole findAttributeRole(String name) {
        AttributeRole role = this.findRoleByName(name);
        return role != null ? role : this.findRoleBySpecialName(name);
    }
}
