package adaa.analytics.rules.data;

import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.*;
import adaa.analytics.rules.data.row.EmptyDoubleColumn;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.row.ExampleIterator;
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
    private ColumnMetadataMap columnMetadataMap = new ColumnMetadataMap();
    private DataTableAnnotations dataTableAnnotations = new DataTableAnnotations();

    //local cache to optimize access to column idx
    private Map<String, Integer> columnIdxMap = new HashMap<>();

    public DataTable(Table table, ColumnMetadataMap columnMetadataMap, DataTableAnnotations dataTableAnnotations) {
        this.table = table;
        this.columnMetadataMap = columnMetadataMap;
        this.dataTableAnnotations = dataTableAnnotations;
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
            columnMetadataMap.add( new ColumnMetaData(attInfo.getName(), attInfo.getCellType(), EColumnRole.regular, attInfo.getValues(), this));
        }

//        convertStringColsToDouble();
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
            EColumnRole colRole = EColumnRole.regular;
            if (attName.equals(decisionAttribute)) {
                colRole = EColumnRole.label;
            } else if (attName.equals(survivalTimeAttribute)) {
                colRole = EColumnRole.survival_time;
            } else if (attName.equals(contrastAttribute)) {
                colRole = EColumnRole.contrast_attribute;
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
            columnMetadataMap.add( colMetaData);
        }

        convertStringColsToDouble();
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
        ColumnMetaData newColMetaData = ((ColumnMetaData) colMetaData);
        newColMetaData.setOwner(this);
        Column<?> tsCol = null;

//        if (newColMetaData.getColumnType() == EColumnType.NOMINAL) {
//            tsCol = StringColumn.create(newColMetaData.getName(), table.rowCount());
//        } else if (newColMetaData.getColumnType() == EColumnType.NUMERICAL) {
            tsCol = DoubleColumn.create(newColMetaData.getName(), table.rowCount());
//        }

