package adaa.analytics.rules.rm.comp;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.EStatisticType;
import adaa.analytics.rules.data.MetaDataTable;
import adaa.analytics.rules.data.condition.EConditionsLogicOperator;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.rm.example.*;
import adaa.analytics.rules.rm.example.table.DataRow;
import adaa.analytics.rules.rm.example.table.IExampleTable;
import adaa.analytics.rules.rm.operator.Annotations;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class TsExampleSet implements IExampleSet {

    protected DataTable dataTable;

    public TsExampleSet(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public TsExampleSet(IExampleSet exampleSet) {
        this.dataTable = exampleSet.getDataTable();
    }

    @Override
    public Object clone() {
        return new TsExampleSet(dataTable.clone());
    }

    @Override
    public IAttributes getAttributes() {
        return new TsAttributes(dataTable);
    }

    @Override
    public int size() {
        return dataTable.rowCount();
    }

    @Override
    public IExampleTable getExampleTable() {
        return new TsExampleTable(dataTable);
    }

    @Override
    public Example getExample(int var1) {
        return new Example(new DataRow(dataTable, var1), this);
    }

    @Override
    public void recalculateAttributeStatistics(IAttribute var1) {
        dataTable.getMetaDataTable().getColumnMetaData(var1.getName()).recalculateStatistics();
    }

    @Override
    public double getStatistics(IAttribute var1, String var2) {
        return dataTable.getStatistic(EStatisticType.fromString(var2), var1.getName());
    }

    @Override
    public IExampleSet filter(ICondition cnd) {
        return new TsExampleSet(dataTable.filterData(cnd));
    }

    @Override
    public IExampleSet filterWithOr(List<ICondition> cndList) {
        return new TsExampleSet(dataTable.filterData(cndList, EConditionsLogicOperator.OR));
    }

    @Override
    public IExampleSet updateMapping(IExampleSet mappingSource) {
        MetaDataTable metaDataTable = mappingSource.getMetaDataTable();

        DataTable resDataTable = dataTable.clone();
        resDataTable.updateMapping(metaDataTable);

        return new TsExampleSet(resDataTable);
    }

    @Override
    public IExampleSet getMetaData() {
        return new TsHeaderExampleSet(dataTable);
    }

    @Override
    public MetaDataTable getMetaDataTable() {
        return dataTable.getMetaDataTable();
    }

    @Override
    public DataTable getDataTable() {
        return dataTable;
    }

    @Override
    public Annotations getAnnotations() {
        return new Annotations(dataTable);
    }

    @NotNull
    @Override
    public Iterator<Example> iterator() {
        return new ExampleIterator(dataTable, this);
    }
}
