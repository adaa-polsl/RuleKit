package adaa.analytics.rules.data;

import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.metadata.ColumnMetadataMap;
import adaa.analytics.rules.data.metadata.EStatisticType;
import adaa.analytics.rules.data.row.Example;

import java.io.Serializable;
import java.util.List;

public interface IExampleSet extends Serializable, Cloneable, Iterable<Example> {
    long serialVersionUID = 4100925167567270064L;

    Object clone();

    boolean equals(Object var1);

    int hashCode();

    DataTableAnnotations getAnnotations();

    int getColumnIndex(String attributeName);

    void recalculateStatistics(EStatisticType stateType, String colName);

    IAttributes getAttributes();

    int size();

    int columnCount();

    void sortBy(String columnName, EColumnSortDirections sortDir);

    Example getExample(int var1);

    IExampleSet filter(ICondition cnd);

    IExampleSet filterWithOr(List<ICondition> cndList);

    IExampleSet updateMapping(IExampleSet mappingSource);


    Object [] getValues(String colName);

    void addNewColumn(IAttribute colMetaData);

    DataColumnDoubleAdapter getDataColumnDoubleAdapter(IAttribute attr, double defaultValue);

    double getDoubleValue(String colName, int colIdx, int rowIndex, double defaultValue);

    void setDoubleValue(IAttribute att, int rowIndex, double value);
}
