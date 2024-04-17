package adaa.analytics.rules.data;

import adaa.analytics.rules.data.condition.EConditionsLogicOperator;
import adaa.analytics.rules.data.condition.ICondition;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.selection.Selection;
import utils.AttributeInfo;

import java.util.*;
import java.util.stream.Collectors;

public class DataTable implements Cloneable {

    private Table table;
    private Map<String, ColumnMetaData> columnMetaData = new LinkedHashMap<>();

    private Map<String, String> annotations = new LinkedHashMap<>();

    private DataTable(Table table) {
        this.table = table;
    }

    public DataTable(CsvReadOptions options) {

        table = Table.read().usingOptions(options);
    }

    public DataTable(CsvReadOptions.Builder builder, List<AttributeInfo> attsInfo) {

        // Predefined column types for tablesaw csv loader (if necessary)
        ColumnType[] columnTypes;

        if(attsInfo == null) {
            throw new NullPointerException("Attributes info list is required");
        }

        // Fill predefined column types for tablesaw csv loader if attributesInfo is defined
        columnTypes = new ColumnType[attsInfo.size()];
        for(int i=0 ; i<attsInfo.size() ; i++) {

            if(attsInfo.get(i).getCellType() == EColumnType.NUMERICAL) {
                columnTypes[i] = ColumnType.DOUBLE;
            }
            else if(attsInfo.get(i).getCellType() == EColumnType.NOMINAL) {
                columnTypes[i] = ColumnType.STRING;
            }
            else if(attsInfo.get(i).getCellType() == EColumnType.DATE) {
                columnTypes[i] = ColumnType.LOCAL_DATE_TIME;
            }
            else {
                columnTypes[i] = ColumnType.STRING;
            }
        }

        builder = builder.columnTypes(columnTypes);

        // Use predefined column types in table saw csv loader
        table = Table.read().usingOptions(builder.build());
        for(int i=0 ; i<attsInfo.size() ; i++) {
            AttributeInfo attInfo = attsInfo.get(i);
            table.column(i).setName(attInfo.getName());
            columnMetaData.put(
                    attInfo.getName(),
                    new ColumnMetaData(attInfo.getName(), attInfo.getCellType(), EColumnRole.regular.name(), attInfo.getValues(), this)
            );
        }
    }

    public int columnCount() {
        return table.columnCount();
    }

    public void createPredictionColumn(String name) {
        ColumnMetaData labelColMetaData = this.getColumnByRole(EColumnRole.label.name());
        labelColMetaData.setName(name);
        labelColMetaData.setRole(EColumnRole.prediction.name());

        addNewColumn(labelColMetaData);
    }

    public void addNewColumn(ColumnMetaData colMetaData) {
        ColumnMetaData newColMetaData = colMetaData.cloneWithNewOwner(this);
        Column<?> tsCol = null;

        if(newColMetaData.getColumnType() == EColumnType.NOMINAL) {
            tsCol = StringColumn.create(newColMetaData.getName(), table.rowCount());
        }
        else if(newColMetaData.getColumnType() == EColumnType.NUMERICAL) {
            tsCol = DoubleColumn.create(newColMetaData.getName(), table.rowCount());
        }
//        else if(newColMetaData.getColumnType() == EColumnType.DATE) {
//            tsCol = DateTimeColumn.create(newColMetaData.getName(), table.rowCount());
//        }

        if(tsCol == null) {
            throw new IllegalStateException("Cannot crate prediction column, unknown column type");
        }

        table.addColumns(tsCol);
        columnMetaData.put(newColMetaData.getName(), newColMetaData);
    }

    public ColumnMetaData getColumn(String name) {
        return columnMetaData.get(name);
    }

    public ColumnMetaData getColumn(int index) {
        String colName = table.column(index).name();
        return columnMetaData.get(colName);
    }

    public List<ColumnMetaData> getColumnsByRole(String role) {

        return columnMetaData.values().stream()
                .filter(columnMetaData -> role.equals(columnMetaData.getRole()))
                .collect(Collectors.toList());
    }

