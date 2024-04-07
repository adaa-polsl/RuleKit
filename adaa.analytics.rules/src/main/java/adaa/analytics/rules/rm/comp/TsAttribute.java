package adaa.analytics.rules.rm.comp;

import adaa.analytics.rules.data.ColumnMetaData;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.table.DataRow;
import adaa.analytics.rules.rm.example.table.INominalMapping;

import java.util.Objects;

public class TsAttribute implements IAttribute {

    ColumnMetaData columnMetaData;

    public TsAttribute(ColumnMetaData columnMetaData) {

        this.columnMetaData = columnMetaData;
    }

    @Override
    public Object clone() {
        return new TsAttribute(columnMetaData.clone());
    }

    @Override
    public String getName() {
        return columnMetaData.getName();
    }

    @Override
    public void setName(String var1) {
        columnMetaData.setName(var1);
    }

    @Override
    public int getTableIndex() {
        return columnMetaData.getTableIndex();
    }

    @Override
    public INominalMapping getMapping() {
        return new TsNominalMapping(columnMetaData);
    }

    public double getValue(DataRow row) {
        return row.get(this);
    }

    public void setValue(DataRow row, double value) {
        row.set(this, value);
    }

    @Override
    public int getValueType() {
        return Converter.EColumnTypeToRmOntology(columnMetaData.getColumnType());
    }

    @Override
    public boolean isNominal() {
        return columnMetaData.isNominal();
    }

    @Override
    public boolean isNumerical() {
        return columnMetaData.isNumerical();
    }

    @Override
    public String getAsString(double value, int fractionDigits, boolean quoteNominal) {
        if (Double.isNaN(value)) {
            return "?";
        }

        if(columnMetaData.isNominal()) {
            String result = columnMetaData.getMapping().getValue((int)value);
            if (quoteNominal) {
                result = "\"" + result + "\"";
            }
            return result;
        }

        return "?";
    }

    @Override
    public ColumnMetaData getColumnMetaData() {
        return columnMetaData;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof TsAttribute)) {
            return false;
        }
        TsAttribute tsa = (TsAttribute) o;
        return tsa.columnMetaData.equals(columnMetaData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnMetaData);
    }
}