//        if (tsCol == null) {
//            throw new IllegalStateException("Cannot crate prediction column, unknown column type");
//        }

        table.addColumns(tsCol);
        columnMetadataMap.add(newColMetaData);
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
        customSort(columnName, sortDir, ESortAlgorithm.QuickSort);
    }

    public void customSort(String columnName, EColumnSortDirections sortDir, ESortAlgorithm sortAlgorithm) {
        if(sortAlgorithm == ESortAlgorithm.NativeTableSaw) {
            table = table.sortOn((sortDir == EColumnSortDirections.DECREASING ? "-" : "") + columnName);
        }
        if (table.column(columnName) instanceof DoubleColumn) {
            if(sortAlgorithm == ESortAlgorithm.BubbleSort) {
                sortDoubleColumnBubble(columnName, sortDir);
            }
            else if(sortAlgorithm == ESortAlgorithm.QuickSort) {
                sortDoubleColumnQuick(columnName, sortDir, 0, table.rowCount() - 1);
            }
        } else {
            throw new IllegalArgumentException("Unsupported column type for custom sorting");
        }
    }

    private void sortDoubleColumnBubble(String columnName, EColumnSortDirections sortDir) {
        DoubleColumn column = (DoubleColumn) table.column(columnName);
        int n = column.size();
        boolean swapped;

        do {
            swapped = false;
            for (int i = 0; i < n - 1; i++) {
                int comparison = Double.compare(column.get(i), column.get(i + 1));
                if ((sortDir == EColumnSortDirections.INCREASING && comparison > 0) ||
                        (sortDir == EColumnSortDirections.DECREASING && comparison < 0)) {
                    swapRows(table, i, i + 1);
                    swapped = true;
                }
            }
        } while (swapped);
    }

    private void sortDoubleColumnQuick(String columnName, EColumnSortDirections sortDir, int low, int high) {
        if (low < high) {
            int pi = partitionDouble(columnName, sortDir, low, high);
            sortDoubleColumnQuick(columnName, sortDir, low, pi - 1);
            sortDoubleColumnQuick(columnName, sortDir, pi + 1, high);
        }
    }

    private int partitionDouble(String columnName, EColumnSortDirections sortDir, int low, int high) {
        DoubleColumn column = (DoubleColumn) table.column(columnName);
        double pivot = column.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            int comparison = Double.compare(column.get(j), pivot);
            if ((sortDir == EColumnSortDirections.INCREASING && comparison <= 0) || (sortDir == EColumnSortDirections.DECREASING && comparison >= 0)) {
                i++;
                swapRows(table, i, j);
            }
        }
        swapRows(table, i + 1, high);
        return i + 1;
    }

    private void swapRows(Table table, int i, int j) {
        for (int k = 0; k < table.columnCount(); k++) {
            if (table.column(k) instanceof DoubleColumn) {
                DoubleColumn column = (DoubleColumn) table.column(k);
                Double temp = column.get(i);
                column.set(i, column.get(j));
                column.set(j, temp);
            }
        }
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
            throw new IllegalStateException(String.format("Cannot calculate %s statistic for not numerical column: %s", stateType, colName));
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
        columnMetadataMap.updateMapping((ColumnMetadataMap) uColumnMetadataMap,this);
    }

    public double getDoubleValue(int colIdx, int rowIndex) {
        DoubleColumn col = table.doubleColumn(colIdx);
        return col.getDouble(rowIndex);
    }

    public DoubleColumn getDoubleColumn(IAttribute attr) {

        String colName = attr != null ? attr.getName() : null;
        if (colName != null) {
            return table.doubleColumn(getColumnIndex(colName));
        }else {
            return new EmptyDoubleColumn();
        }
    }

    public void setDoubleValue(IAttribute att, int rowIndex, double value) {
        if (att == null) {
            throw new IllegalStateException("Column not exist");
        }

        DoubleColumn col = (DoubleColumn) table.column(att.getName());
        col.set(rowIndex, value);
    }

    private Selection addCondition(List<ICondition> conditions, int conditionIndex) {

        ICondition condition = conditions.get(conditionIndex);
        if (conditionIndex == conditions.size() - 1) {
            return condition.createSelection(table);
        } else {
            Selection selection = addCondition(conditions, conditionIndex + 1);
            return selection.or(condition.createSelection(table));
        }
    }

    @Override
    public IExampleSet filter(ICondition cnd) {
        DataTable filteredDT =  new DataTable(table.where(cnd.createSelection(table)),null,null);
        filteredDT.columnMetadataMap =  columnMetadataMap.cloneWithNewOwner(filteredDT);
        filteredDT.dataTableAnnotations = dataTableAnnotations.clone();
        return filteredDT;
    }

    @Override
    public IExampleSet filterWithOr(List<ICondition> cndList) {
        if (cndList.isEmpty()) {
            return null;
        }

        Selection sel = addCondition(cndList, 0);
        DataTable filteredDT =  new DataTable(table.where(sel),null,null);
        filteredDT.columnMetadataMap =  columnMetadataMap.cloneWithNewOwner(filteredDT);
        filteredDT.dataTableAnnotations = dataTableAnnotations.clone();
        return filteredDT;
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
    public Example getExample(int rowIndex) {
        return new Example( rowIndex, this);
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

    public void convertStringColsToDouble() {

        for(ColumnMetaData colMetaData : columnMetadataMap.getAllColumnMetaData()) {
            if(!colMetaData.isNominal())
                continue;

            int cIndex = table.columnIndex(colMetaData.getName());
            StringColumn strCol = (StringColumn) table.column(colMetaData.getName());

            DoubleColumn doubleColumn = DoubleColumn.create(colMetaData.getName());
            for (String value : strCol) {
                doubleColumn.append(colMetaData.getMapping().getIndex(value));
            }

            table.removeColumns(colMetaData.getName());
            table.insertColumn(cIndex, doubleColumn);
        }
    }
}
