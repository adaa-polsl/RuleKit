package adaa.analytics.rules.data;

import adaa.analytics.rules.data.INominalMapping;

import java.io.Serializable;
import java.util.*;

public class NominalMapping implements Serializable, INominalMapping {

    Map<String,Integer> stringToIdx = new HashMap<>();
    List<String> idxToString = new ArrayList<>();
    public int addValue(String val) {
        val = val.replace("\"","");
        if (stringToIdx.containsKey(val))
        {
            return stringToIdx.get(val);
        }

        int firstFreeIndex = idxToString.size();
        idxToString.add(val);
        stringToIdx.put(val,firstFreeIndex);

        return firstFreeIndex;
    }

    public String getValue(int index) {
        String value = findByIndex(index);
        if(value == null) {
            throw new IllegalStateException(String.format("Index '%d' does not exist in mapping", index));
        }

        return value;
    }

    public Integer getIndex(String val) {
        return findByValue(val);
    }


    public boolean hasIndex(int index) {
        return findByIndex(index) != null;
    }

    public int size() {
        return stringToIdx.size();
    }


    public boolean equals(INominalMapping outMapping) {
        if(stringToIdx.size() != outMapping.size()) {
            return false;
        }
        for(String keyVal: stringToIdx.keySet())
        {
            Integer outIdx = outMapping.getIndex(keyVal);
            if (outIdx==null)
            {
                return false;
            }
            if (!outIdx.equals(stringToIdx.get(keyVal)))
                return false;
        }


        return true;
    }

    public void clear() {
        stringToIdx.clear();idxToString.clear();
    }


    public List<String> getValues() {
        return idxToString;
    }

    public String getPositiveValue() {
        List<String> sortedByIndex = getValues();
        if(sortedByIndex.size() < 2) {
            throw new IllegalStateException("Attribute: No other class than FIRST_CLASS_INDEX found!");
        }

        return sortedByIndex.get(1);
    }

    private String findByIndex(int index) {
        if (index>=idxToString.size())
            return null;
        return idxToString.get(index);
    }

    private Integer findByValue(String val) {
        return  stringToIdx.get(val.replace("\"",""));
    }


    @Override
    public NominalMapping clone() {
        try {
            NominalMapping cloned = (NominalMapping) super.clone();

            cloned.stringToIdx =  new HashMap<String, Integer>(stringToIdx);
            cloned.idxToString = new ArrayList<>(idxToString);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String getPositiveString() {
        return getPositiveValue();
    }

    @Override
    public Integer mapString(String str) {
        if (str == null) {
            return -1;
        } else {
            return addValue(str);
        }
    }

    @Override
    public String mapIndex(int idx) {
        return getValue(idx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringToIdx, idxToString);
    }
}
