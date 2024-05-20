package adaa.analytics.rules.data;

import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.metadata.EStatisticType;
import adaa.analytics.rules.data.row.Example;
import tech.tablesaw.api.DoubleColumn;

import java.io.Serializable;
import java.util.List;

public interface IExampleSet extends Serializable, Cloneable, Iterable<Example> {
    long serialVersionUID = 4100925167567270064L;

    Object clone();



    IAttributes getAttributes();

    int getColumnIndex(String attributeName);

    double getDoubleValue(int colIdx, int rowIndex);

    void setDoubleValue(IAttribute att, int rowIndex, double value);

    DataTableAnnotations getAnnotations();


    void recalculateStatistics(EStatisticType stateType, String colName);



    int size();

    int columnCount();

    void sortBy(String columnName, EColumnSortDirections sortDir);

    Example getExample(int var1);

    IExampleSet filter(ICondition cnd);

    IExampleSet filterWithOr(List<ICondition> cndList);

    IExampleSet updateMapping(IExampleSet mappingSource);


    void addNewColumn(IAttribute colMetaData);

    DoubleColumn getDoubleColumn(IAttribute attr);


}
