package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.AttributeTypeException;
import adaa.analytics.rules.rm.tools.Tools;

import java.util.*;

public class PolynominalMapping implements INominalMapping {
    private static final long serialVersionUID = 5021638750496191771L;
    private final Map<String, Integer> symbolToIndexMap = new LinkedHashMap();
    private final List<String> indexToSymbolMap = new ArrayList();

    public PolynominalMapping() {
    }

    public PolynominalMapping(Map<Integer, String> map) {
        this.symbolToIndexMap.clear();
        this.indexToSymbolMap.clear();
        Iterator var2 = map.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Integer, String> entry = (Map.Entry)var2.next();
            int index = (Integer)entry.getKey();
            String value = (String)entry.getValue();
            this.symbolToIndexMap.put(value, index);

            while(this.indexToSymbolMap.size() <= index) {
                this.indexToSymbolMap.add(null);
            }

            this.indexToSymbolMap.set(index, value);
        }

    }

    PolynominalMapping(INominalMapping mapping) {
        this.symbolToIndexMap.clear();
        this.indexToSymbolMap.clear();

        for(int i = 0; i < mapping.size(); ++i) {
            String value = mapping.mapIndex(i);
            this.symbolToIndexMap.put(value, i);
            this.indexToSymbolMap.add(value);
        }

    }

    public Object clone() {
        return new PolynominalMapping(this);
    }

    public boolean equals(INominalMapping mapping) {
        if (mapping.size() != this.size()) {
            return false;
        } else {
            Iterator var2 = mapping.getValues().iterator();

            String value;
            do {
                if (!var2.hasNext()) {
                    return true;
                }

                value = (String)var2.next();
            } while(this.symbolToIndexMap.containsKey(value));

            return false;
        }
    }

    public int mapString(String str) {
        if (str == null) {
            return -1;
        } else {
            int index = this.getIndex(str);
            if (index < 0) {
                this.indexToSymbolMap.add(str);
                index = this.indexToSymbolMap.size() - 1;
                this.symbolToIndexMap.put(str, index);
            }

            return index;
        }
    }

    public int getIndex(String str) {
        Integer index = (Integer)this.symbolToIndexMap.get(str);
        return index == null ? -1 : index;
    }

    public String mapIndex(int index) {
        if (index >= 0 && index < this.indexToSymbolMap.size()) {
            return (String)this.indexToSymbolMap.get(index);
        } else {
            throw new AttributeTypeException("Cannot map index of nominal attribute to nominal value: index " + index + " is out of bounds!");
        }
    }

    public void setMapping(String nominalValue, int index) {
        String oldValue = (String)this.indexToSymbolMap.get(index);
        this.indexToSymbolMap.set(index, nominalValue);
        this.symbolToIndexMap.remove(oldValue);
        this.symbolToIndexMap.put(nominalValue, index);
    }

    public int getNegativeIndex() {
        this.ensureClassification();
        if (this.mapIndex(0) == null) {
            throw new AttributeTypeException("Attribute: Cannot use FIRST_CLASS_INDEX for negative class!");
        } else {
            return 0;
        }
    }

    public int getPositiveIndex() {
        this.ensureClassification();
        if (this.mapIndex(0) == null) {
            throw new AttributeTypeException("Attribute: Cannot use FIRST_CLASS_INDEX for negative class!");
        } else {
            Iterator<Integer> i = this.symbolToIndexMap.values().iterator();

            int index;
            do {
                if (!i.hasNext()) {
                    throw new AttributeTypeException("Attribute: No other class than FIRST_CLASS_INDEX found!");
                }

                index = (Integer)i.next();
            } while(index == 0);

            return index;
        }
    }

    public String getNegativeString() {
        return this.mapIndex(this.getNegativeIndex());
    }

    public String getPositiveString() {
        return this.mapIndex(this.getPositiveIndex());
    }

    public List<String> getValues() {
        return this.indexToSymbolMap;
    }

    public int size() {
        return this.indexToSymbolMap.size();
    }

    public void sortMappings() {
        List<String> allStrings = new LinkedList(this.symbolToIndexMap.keySet());
        Collections.sort(allStrings);
        this.symbolToIndexMap.clear();
        this.indexToSymbolMap.clear();
        Iterator<String> i = allStrings.iterator();

        while(i.hasNext()) {
            this.mapString((String)i.next());
        }

    }

    public void clear() {
        this.symbolToIndexMap.clear();
        this.indexToSymbolMap.clear();
    }

    private void ensureClassification() {
        if (this.size() != 2) {
            throw new AttributeTypeException("Attribute " + this.toString() + " is not a classification attribute!");
        }
    }

    public String toString() {
        return this.indexToSymbolMap.toString() + Tools.getLineSeparator() + this.symbolToIndexMap.toString();
    }
}
