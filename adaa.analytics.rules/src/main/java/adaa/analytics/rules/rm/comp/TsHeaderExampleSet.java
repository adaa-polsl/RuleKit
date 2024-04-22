package adaa.analytics.rules.rm.comp;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.MetaDataTable;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.table.IExampleTable;
import adaa.analytics.rules.rm.operator.Annotations;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class TsHeaderExampleSet implements IExampleSet {

    MetaDataTable metaDataTable = null;

    IAttributes attributes = null;

    public TsHeaderExampleSet(DataTable dataTable) {
        this.metaDataTable = dataTable.getMetaDataTable();
        attributes = new TsAttributes(dataTable);
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public IAttributes getAttributes() {
        return attributes;
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public IExampleTable getExampleTable() {
        return null;
    }

    @Override
    public Example getExample(int var1) {
        return null;
    }

    @Override
    public void recalculateAttributeStatistics(IAttribute var1) {

    }

    @Override
    public double getStatistics(IAttribute var1, String var2) {
        return 0;
    }

    @Override
    public IExampleSet filter(ICondition cnd) {
        return null;
    }

    @Override
    public IExampleSet filterWithOr(List<ICondition> cndList) {
        return null;
    }

    @Override
    public IExampleSet updateMapping(IExampleSet mappingSource) {
        return null;
    }

    @Override
    public IExampleSet getMetaData() {
        return this;
    }

    @Override
    public MetaDataTable getMetaDataTable() {
        return metaDataTable;
    }

    @Override
    public DataTable getDataTable() {
        return null;
    }

    @Override
    public Object[] getValues(String colName) {
        return null;
    }

    @Override
    public Annotations getAnnotations() {
        return null;
    }

    @NotNull
    @Override
    public Iterator<Example> iterator() {
        return null;
    }
}
