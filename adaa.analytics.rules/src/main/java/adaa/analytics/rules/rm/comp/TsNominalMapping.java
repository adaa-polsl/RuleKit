package adaa.analytics.rules.rm.comp;

import adaa.analytics.rules.data.ColumnMetaData;
import adaa.analytics.rules.rm.example.table.INominalMapping;

import java.util.List;

public class TsNominalMapping implements INominalMapping {

    private ColumnMetaData colMetaData;

    public TsNominalMapping(ColumnMetaData colMetaData) {
        this.colMetaData = colMetaData;
    }

    @Override
    public boolean equals(INominalMapping mapping) {
        return colMetaData.isNominalMappingEqual(mapping.getColMetaData());
    }

    @Override
    public Object clone() {

        return null;
    }

    @Override
    public String getPositiveString() {
        return colMetaData.getMapping().getPositiveValue();
    }

    @Override
    public int getIndex(String str) {
        return colMetaData.getMapping().getIndex(str);
    }

    @Override
    public int mapString(String str) {
        if (str == null) {
            return -1;
        } else {
            return colMetaData.getMapping().addValue(str);
        }
    }

    @Override
    public String mapIndex(int var1) {
        return colMetaData.getMapping().getValue(var1);
    }

    @Override
    public List<String> getValues() {
        return colMetaData.getMapping().getValues();
    }

    @Override
    public int size() {
        return colMetaData.getMapping().size();
    }

    @Override
    public void clear() {
        colMetaData.getMapping().clear();
    }

    @Override
    public ColumnMetaData getColMetaData() {
        return colMetaData;
    }
}
