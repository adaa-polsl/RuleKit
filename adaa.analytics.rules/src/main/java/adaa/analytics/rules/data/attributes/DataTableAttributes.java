package adaa.analytics.rules.data.attributes;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.metadata.ColumnMetaData;
import adaa.analytics.rules.data.metadata.EColumnRole;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IAttributes;

import java.util.Iterator;
import java.util.List;

public class DataTableAttributes implements IAttributes {

    protected DataTable dataTable;

    public DataTableAttributes(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public Iterator<IAttribute> iterator() {
        return new AttributeIterator(dataTable, EColumnRole.regular.name());
    }

    @Override
    public Iterator<IAttribute> allAttributes() {
        return new AttributeIterator(dataTable);
    }

    @Override
    public boolean contains(IAttribute var1) {
        return dataTable.getColumn(var1.getName()).equals(var1);
    }

    @Override
    public int size() {
        return dataTable.getColumnsByRole(EColumnRole.regular.name()).size();
    }

    @Override
    public int allSize() {
        return dataTable.columnCount();
    }

    @Override
    public void addRegular(IAttribute var1) {
        String name = var1.getName();
        dataTable.setRole(name, EColumnRole.regular.name());
    }

    @Override
    public boolean remove(IAttribute var1) {
        String name = var1.getName();
        return dataTable.setRole(name, EColumnRole.not_defined.name());
    }

    @Override
    public IAttribute get(String name) {
        return get(name, true);
    }

    @Override
    public IAttribute get(String name, boolean caseSensitive) {
        IAttribute cmd = null;

        if(caseSensitive) {
            cmd = dataTable.getColumn(name);
            if(cmd == null) {
                cmd = dataTable.getColumnByRole(name);
            }
            return cmd == null ? null : cmd;
        }

        for(int i=0 ; i<dataTable.columnCount() ; i++) {
            if(dataTable.getColumn(i).getName().equalsIgnoreCase(name)) {
                cmd = dataTable.getColumn(i);
            }
        }

        if(cmd == null) {
            for(int i=0 ; i<dataTable.columnCount() ; i++) {
                if(dataTable.getColumn(i).getRole().equalsIgnoreCase(name)) {
                    cmd = dataTable.getColumn(i);
                }
            }
        }

        return cmd == null ? null : cmd;
    }

    @Override
    public IAttribute getRegular(String name) {
        ColumnMetaData colMetaData = dataTable.getColumn(name);
        if(colMetaData == null) {
            return null;
        }

        if(colMetaData.getRole() == EColumnRole.regular.name()) {
            return colMetaData;
        }

        return null;
    }

    @Override
    public IAttribute getSpecial(String role) {
        List<ColumnMetaData> cols = dataTable.getColumnsByRole(role);
        if(cols.isEmpty()) {
            return null;
        }
        else if(cols.size() > 1) {
            throw new IllegalStateException("To many returned columns for role " + role);
        }
        return cols.get(0);
    }

    @Override
    public IAttribute getLabel() {
        return this.getSpecial("label");
    }

    @Override
    public void setLabel(IAttribute var1) {
        this.setSpecialAttribute(var1, "label");
    }

    public IAttribute getPredictedLabel() {
        return this.getSpecial("prediction");
    }

    public IAttribute getConfidence(String classLabel) {
        return this.getSpecial("confidence_" + classLabel);
    }

    public void setPredictedLabel(IAttribute predictedLabel) {
        this.setSpecialAttribute(predictedLabel, "prediction");
    }

    public IAttribute getWeight() {
        return this.getSpecial("weight");
    }

    public IAttribute getCost() {
        return this.getSpecial("cost");
    }

    @Override
    public void setSpecialAttribute(IAttribute var1, String role) {
        String name = var1.getName();
        dataTable.setRole(name, role);
    }

    @Override
    public String findRoleBySpecialName(String role) {
        List<ColumnMetaData> colMetaDataList = dataTable.getColumnsByRole(role);
        if(colMetaDataList.isEmpty()) {
            return null;
        }
        else if(colMetaDataList.size() > 1) {
            throw new IllegalStateException("More then 1 column related to role name: " + role);
        }
        return colMetaDataList.get(0).getName();
    }
}
