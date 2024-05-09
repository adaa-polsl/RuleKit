package adaa.analytics.rules.data;

import adaa.analytics.rules.data.condition.EConditionsLogicOperator;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.*;
import adaa.analytics.rules.data.row.DataRow;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.row.ExampleIterator;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.utils.Tools;
import ioutils.AttributeInfo;
import org.jetbrains.annotations.NotNull;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.selection.Selection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class DataTable implements Serializable, IExampleSet {

    private Table table;
    private ColumnMetadataMap columnMetadataMap = new ColumnMetadataMap(this);
    private DataTableAnnotations dataTableAnnotations = new DataTableAnnotations();

    //local cache to optimize access to column idx
    private Map<String, Integer> columnIdxMap = new HashMap<>();

    private DataTable(Table table, ColumnMetadataMap columnMetadataMap, DataTableAnnotations dataTableAnnotations) {
        this.table = table;
    }

    public DataTable(CsvReadOptions.Builder builder, List<AttributeInfo> attsInfo) {

        // Predefined column types for tablesaw csv loader (if necessary)
        ColumnType[] columnTypes;

        if (attsInfo == null) {
            throw new NullPointerException("Attributes info list is required");
        }

        // Fill predefined column types for tablesaw csv loader if attributesInfo is defined
        columnTypes = new ColumnType[attsInfo.size()];
        for (int i = 0; i < attsInfo.size(); i++) {

            if (attsInfo.get(i).getCellType() == EColumnType.NUMERICAL) {
                columnTypes[i] = ColumnType.DOUBLE;
            } else if (attsInfo.get(i).getCellType() == EColumnType.NOMINAL) {
                columnTypes[i] = ColumnType.STRING;
            } else if (attsInfo.get(i).getCellType() == EColumnType.DATE) {
                columnTypes[i] = ColumnType.LOCAL_DATE_TIME;
            } else {
                columnTypes[i] = ColumnType.STRING;
            }
        }

        builder = builder.columnTypes(columnTypes);

        // Use predefined column types in table saw csv loader
        table = Table.read().usingOptions(builder.build());
        for (int i = 0; i < attsInfo.size(); i++) {
            AttributeInfo attInfo = attsInfo.get(i);
            table.column(i).setName(attInfo.getName());
            columnMetadataMap.add(attInfo.getName(), new ColumnMetaData(attInfo.getName(), attInfo.getCellType(), EColumnRole.regular.name(), attInfo.getValues(), this));
        }
    }

    public DataTable(Object[][] values, String[] attributesNames, String decisionAttribute, String survivalTimeAttribute, String contrastAttribute) {

        if (values.length == 0) {
            throw new RuntimeException("DataTable: data matrix is not allowed to be empty.");
        }

        if (values[0].length != attributesNames.length) {
            throw new RuntimeException("DataTable: number of columns in matrix is not equal to number of attributes");
        }

        table = Table.create("");

        for (int i = 0; i < attributesNames.length; i++) {
            String attName = attributesNames[i];
            Object obj = values[0][i];
            EColumnType colType = EColumnType.OTHER;
            if (obj instanceof String) {
                colType = EColumnType.NOMINAL;
            } else if (obj instanceof Boolean || obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double) {
                colType = EColumnType.NUMERICAL;
            }
            String colRole = EColumnRole.regular.name();
            if (attName.equals(decisionAttribute)) {
                colRole = EColumnRole.label.name();
            } else if (attName.equals(survivalTimeAttribute)) {
                colRole = EColumnRole.survival_time.name();
            } else if (attName.equals(contrastAttribute)) {
                colRole = ContrastRule.CONTRAST_ATTRIBUTE_ROLE;
            }

            String[] nomData = null;
            Double[] numData = null;

            Set<String> nomMapValues = new HashSet<>();

            if (colType == EColumnType.NOMINAL) {
                nomData = new String[values.length];
            } else if (colType == EColumnType.NUMERICAL) {
                numData = new Double[values.length];
            }

            for (int j = 0; j < values.length; j++) {
                if (colType == EColumnType.NOMINAL) {
                    nomData[j] = (String) values[j][i];
                    nomMapValues.add(nomData[j]);
                } else if (colType == EColumnType.NUMERICAL) {
                    numData[j] = Tools.convertToDouble(values[j][i]);
                }
            }

            ColumnMetaData colMetaData = new ColumnMetaData(attName, colType, colRole, new ArrayList<>(nomMapValues), this);

            Column<?> col = null;
            if (colType == EColumnType.NOMINAL) {
                col = StringColumn.create(attName, nomData);
            } else if (colType == EColumnType.NUMERICAL) {
                col = DoubleColumn.create(attName, numData);
            }
            table.addColumns(col);
            columnMetadataMap.add(attName, colMetaData);
        }
    }

    public int columnCount() {
        return table.columnCount();
    }

    public void createPredictionColumn(String name) {
        IAttribute labelColMetaData = columnMetadataMap.getColumnByRole(EColumnRole.label.name());
        labelColMetaData.setName(name);
        labelColMetaData.setRole(EColumnRole.prediction.name());

        addNewColumn(labelColMetaData);
    }

    public void addNewColumn(IAttribute colMetaData) {
        ColumnMetaData newColMetaData = ((ColumnMetaData) colMetaData).cloneWithNewOwner(this);
        Column<?> tsCol = null;

        if (newColMetaData.getColumnType() == EColumnType.NOMINAL) {
            tsCol = StringColumn.create(newColMetaData.getName(), table.rowCount());
        } else if (newColMetaData.getColumnType() == EColumnType.NUMERICAL) {
            tsCol = DoubleColumn.create(newColMetaData.getName(), table.rowCount());
        }

        if (tsCol == null) {
            throw new IllegalStateException("Cannot crate prediction column, unknown column type");
        }

        table.addColumns(tsCol);
        columnMetadataMap.add(newColMetaData.getName(), newColMetaData);
    }

    public ColumnMetaData getColumn(String name) {
        return columnMetadataMap.getColumnMetaData(name);
    }

    public ColumnMetaData getColumn(int index) {
        String colName = table.column(index).name();
        return columnMetadataMap.getColumnMetaData(colName);
    }

    public Row getRow(int index) {
        return table.row(index);
    }


    public int getColumnIndex(String attributeName) {
        if (columnIdxMap.containsKey(attributeName)) {
            return columnIdxMap.get(attributeName).intValue();
        } else {
            int idx = table.columnIndex(attributeName);
            columnIdxMap.put(attributeName, idx);
            return idx;
        }
    }

    public boolean setRole(String columnName, String role) {
        return columnMetadataMap.setColumnRole(columnName, role);
    }

    public void sortBy(String columnName, EColumnSortDirections sortDir) {
        table = table.sortOn((sortDir == EColumnSortDirections.DECREASING ? "-" : "") + columnName);
    }

    @Override
    public DataTable clone() {
        DataTable cloned = new DataTable(table.copy(),null, dataTableAnnotations.clone());
        cloned.columnMetadataMap = columnMetadataMap.cloneWithNewOwner(cloned);
        return cloned;
    }

    public void recalculateStatistics(EStatisticType stateType, String colName) {
        Column<?> col = table.column(colName);

        if (!(col instanceof DoubleColumn)) {
            // @TODO Add log
//            throw new IllegalStateException(String.format("Cannot calculate %s statistic for not numerical column: %s", stateType, colName));
            return;
        }

        DoubleColumn numCol = (DoubleColumn) col;

        double value = 0.0;

        if (stateType == EStatisticType.AVERAGE) {
            value = numCol.mean();
        } else if (stateType == EStatisticType.MAXIMUM) {
            value = numCol.max();
        } else if (stateType == EStatisticType.MINIMUM) {
            value = numCol.min();
        } else if (stateType == EStatisticType.VARIANCE) {
            value = numCol.variance();
        } else if (stateType == EStatisticType.AVERAGE_WEIGHTED) {
            IAttribute colMetaData = columnMetadataMap.getColumnByRole(EColumnRole.weight.name());
            if (colMetaData == null || !colMetaData.isNumerical()) {
//                throw new NotImplementedException("Column with 'weight' role not found");
                value = Double.NaN;
            } else {
                DoubleColumn weightCol = (DoubleColumn) table.column(colMetaData.getName());

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

        ColumnMetaData colMetaData = columnMetadataMap.getColumnMetaData(colName);
        if (colMetaData == null) {
            throw new IllegalStateException(String.format("Cannot find column '%s' in table meta data", colName));
        }

        colMetaData.setStatistic(stateType, value);
    }

    void updateMapping(IAttributes uColumnMetadataMap) {
        columnMetadataMap.updateMapping((ColumnMetadataMap) uColumnMetadataMap);
    }

    public double getDoubleValue(String colName, int colIdx, int rowIndex, double defaultValue) {
        Column col = table.column(colIdx);
        if (col.type().equals(ColumnType.DOUBLE)) {
            DoubleColumn colNum = (DoubleColumn) col;
            return colNum.getDouble(rowIndex);
        } else {
            StringColumn colStr = (StringColumn) col;
            String value = colStr.get(rowIndex);
            ColumnMetaData colMetaData = columnMetadataMap.getColumnMetaData(colName);
            if (colMetaData == null) return defaultValue;
            Integer iVal = colMetaData.getMapping().getIndex(value);
            return iVal == null ? defaultValue : iVal.doubleValue();
        }

    }

    public DataColumnDoubleAdapter getDataColumnDoubleAdapter(IAttribute attr, double defaultValue) {
        ColumnMetaData colMetaData = null;
        DoubleColumn colNum = null;
        StringColumn colStr = null;
        String colName = attr != null ? attr.getName() : null;
        if (colName != null) colMetaData = columnMetadataMap.getColumnMetaData(colName);
        if (colMetaData != null) {
            if (colMetaData.isNominal()) {
                colStr = table.stringColumn(colName);

            } else {
                colNum = (DoubleColumn) table.column(colName);
            }
        }
        return new DataColumnDoubleAdapter(colMetaData, colNum, colStr, attr, defaultValue);
    }

    public void setDoubleValue(String colName, int rowIndex, double value) {
        ColumnMetaData colMetaData = columnMetadataMap.getColumnMetaData(colName);
        if (colMetaData == null) {
            throw new IllegalStateException(String.format("Column '%s' does not exist", colName));
        }

        if (colMetaData.isNominal()) {
            int iValue = (int) value;
            if (!colMetaData.getMapping().hasIndex(iValue)) {
                throw new IllegalStateException(String.format("There is no index '%d' in '%s' column mapping", iValue, colName));
            }
            StringColumn colStr = table.stringColumn(colName);
            colStr.set(rowIndex, colMetaData.getMapping().mapIndex(iValue));
        } else {
            DoubleColumn colNum = (DoubleColumn) table.column(colName);
            colNum.set(rowIndex, value);
        }
    }


    public Object[] getValues(String colName) {
        ColumnMetaData cmd = getColumn(colName);

        if (cmd == null) {
            throw new IllegalStateException(String.format("Column '%s' does not exist", colName));
        }

        if (cmd.isNumerical()) {
            return table.doubleColumn(colName).asObjectArray();
        }

        if (cmd.isNominal()) {
            return table.stringColumn(colName).asObjectArray();
        }

        throw new IllegalStateException(String.format("Column '%s' is neither numerical nor nominal", colName));
    }

    private Selection addCondition(List<ICondition> conditions, int conditionIndex, EConditionsLogicOperator logicOp) {

        ICondition condition = conditions.get(conditionIndex);
        if (conditionIndex == conditions.size() - 1) {
            return condition.createSelection(table);
        } else {
            Selection selection = addCondition(conditions, conditionIndex + 1, logicOp);
            return selection.or(condition.createSelection(table));
        }
    }


    @Override
    public IAttributes getAttributes() {
        return columnMetadataMap;
    }

    @Override
    public int size() {
        return table.rowCount();
    }

    @Override
    public Example getExample(int var1) {
        return new Example(new DataRow(this, var1), this);
    }

    @Override
    public IExampleSet filter(ICondition cnd) {
        return new DataTable(table.where(cnd.createSelection(table)), columnMetadataMap, dataTableAnnotations);
    }

    @Override
    public IExampleSet filterWithOr(List<ICondition> cndList) {
        if (cndList.isEmpty()) {
            return null;
        }

        Selection sel = addCondition(cndList, 0, EConditionsLogicOperator.OR);
        return new DataTable(table.where(sel), columnMetadataMap, dataTableAnnotations);
    }

    @Override
    public IExampleSet updateMapping(IExampleSet mappingSource) {
        IAttributes metaDataTable = mappingSource.getAttributes();

        DataTable resDataTable = this.clone();
        resDataTable.updateMapping(metaDataTable);

        return resDataTable;
    }


    @Override
    public DataTableAnnotations getAnnotations() {
        return dataTableAnnotations;
    }

    @NotNull
    @Override
    public Iterator<Example> iterator() {
        return new ExampleIterator(this);
    }

    @Override
    public int addAttribute(IAttribute var1) {
        addNewColumn(var1);
        return getColumnIndex(var1.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DataTable)) {
            return false;
        }
        DataTable dt = (DataTable) o;
        if (!dt.table.equals(table)) {
            return false;
        }
        return dt.columnMetadataMap.equals(columnMetadataMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, columnMetadataMap);
    }


    private void writeObject(ObjectOutputStream oos) throws IOException {
        Object[] data = new Object[3];
        data[0] = table.write().toString("csv");
        data[1] = columnMetadataMap;
        data[2] = dataTableAnnotations;
        oos.writeObject(data);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        Object[] data = (Object[]) ois.readObject();
        table = Table.read().string((String) data[0], "csv");
        columnMetadataMap = (ColumnMetadataMap) data[1];
        dataTableAnnotations = (DataTableAnnotations) data[2];
    }
}
