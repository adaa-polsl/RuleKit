package adaa.analytics.rules.rm.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class Annotations implements Serializable, Map<String, String>, Cloneable {
    private static final ObjectReader reader;
    private static final ObjectWriter writer;
    private static final long serialVersionUID = 1L;
    public static final String ANNOTATIONS_TAG_NAME = "annotations";
    public static final String KEY_SOURCE = "Source";
    public static final String KEY_FILENAME = "Filename";
    public static final String KEY_COMMENT = "Comment";
    public static final String KEY_UNIT = "Unit";
    public static final String KEY_COLOR_MAP = "Colors";
    public static final String KEY_DC_AUTHOR = "dc.author";
    public static final String KEY_DC_TITLE = "dc.title";
    public static final String KEY_DC_SUBJECT = "dc.subject";
    public static final String KEY_DC_COVERAGE = "dc.coverage";
    public static final String KEY_DC_DESCRIPTION = "dc.description";
    public static final String KEY_DC_CREATOR = "dc.creator";
    public static final String KEY_DC_PUBLISHER = "dc.publisher";
    public static final String KEY_DC_CONTRIBUTOR = "dc.contributor";
    public static final String KEY_DC_RIGHTS_HOLDER = "dc.rightsHolder";
    public static final String KEY_DC_RIGHTS = "dc.rights";
    public static final String KEY_DC_PROVENANCE = "dc.provenance";
    public static final String KEY_DC_SOURCE = "dc.source";
    public static final String KEY_DC_RELATION = "dc.relation";
    public static final String KEY_DC_AUDIENCE = "dc.audience";
    public static final String KEY_DC_INSTRUCTIONAL_METHOD = "dc.description";
    public static final String[] KEYS_RAPIDMINER_IOOBJECT;
    public static final String[] KEYS_DUBLIN_CORE;
    public static final String[] ALL_KEYS_IOOBJECT;
    public static final String[] ALL_KEYS_ATTRIBUTE;
    private LinkedHashMap<String, String> keyValueMap = new LinkedHashMap();
    public static final String ANNOTATION_NAME = "Name";

    public Annotations() {
    }

    public Annotations(Annotations annotations) {
        this.keyValueMap = new LinkedHashMap(annotations.keyValueMap);
    }

    public void setAnnotation(String key, String value) {
        this.keyValueMap.put(key, value);
    }

    public String getAnnotation(String key) {
        return (String)this.keyValueMap.get(key);
    }

    public List<String> getKeys() {
        return new ArrayList(this.keyValueMap.keySet());
    }

    public void removeAnnotation(String key) {
        this.keyValueMap.remove(key);
    }

    public int size() {
        return this.keyValueMap.size();
    }

    public void clear() {
        this.keyValueMap.clear();
    }

    public boolean containsKey(Object key) {
        return this.keyValueMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.keyValueMap.containsValue(value);
    }

    public Set<Entry<String, String>> entrySet() {
        return this.keyValueMap.entrySet();
    }

    public String get(Object key) {
        return key instanceof String ? this.getAnnotation((String)key) : null;
    }

    public boolean isEmpty() {
        return this.keyValueMap.isEmpty();
    }

    public Set<String> keySet() {
        return this.keyValueMap.keySet();
    }

    public String put(String key, String value) {
        this.setAnnotation(key, value);
        return value;
    }

    public void putAll(Map<? extends String, ? extends String> m) {
        this.keyValueMap.putAll(m);
    }

    public String remove(Object key) {
        return (String)this.keyValueMap.remove(key);
    }

    public Collection<String> values() {
        return this.keyValueMap.values();
    }

    public String toString() {
        return this.keyValueMap.toString();
    }

    public Element toXML(Document doc) {
        Element elem = doc.createElement("annotations");
        Iterator var3 = this.keyValueMap.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var3.next();
            addAnnotationToXML(elem, (String)entry.getKey(), (String)entry.getValue());
        }

        return elem;
    }

    public static void addAnnotationToXML(Element annotationsElement, String name, String value) {
        if (value == null) {
            deleteAnnotationFromXML(annotationsElement, name);
        } else {
            Document doc = annotationsElement.getOwnerDocument();
            Element elem = doc.createElement("annotation");
            annotationsElement.appendChild(elem);
            elem.setAttribute("key", name);
            elem.setTextContent(value);
        }

    }

    public static void deleteAnnotationFromXML(Element annotationsElement, String name) {
        NodeList children = annotationsElement.getChildNodes();

        for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child instanceof Element && name.equals(((Element)child).getAttribute("annotation"))) {
                annotationsElement.removeChild(child);
            }
        }

    }

    public void parseXML(Element annotationsElem) {
        NodeList children = annotationsElem.getElementsByTagName("annotation");

        for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child instanceof Element) {
                String name = ((Element)child).getAttribute("key");
                this.setAnnotation(name, child.getTextContent());
            }
        }

    }

    public List<String> getDefinedAnnotationNames() {
        return new LinkedList(this.keySet());
    }

    protected Annotations clone() {
        return new Annotations(this);
    }

    public void addAll(Annotations annotations) {
        if (annotations != null) {
            this.keyValueMap.putAll(annotations);
        }

    }

    public boolean equals(Object obj) {
        return obj instanceof Annotations && this.keyValueMap.equals(((Annotations)obj).keyValueMap);
    }

    public String asPropertyStyle() throws IOException {
        return writer.writeValueAsString(this);
    }

    public static Annotations fromPropertyStyle(InputStream in) throws IOException {
        return (Annotations)reader.readValue(in);
    }

    static {
        ObjectMapper mapper = new ObjectMapper();
        reader = mapper.reader(Annotations.class);
        writer = mapper.writerWithDefaultPrettyPrinter().withType(Annotations.class);
        KEYS_RAPIDMINER_IOOBJECT = new String[]{"Source", "Comment"};
        KEYS_DUBLIN_CORE = new String[]{"dc.author", "dc.title", "dc.subject", "dc.coverage", "dc.description", "dc.creator", "dc.publisher", "dc.contributor", "dc.rightsHolder", "dc.rights", "dc.provenance", "dc.source", "dc.relation", "dc.audience", "dc.description"};
        ALL_KEYS_IOOBJECT = new String[]{"Source", "Comment", "Filename", "dc.author", "dc.title", "dc.subject", "dc.coverage", "dc.description", "dc.creator", "dc.publisher", "dc.contributor", "dc.rightsHolder", "dc.rights", "dc.provenance", "dc.source", "dc.relation", "dc.audience", "dc.description"};
        ALL_KEYS_ATTRIBUTE = new String[]{"Comment", "Unit"};
    }
}
