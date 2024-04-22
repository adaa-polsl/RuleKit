package adaa.analytics.rules.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NominalMapping implements Cloneable, Serializable {

    private List<NominalMap> mapping = new ArrayList<>();

    public int addValue(String val) {
        NominalMap found = findByValue(val);
        if(found != null) {
            return found.getIndex();
        }

        int firstFreeIndex = 0;
        while (findByIndex(firstFreeIndex) != null) {
            firstFreeIndex++;
        }

        mapping.add(new NominalMap(firstFreeIndex, val));

        return firstFreeIndex;
    }

    public String getValue(int index) {
        NominalMap found = findByIndex(index);
        if(found == null) {
            throw new IllegalStateException(String.format("Index '%d' does not exist in mapping", index));
        }

        return found.getValue();
    }

    public Integer getIndex(String val) {
        NominalMap found = findByValue(val);
        return found == null ? null : found.getIndex();
    }

    public boolean hasIndex(int index) {
        return findByIndex(index) != null;
    }

    public int size() {
        return mapping.size();
    }

    public boolean containesValue(String val) {
        return findByValue(val) != null;
    }

    public boolean equal(NominalMapping outMapping) {
        if(mapping.size() != outMapping.size()) {
            return false;
        }

        for(NominalMap nomMap : mapping) {
            NominalMap outNomMap = outMapping.findByValue(nomMap.getValue());
            if(outNomMap == null) {
                return false;
            }
            if(nomMap.equals(outNomMap)) {
                return false;
            }
        }

        return true;
    }

    public void clear() {
        mapping.clear();
    }

    public List<String> getValues() {

        return mapping.stream()
                .sorted(Comparator.comparingInt(NominalMap::getIndex))
                .map(NominalMap::getValue)
                .collect(Collectors.toList());
    }

    public String getPositiveValue() {
        List<String> sortedByIndex = getValues();
        if(sortedByIndex.size() < 2) {
            throw new IllegalStateException("Attribute: No other class than FIRST_CLASS_INDEX found!");
        }

        return sortedByIndex.get(1);
    }

    private NominalMap findByIndex(int index) {
        return mapping.stream().filter(e -> e.getIndex() == index).findFirst().orElse(null);
    }

    private NominalMap findByValue(String val) {
        return mapping.stream().filter(e -> e.getValue().equals(val)).findFirst().orElse(null);
    }

    @Override
    public NominalMapping clone() {
        try {
            NominalMapping cloned = (NominalMapping) super.clone();
            cloned.mapping = new ArrayList<>(mapping.size());
            for(NominalMap nomMap : mapping) {
                cloned.mapping.add(nomMap.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapping);
    }
}