    public ColumnMetaData getColumnByRole(String role) {

        List<ColumnMetaData> cols = columnMetaData.values().stream()
                .filter(columnMetaData -> role.equals(columnMetaData.getRole()))
                .collect(Collectors.toList());

        if(cols.isEmpty()) {
            return null;
        }
        else if(cols.size() > 1) {
            throw new IllegalStateException(String.format("More than 1 column found for role '%s'", role));
        }

        return cols.get(0);
    }

    public void removeColumn(String name) {
        columnMetaData.remove(name);
        table.removeColumns(name);
    }

    public int rowCount() {
        return table.rowCount();
    }

    public Row getRow(int index) {
        return table.row(index);
    }

    public int getColumnIndex(String attributeName) {
        return table.columnIndex(attributeName);
    }

    public boolean setRole(String columnName, String role) {
        if(!columnMetaData.containsKey(columnName)) {
            return false;
        }

        columnMetaData.get(columnName).setRole(role);

        return true;
    }

    public void sortBy(String columnName, EColumnSortDirections sortDir) {
        table = table.sortOn((sortDir == EColumnSortDirections.DECREASING ? "-" : "") + columnName);
    }

    public Iterator<Row> iterator(){
        return table.iterator();
    }

    @Override
    public DataTable clone() {
        try {
            DataTable cloned = (DataTable) super.clone();
            cloned.table = table.copy();
            cloned.columnMetaData = new LinkedHashMap<>();
            for(Map.Entry<String, ColumnMetaData> entry : columnMetaData.entrySet()) {
                cloned.columnMetaData.put(entry.getKey(), entry.getValue().cloneWithNewOwner(cloned));
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public double getStatistic(EStatisticType statType, String colName) {

        return columnMetaData.get(colName).getStatistic(statType);
    }

    public void recalculateStatistics() {

        for(String colName : columnMetaData.keySet()) {
            recalculateStatistics(colName);
        }
    }

    public void recalculateStatistics(EStatisticType statType) {

        for(String colName : columnMetaData.keySet()) {
            recalculateStatistics(statType, colName);
        }
    }

    public void recalculateStatistics(EStatisticType stateType, String colName) {
        Column<?> col = table.column(colName);

        if(!(col instanceof DoubleColumn)) {
            // @TODO Add log
//            throw new IllegalStateException(String.format("Cannot calculate %s statistic for not numerical column: %s", stateType, colName));
            return;
        }

        DoubleColumn numCol = (DoubleColumn)col;

        double value = 0.0;

        if(stateType == EStatisticType.AVERAGE) {
            value = numCol.mean();
        }
        else if(stateType == EStatisticType.MAXIMUM) {
            value = numCol.max();
        }
        else if(stateType == EStatisticType.MINIMUM) {
            value = numCol.min();
        }
        else if(stateType == EStatisticType.VARIANCE) {
            value = numCol.variance();
        }
        else if(stateType == EStatisticType.AVERAGE_WEIGHTED) {
            ColumnMetaData colMetaData = this.getColumnByRole(EColumnRole.weight.name());
            if(colMetaData == null || !colMetaData.isNumerical()) {
//                throw new NotImplementedException("Column with 'weight' role not found");
                value = Double.NaN;
            }
            else {
                DoubleColumn weightCol = (DoubleColumn)table.column(colMetaData.getName());

                double weightSum = 0.0;
                for (int i = 0; i < numCol.size(); i++) {
                    double dVal = numCol.getDouble(i);
                    double wVal = weightCol.getDouble(i);
                    value += wVal * dVal;
                    weightSum += wVal;
                }

                value = value / weightSum;
            }
        }

        ColumnMetaData colMetaData = columnMetaData.get(colName);
        if(colMetaData == null) {
            throw new IllegalStateException(String.format("Cannot find column '%s' in table meta data", colName));
        }

        colMetaData.setStatistic(stateType, value);
    }

    public void recalculateStatistics(String colName) {

        for(EStatisticType statType : EStatisticType.values()) {
            recalculateStatistics(statType, colName);
        }
    }

    public DataTable filterData(ICondition condition) {
        return new DataTable(table.where(condition.createSelection(table)));
    }

    public DataTable filterData(List<ICondition> conditions, EConditionsLogicOperator logicOp) {

        if(conditions.isEmpty()){
            return null;
        }

        Selection sel = addCondition(conditions, 0, logicOp);
        return new DataTable(table.where(sel));
    }

    public MetaDataTable getMetaDataTable() {
        return new MetaDataTable(columnMetaData.values());
    }

    public MetaDataTable getMetaDataTable(String eColRole) {
        return new MetaDataTable(columnMetaData.values().stream()
                .filter(c -> c.getRole() == eColRole)
                .collect(Collectors.toList()));
    }

    public void updateMapping(MetaDataTable metaDataTable) {
        for(String colName : metaDataTable.getColumnNames()) {
            if(!columnMetaData.containsKey(colName)) {
                columnMetaData.put(colName, metaDataTable.getColumnMetaData(colName).cloneWithNewOwner(this));
            }
        }
    }

    public double getDoubleValue(String colName, int rowIndex, double defaultValue) {
        ColumnMetaData colMetaData = columnMetaData.get(colName);
        if(colMetaData == null) {
            return defaultValue;
        }

        if(colMetaData.isNominal()) {
            StringColumn colStr = table.stringColumn(colName);
            String value = colStr.get(rowIndex);
            Integer iVal = colMetaData.getMapping().getIndex(value);
            return iVal == null ? defaultValue : iVal.doubleValue();
        }

        DoubleColumn colNum = (DoubleColumn) table.column(colName);
        return colNum.getDouble(rowIndex);
    }

    public void setDoubleValue(String colName, int rowIndex, double value) {
        ColumnMetaData colMetaData = columnMetaData.get(colName);
        if(colMetaData == null) {
            throw new IllegalStateException(String.format("Column '%s' does not exist", colName));
        }

        if(colMetaData.isNominal()) {
            int iValue = (int)value;
            if(!colMetaData.getMapping().hasIndex(iValue)) {
                throw new IllegalStateException(String.format("There is no index '%d' in '%s' column mapping", iValue, colName));
            }
            StringColumn colStr = table.stringColumn(colName);
            colStr.set(rowIndex, colMetaData.getMapping().getValue(iValue));
        }
        else {
            DoubleColumn colNum = (DoubleColumn) table.column(colName);
            colNum.set(rowIndex, value);
        }
    }

    public void setAnnotation(String key, String value) {
        annotations.put(key, value);
    }

    public String getAnnotation(String key) {
        return annotations.get(key);
    }

    public int sizeAnnotations() {
        return annotations.size();
    }

    public void clearAnnotations() {
        annotations.clear();
    }

    public boolean containsAnnotationKey(String key) {
        return annotations.containsKey(key);
    }

    public Object [] getValues(String colName) {
        ColumnMetaData cmd = getColumn(colName);

        if(cmd == null) {
            throw new IllegalStateException(String.format("Column '%s' does not exist", colName));
        }

        if(cmd.isNumerical()) {
            return table.doubleColumn(colName).asObjectArray();
        }

        if(cmd.isNominal()) {
            return table.stringColumn(colName).asObjectArray();
        }

        return null;
    }

    private Selection addCondition(List<ICondition> conditions, int conditionIndex, EConditionsLogicOperator logicOp) {

        ICondition condition = conditions.get(conditionIndex);
        if(conditionIndex == conditions.size()-1) {
            return condition.createSelection(table);
        }
        else {
            Selection selection = addCondition(conditions, conditionIndex+1, logicOp);
            return selection.or(condition.createSelection(table));
        }
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof DataTable)) {
            return false;
        }
        DataTable dt = (DataTable) o;
        if(!dt.table.equals(table)) {
            return false;
        }
        return dt.columnMetaData.equals(columnMetaData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, columnMetaData);
    }
}
