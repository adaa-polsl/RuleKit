package adaa.analytics.rules.data.metadata;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IAttributes;
import com.google.common.collect.LinkedHashMultimap;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnMetadataMap implements Serializable, IAttributes {

    private Map<String, ColumnMetaData> attributeMetaData = new LinkedHashMap<>();

    private DataTable owner;
    public ColumnMetadataMap(DataTable owner) {
        this.owner = owner;
    }

    public void add(String columnName, ColumnMetaData columnMetaData)
    {
        attributeMetaData.put(columnName,columnMetaData);
    }

    public void updateMapping(ColumnMetadataMap uColumnMetadataMap) {
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

    public boolean setColumnRole(String columnName, String role)
    {
        if (! attributeMetaData.containsKey(columnName))
            return false;

        ColumnMetaData columnMetadata = attributeMetaData.get(columnName);
        columnMetadata.setRole(role);
        return true;
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
        ColumnMetadataMap cloned = new ColumnMetadataMap(newOwner);

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
    public boolean contains(IAttribute var1) {
        return getColumnMetaData(var1.getName()).equals(var1);
    }

    @Override
    public int regularSize() {
        return getColumnsByRole(EColumnRole.regular.name()).size();
    }

    @Override
    public int allSize() {
        return owner.columnCount();
    }

    @Override
    public void setRegularRole(IAttribute var1) {
        String name = var1.getName();
        this.setColumnRole(name, EColumnRole.regular.name());
    }

    @Override
    public boolean removeRegularRole(IAttribute var1) {
        String name = var1.getName();
        return this.setColumnRole(name, EColumnRole.not_defined.name());
    }

    @Override
    public IAttribute get(String name) {
        return get(name, true);
    }

    @Override
    public IAttribute get(String name, boolean caseSensitive) {
        IAttribute cmd = null;

        if(caseSensitive) {
            cmd = getColumnMetaData(name);
            if(cmd == null) {
                cmd = getColumnByRole(name);
            }
            return cmd == null ? null : cmd;
        }

        for(int i=0 ; i<owner.columnCount() ; i++) {
            if(owner.getColumn(i).getName().equalsIgnoreCase(name)) {
                cmd = owner.getColumn(i);
            }
        }

        if(cmd == null) {
            for(int i=0 ; i<owner.columnCount() ; i++) {
                if(owner.getColumn(i).getRole().equalsIgnoreCase(name)) {
                    cmd = owner.getColumn(i);
                }
            }
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
    public String findRoleBySpecialName(String role) {
        IAttribute att = getColumnByRole(role);
        if (att==null)
            return null;
        return att.getName();
    }

    @Override
    public IAttribute getLabel() {
        return this.getColumnByRole(EColumnRole.label.name());
    }

    @Override
    public void setLabel(IAttribute var1) {
        this.setSpecialAttribute(var1, EColumnRole.label.name());
    }

    public IAttribute getPredictedLabel() {
        return this.getColumnByRole(EColumnRole.prediction.name());
    }

    public IAttribute getConfidence(String classLabel) {
        return this.getColumnByRole("confidence_" + classLabel);
    }

    public void setPredictedLabel(IAttribute predictedLabel) {
        this.setSpecialAttribute(predictedLabel, EColumnRole.prediction.name());
    }

    public IAttribute getWeight() {
        return this.getColumnByRole(EColumnRole.weight.name());
    }

    public IAttribute getCost() {
        return this.getColumnByRole(EColumnRole.cost.name());
    }

    @Override
    public void setSpecialAttribute(IAttribute var1, String role) {
        String name = var1.getName();
        setColumnRole(name, role);
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
