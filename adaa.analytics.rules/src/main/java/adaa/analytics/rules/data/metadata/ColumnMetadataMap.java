package adaa.analytics.rules.data.metadata;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IAttributes;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnMetadataMap implements Serializable, IAttributes {

    private Map<String, ColumnMetaData> attributeMetaData = new LinkedHashMap<>();

    public void add(ColumnMetaData columnMetaData)
    {
        attributeMetaData.put(columnMetaData.getName(),columnMetaData);
    }

    public void updateMapping(ColumnMetadataMap uColumnMetadataMap, DataTable owner) {
        for (String colName : uColumnMetadataMap.getColumnNames()) {
            ColumnMetaData uColumnMetadata =  uColumnMetadataMap.getColumnMetaData(colName).cloneWithNewOwner(owner);
            if (!attributeMetaData.containsKey(colName)) {
                attributeMetaData.put(colName, uColumnMetadata);
            }
        }
    }

    public Set<String> getColumnNames() {
        return attributeMetaData.keySet();
    }


    public Collection<ColumnMetaData> getAllColumnMetaData()
    {
        return attributeMetaData.values();
    }

    public ColumnMetaData getColumnMetaData(String colName) {
        return attributeMetaData.get(colName);
    }

    public ColumnMetadataMap cloneWithNewOwner(DataTable newOwner)
    {
        ColumnMetadataMap cloned = new ColumnMetadataMap();

        for(Map.Entry<String, ColumnMetaData> entry : attributeMetaData.entrySet()) {
            ColumnMetaData clonedColumnMetaData = entry.getValue().cloneWithNewOwner(newOwner);
            cloned.attributeMetaData.put(entry.getKey(), clonedColumnMetaData);
        }
        return cloned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnMetadataMap that = (ColumnMetadataMap) o;
        return Objects.equals(attributeMetaData, that.attributeMetaData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attributeMetaData);
    }

    @Override
    public Iterator<IAttribute> iterator() {
        return new AttributeIterator(this, EColumnRole.regular.name());
    }

    @Override
    public Iterator<IAttribute> allAttributes() {
        return new AttributeIterator(this);
    }

    @Override
    public int regularSize() {
        return getColumnsByRole(EColumnRole.regular.name()).size();
    }

    @Override
    public void removeRegularRole(IAttribute var1) {
        this.setSpecialAttribute(var1, EColumnRole.not_defined.name());
    }

    @Override
    public IAttribute get(String name) {
        IAttribute cmd = getColumnMetaData(name);
        if(cmd == null) {
            cmd = getColumnByRole(name);
        }
        return cmd == null ? null : cmd;

    }

    @Override
    public IAttribute getRegular(String name) {
        ColumnMetaData colMetaData = getColumnMetaData(name);
        if(colMetaData == null) {
            return null;
        }

        if(colMetaData.getRole()!=null && colMetaData.getRole().equals(EColumnRole.regular.name())) {
            return colMetaData;
        }

        return null;
    }

    @Override
    public IAttribute getColumnByRole(String role) {
        Collection<ColumnMetaData> cols = getColumnsByRole(role);
        if(cols.isEmpty()) {
            return null;
        }
        else if(cols.size() > 1) {
            throw new IllegalStateException("To many returned columns for role " + role);
        }
        //first element
        return cols.iterator().next();
    }

    @Override
    public IAttribute getLabel() {
        return this.getColumnByRole(EColumnRole.label.name());
    }

    @Override
    public void setLabel(IAttribute var1) {
        this.setSpecialAttribute(var1, EColumnRole.label.name());
    }

    @Override
    public void setRegularRole(IAttribute var1) {
        this.setSpecialAttribute(var1, EColumnRole.regular.name());
    }

    public void setPredictedLabel(IAttribute predictedLabel) {
        this.setSpecialAttribute(predictedLabel, EColumnRole.prediction.name());
    }
    @Override
    public void setSpecialAttribute(IAttribute var1, String role) {
       var1.setRole(role);
    }

    public boolean setColumnRole(String columnName, String role)
    {
        if (! attributeMetaData.containsKey(columnName))
            return false;

        ColumnMetaData columnMetadata = attributeMetaData.get(columnName);
        columnMetadata.setRole(role);
        return true;
    }



    public IAttribute getPredictedLabel() {
        return this.getColumnByRole(EColumnRole.prediction.name());
    }

    public IAttribute getConfidence(String classLabel) {
        return this.getColumnByRole("confidence_" + classLabel);
    }


    public IAttribute getWeight() {
        return this.getColumnByRole(EColumnRole.weight.name());
    }

    public IAttribute getCost() {
        return this.getColumnByRole(EColumnRole.cost.name());
    }


    public List<ColumnMetaData> getColumnsByRole(String role) {
        if (role == null) return new ArrayList<>(0);
        return attributeMetaData.values().stream().filter(columnMetaData -> role.equals(columnMetaData.getRole())).collect(Collectors.toList());
    }

    public IAttribute getColumnByRoleUnsafe(String role) {
        Iterator<IAttribute> iAtts = allAttributes();
        while(iAtts.hasNext()) {
            IAttribute att = iAtts.next();
            if(att.getRole().equals(role)) {
                return att;
            }
        }
        return null;
    }

    public IAttribute getLabelUnsafe() {
        return getColumnByRoleUnsafe(EColumnRole.label.name());
    }
}
