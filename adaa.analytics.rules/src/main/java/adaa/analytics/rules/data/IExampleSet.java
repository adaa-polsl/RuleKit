package adaa.analytics.rules.data;

import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.metadata.MetaDataTable;
import adaa.analytics.rules.data.row.Example;

import java.io.Serializable;
import java.util.List;

public interface IExampleSet extends Serializable, Cloneable, Iterable<Example> {
    long serialVersionUID = 4100925167567270064L;

    Object clone();

    boolean equals(Object var1);

    int hashCode();

    DataTableAnnotations getAnnotations();

    IAttributes getAttributes();

    int size();

    void sortBy(String columnName, EColumnSortDirections sortDir);

    Example getExample(int var1);

    void recalculateAttributeStatistics(IAttribute var1);

    double getStatistics(IAttribute var1, String var2);

    IExampleSet filter(ICondition cnd);

    IExampleSet filterWithOr(List<ICondition> cndList);

    IExampleSet updateMapping(IExampleSet mappingSource);

    MetaDataTable getMetaDataTable();

    Object [] getValues(String colName);

    int addAttribute(IAttribute var1);

    DataColumnDoubleAdapter getDataColumnDoubleAdapter(IAttribute attr, double defaultValue);

    IAttribute getColumnByRole(String role);
}
