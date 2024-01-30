package adaa.analytics.rules.rm.example;

//import AbstractAttributes;
//import Attribute;
//import AttributeRole;
//import DuplicateAttributeException;
//import NoSuchAttributeException;

import java.util.*;

public class SimpleAttributes extends AbstractAttributes {
    private static final long serialVersionUID = 6388263725741578818L;
    private final List<AttributeRole> attributes;
    private transient Map<String, AttributeRole> nameToAttributeRoleMap = new HashMap();
    private transient Map<String, AttributeRole> specialNameToAttributeRoleMap = new HashMap();

    public SimpleAttributes() {
        this.attributes = Collections.synchronizedList(new ArrayList());
    }

    private SimpleAttributes(SimpleAttributes attributes) {
        this.attributes = Collections.synchronizedList(new ArrayList(attributes.allSize()));
        Iterator var2 = attributes.attributes.iterator();

        while(var2.hasNext()) {
            AttributeRole role = (AttributeRole)var2.next();
            this.register((AttributeRole)role.clone(), false);
        }

    }

    public Object readResolve() {
        if (this.nameToAttributeRoleMap == null) {
            this.nameToAttributeRoleMap = new HashMap();
            this.specialNameToAttributeRoleMap = new HashMap();
        }

        Iterator var1 = this.attributes.iterator();

        while(var1.hasNext()) {
            AttributeRole attributeRole = (AttributeRole)var1.next();
            this.register(attributeRole, true);
        }

        return this;
    }

    public Object clone() {
        return new SimpleAttributes(this);
    }

    public boolean equals(Object o) {
        if (!(o instanceof SimpleAttributes)) {
            return false;
        } else {
            SimpleAttributes other = (SimpleAttributes)o;
            return this.attributes.equals(other.attributes);
        }
    }

    public int hashCode() {
        return this.attributes.hashCode();
    }

    public Iterator<AttributeRole> allAttributeRoles() {
        final Iterator<AttributeRole> i = this.attributes.iterator();
        return new Iterator<AttributeRole>() {
            private AttributeRole current;

            public boolean hasNext() {
                return i.hasNext();
            }

            public AttributeRole next() {
                this.current = (AttributeRole)i.next();
                return this.current;
            }

            public void remove() {
                i.remove();
                SimpleAttributes.this.unregister(this.current, true);
            }
        };
    }

    private void register(AttributeRole attributeRole, boolean onlyMaps) {
        String name = attributeRole.getAttribute().getName();
        if (this.nameToAttributeRoleMap.containsKey(name)) {
            // @TODO Exception
//            throw new DuplicateAttributeException(name);
            throw new RuntimeException("");
        } else {
            String specialName = attributeRole.getSpecialName();
            if (specialName != null && this.specialNameToAttributeRoleMap.containsKey(specialName)) {
                // @TODO Exception
//                throw new DuplicateAttributeException(name, true);
                throw new RuntimeException("");
            } else {
                this.nameToAttributeRoleMap.put(name, attributeRole);
                if (specialName != null) {
                    this.specialNameToAttributeRoleMap.put(specialName, attributeRole);
                }

                if (!onlyMaps) {
                    this.attributes.add(attributeRole);
                }

                attributeRole.addOwner(this);
                attributeRole.getAttribute().addOwner(this);
            }
        }
    }

    private boolean unregister(AttributeRole attributeRole, boolean onlyMap) {
        if (!this.nameToAttributeRoleMap.containsKey(attributeRole.getAttribute().getName())) {
            return false;
        } else {
            this.nameToAttributeRoleMap.remove(attributeRole.getAttribute().getName());
            if (attributeRole.getSpecialName() != null) {
                this.specialNameToAttributeRoleMap.remove(attributeRole.getSpecialName());
            }

            if (!onlyMap) {
                this.attributes.remove(attributeRole);
            }

            attributeRole.removeOwner(this);
            attributeRole.getAttribute().removeOwner(this);
            return true;
        }
    }

    public void rename(AttributeRole attributeRole, String newSpecialName) {
        if (attributeRole.getSpecialName() != null) {
            AttributeRole role = (AttributeRole)this.specialNameToAttributeRoleMap.get(attributeRole.getSpecialName());
            if (role == null) {
                // @TODO Exception
                throw new RuntimeException("");
//                throw new NoSuchAttributeException(attributeRole.getSpecialName(), true);
            }

            if (role != attributeRole) {
                throw new RuntimeException("Broken attribute role map.");
            }
        }

        this.specialNameToAttributeRoleMap.remove(attributeRole.getSpecialName());
        if (newSpecialName != null) {
            this.specialNameToAttributeRoleMap.put(newSpecialName, attributeRole);
        }

    }

    public void rename(IAttribute attribute, String newName) {
        if (this.nameToAttributeRoleMap.containsKey(newName)) {
//            throw new DuplicateAttributeException(newName);
            // @TODO Exception
            throw new RuntimeException("");
        } else {
            AttributeRole role = (AttributeRole)this.nameToAttributeRoleMap.get(attribute.getName());
            if (role == null) {
//                throw new NoSuchAttributeException(attribute.getName());
                // @TODO Exception
                throw new RuntimeException("");
            } else if (role.getAttribute() != attribute) {
                throw new RuntimeException("Broken attribute map.");
            } else {
                this.nameToAttributeRoleMap.remove(role.getAttribute().getName());
                this.nameToAttributeRoleMap.put(newName, role);
            }
        }
    }

    public void add(AttributeRole attributeRole) {
        this.register(attributeRole, false);
    }

    public boolean remove(AttributeRole attributeRole) {
        return this.unregister(attributeRole, false);
    }

    public AttributeRole findRoleByName(String name, boolean caseSensitive) {
        return findRole(name, caseSensitive, this.nameToAttributeRoleMap);
    }

    public AttributeRole findRoleBySpecialName(String specialName, boolean caseSensitive) {
        return findRole(specialName, caseSensitive, this.specialNameToAttributeRoleMap);
    }

    private static AttributeRole findRole(String key, boolean caseSensitive, Map<String, AttributeRole> roleMap) {
        if (!caseSensitive && key != null) {
            Iterator var3 = roleMap.entrySet().iterator();

            Map.Entry entry;
            do {
                if (!var3.hasNext()) {
                    return null;
                }

                entry = (Map.Entry)var3.next();
            } while(!key.equalsIgnoreCase((String)entry.getKey()));

            return (AttributeRole)entry.getValue();
        } else {
            return (AttributeRole)roleMap.get(key);
        }
    }

    public int size() {
        return this.nameToAttributeRoleMap.size() - this.specialNameToAttributeRoleMap.size();
    }

    public int allSize() {
        return this.nameToAttributeRoleMap.size();
    }

    public int specialSize() {
        return this.specialNameToAttributeRoleMap.size();
    }
}
