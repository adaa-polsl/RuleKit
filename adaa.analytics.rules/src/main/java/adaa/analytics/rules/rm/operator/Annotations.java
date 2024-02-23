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
    private static final long serialVersionUID = 1L;
    private LinkedHashMap<String, String> keyValueMap = new LinkedHashMap();


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

}
