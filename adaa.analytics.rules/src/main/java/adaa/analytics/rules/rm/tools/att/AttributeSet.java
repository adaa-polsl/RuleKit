package adaa.analytics.rules.rm.tools.att;

import adaa.analytics.rules.rm.example.IAttribute;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class AttributeSet {
    private List<IAttribute> regularAttributes;
    private Map<String, IAttribute> specialAttributes;
    private File defaultSource;

    public AttributeSet() {
        this.regularAttributes = new ArrayList();
        this.specialAttributes = new HashMap();
    }

    public AttributeSet(int initialCapacity) {
        this.regularAttributes = new ArrayList();
        this.specialAttributes = new HashMap();
        this.regularAttributes = new ArrayList(initialCapacity);
    }

//    public AttributeSet(AttributeDataSources attributeDataSources) throws UserError {
//        this.regularAttributes = new ArrayList();
//        this.specialAttributes = new HashMap();
//        Iterator<AttributeDataSource> i = attributeDataSources.getDataSources().iterator();
//
//        while(i.hasNext()) {
//            AttributeDataSource ads = (AttributeDataSource)i.next();
//            if (ads.getType().equals("attribute")) {
//                this.addAttribute(ads.getAttribute());
//            } else {
//                Attribute attribute = (Attribute)this.specialAttributes.get(ads.getType());
//                if (attribute != null) {
//                    throw new UserError((Operator)null, 402, new Object[]{"Special attribute name '" + ads.getType() + "' was used more than one time. Please make sure that the names of special attributes (e.g. 'label' or 'id') are unique."});
//                }
//
//                this.setSpecialAttribute(ads.getType(), ads.getAttribute());
//            }
//        }
//
//        this.defaultSource = attributeDataSources.getDefaultSource();
//    }

//    public AttributeSet(File attributeDescriptionFile, boolean sourceColRequired, LoggingHandler logging) throws XMLException, ParserConfigurationException, SAXException, IOException, UserError {
//        this(AttributeDataSource.createAttributeDataSources(attributeDescriptionFile, sourceColRequired, logging));
//    }

    public AttributeSet(List<IAttribute> regularAttributes, Map<String, IAttribute> specialAttributes) {
        this.regularAttributes = new ArrayList();
        this.specialAttributes = new HashMap();
        this.regularAttributes = regularAttributes;
        this.specialAttributes = specialAttributes;
    }

    public File getDefaultSource() {
        return this.defaultSource;
    }

    public IAttribute getAttribute(int index) {
        return (IAttribute)this.regularAttributes.get(index);
    }

    public void addAttribute(IAttribute attribute) {
        this.regularAttributes.add(attribute);
    }

    public IAttribute getSpecialAttribute(String name) {
        return (IAttribute)this.specialAttributes.get(name);
    }

    public void setSpecialAttribute(String name, IAttribute attribute) {
        this.specialAttributes.put(name, attribute);
    }

    public Set<String> getSpecialNames() {
        return this.specialAttributes.keySet();
    }

    public List<IAttribute> getRegularAttributes() {
        return this.regularAttributes;
    }

    public int getNumberOfRegularAttributes() {
        return this.regularAttributes.size();
    }

    public Map<String, IAttribute> getSpecialAttributes() {
        return this.specialAttributes;
    }

    public List<IAttribute> getAllAttributes() {
        List<IAttribute> attributes = new LinkedList();
        attributes.addAll(this.regularAttributes);
        attributes.addAll(this.specialAttributes.values());
        return attributes;
    }
}
