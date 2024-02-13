package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.*;
import adaa.analytics.rules.rm.example.table.IExampleTable;
import adaa.analytics.rules.rm.example.table.INominalMapping;
import adaa.analytics.rules.rm.operator.Annotations;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RemappedExampleSet extends AbstractExampleSet {
    private static final long serialVersionUID = 3460640319989955936L;
    private IExampleSet parent;

    /** @deprecated */
    @Deprecated
    public RemappedExampleSet(IExampleSet parentSet, IExampleSet mappingSet) {
        this(parentSet, mappingSet, true);
    }

    /** @deprecated */
    @Deprecated
    public RemappedExampleSet(IExampleSet parentSet, IExampleSet _mappingSet, boolean keepAdditional) {
        this(parentSet, _mappingSet, keepAdditional, true);
    }

    /** @deprecated */
    @Deprecated
    public RemappedExampleSet(IExampleSet parentSet, IExampleSet mappingSet, boolean keepAdditional, boolean transformMappings) {
        this.parent = (IExampleSet)parentSet.clone();
        remap(mappingSet, keepAdditional, transformMappings, this.parent);
    }

    public static IExampleSet create(IExampleSet parentSet, IExampleSet mappingSet, boolean keepAdditional, boolean transformMappings) {
        IExampleSet newSet = (IExampleSet)parentSet.clone();
        remap(mappingSet, keepAdditional, transformMappings, newSet);
        return newSet;
    }

    private static void remap(IExampleSet mappingSet, boolean keepAdditional, boolean transformMappings, IExampleSet exampleSet) {
        IExampleSet clonedMappingSet = (IExampleSet)mappingSet.clone();
        if (clonedMappingSet != null) {
            IAttributes attributes = exampleSet.getAttributes();
            Map<String, IAttribute> attributeMap = new LinkedHashMap(exampleSet.size());
            Iterator a = attributes.iterator();

            IAttribute attribute;
            while(a.hasNext()) {
                attribute = (IAttribute)a.next();
                attributeMap.put(attribute.getName(), attribute);
            }

            attributes.clearRegular();
            a = clonedMappingSet.getAttributes().iterator();

            IAttribute oldMappingAttribute;
            while(a.hasNext()) {
                attribute = (IAttribute)a.next();
                String name = attribute.getName();
                oldMappingAttribute = (IAttribute)attributeMap.get(name);
                if (oldMappingAttribute != null) {
                    attributes.addRegular(oldMappingAttribute);
                    attributeMap.remove(name);
                }
            }

            if (keepAdditional) {
                a = attributeMap.values().iterator();

                while(a.hasNext()) {
                    attribute = (IAttribute)a.next();
                    attributes.addRegular(attribute);
                }
            }

            if (transformMappings) {
                a = exampleSet.getAttributes().allAttributeRoles();

                while(a.hasNext()) {
                    AttributeRole role = (AttributeRole)a.next();
                    IAttribute currentAttribute = role.getAttribute();
                    if (currentAttribute.isNominal()) {
                        oldMappingAttribute = clonedMappingSet.getAttributes().get(role.getAttribute().getName());
                        if (oldMappingAttribute != null && oldMappingAttribute.isNominal()) {
                            INominalMapping currentMapping = currentAttribute.getMapping();
                            INominalMapping overlayedMapping = oldMappingAttribute.getMapping();
                            currentAttribute.setMapping(overlayedMapping);
                            currentAttribute.addTransformation(new FullAttributeTransformationRemapping(currentMapping));
                        }
                    }
                }
            }
        }

    }

    public RemappedExampleSet(RemappedExampleSet other) {
        this.parent = (IExampleSet)other.parent.clone();
    }

    public IAttributes getAttributes() {
        return this.parent.getAttributes();
    }

    public IExampleTable getExampleTable() {
        return this.parent.getExampleTable();
    }

    public int size() {
        return this.parent.size();
    }

    public Iterator<Example> iterator() {
        return new AttributesExampleReader(this.parent.iterator(), this);
    }

    public Example getExample(int index) {
        return this.parent.getExample(index);
    }

    public Annotations getAnnotations() {
        return this.parent.getAnnotations();
    }

    public void cleanup() {
        this.parent.cleanup();
    }
}
