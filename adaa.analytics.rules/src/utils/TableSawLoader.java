package utils;

import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.SimpleExampleSet;
import adaa.analytics.rules.rm.example.table.*;
import org.jetbrains.annotations.NotNull;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.util.ArrayList;
import java.util.List;

public abstract class TableSawLoader extends ExamplesetFileLoader {

    private static final DataRowFactory dataRowFactory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY);

    protected IExampleSet loadExampleSet(
            CsvReadOptions.Builder builder,
            String labelParameterName,
            String survivalTimeParameter
    ) {
        return loadExampleSet(builder, labelParameterName, survivalTimeParameter, null);
    }

    protected IExampleSet loadExampleSet(
            CsvReadOptions.Builder builder,
            String labelParameterName,
            String survivalTimeParameter,
            List<AttributeInfo> attributesInfo
    ) {

        builder = builder
                .separator(',')
                .quoteChar('\'')
                .missingValueIndicator("?");

        ColumnType[] columnTypes = null;

        if(attributesInfo != null) {

            columnTypes = new ColumnType[attributesInfo.size()];
            for(int i=0 ; i<attributesInfo.size() ; i++) {

                if(attributesInfo.get(i).getCellType() == EColType.NUMERIC) {
                    columnTypes[i] = ColumnType.DOUBLE;
                }
                else if(attributesInfo.get(i).getCellType() == EColType.TEXT) {
                    columnTypes[i] = ColumnType.STRING;
                }
                else if(attributesInfo.get(i).getCellType() == EColType.DATE) {
                    columnTypes[i] = ColumnType.LOCAL_DATE_TIME;
                }
                else {
                    columnTypes[i] = ColumnType.STRING;
                }
            }
            builder = builder.columnTypes(columnTypes);
        }

        CsvReadOptions options = builder.build();

        Table tsTable = Table.read().usingOptions(options);

        if(attributesInfo != null) {

            if(attributesInfo.size() != tsTable.columnCount()) {

                throw new IllegalStateException("Niezgodna liczba atrybutów");
            }
            for(int i=0 ; i<attributesInfo.size() ; i++) {

                tsTable.column(i).setName(attributesInfo.get(i).getName());
            }
        }
        else {

            attributesInfo = new ArrayList<>(tsTable.columnCount());
            for(Column<?> col : tsTable.columnArray()) {

                attributesInfo.add(new AttributeInfo(col.name(), getCellType(col.type()), col.unique().asList()));
            }
        }

        List<IAttribute> attributes = new ArrayList<>(attributesInfo.size());
        for(AttributeInfo attributeInfo : attributesInfo) {

            IAttribute attribute = null;

            if(attributeInfo.getCellType() == EColType.TEXT) {

                int uniqueDataSize = 0;
                if(attributeInfo.getValues() != null) {
                    uniqueDataSize = attributeInfo.getValues().length;
                }

                if(uniqueDataSize <= 2) {
                    attribute = new BinominalAttribute(attributeInfo.getName());
                    BinominalMapping biMapping = new BinominalMapping();
                    for(String value : attributeInfo.getValues()){
                        biMapping.mapString(value);
                    }
                    attribute.setMapping(biMapping);
                }
                else {
                    attribute = new PolynominalAttribute(attributeInfo.getName());
                    PolynominalMapping polyMapping = new PolynominalMapping();
                    for(String value : attributeInfo.getValues()){
                        polyMapping.mapString(value);
                    }
                    attribute.setMapping(polyMapping);
                }
            }
            else if(attributeInfo.getCellType() == EColType.NUMERIC) {

                attribute = new NumericalAttribute(attributeInfo.getName());
            }
            else if(attributeInfo.getCellType() == EColType.DATE) {

                attribute = new NumericalAttribute(attributeInfo.getName());
            }
            if(attribute != null){
                attributes.add(attribute);
            }
        }
        IAttribute[] attributeArray = attributes.toArray(new IAttribute[attributes.size()]);

        MemoryExampleTable meTable = new MemoryExampleTable(attributes);

        for(Row row : tsTable) {

            DataRow dataRow = rowToDataRow(row, attributeArray);
            meTable.addDataRow(dataRow);
        }

        SimpleExampleSet exampleSet = new SimpleExampleSet(meTable);

        exampleSet.getAttributes().setLabel(exampleSet.getAttributes().get(labelParameterName));
        exampleSet.getAttributes().setSpecialAttribute(exampleSet.getAttributes().get(survivalTimeParameter), SurvivalRule.SURVIVAL_TIME_ROLE);

        return exampleSet;
    }

    protected EColType getCellType(@NotNull ColumnType type) {

        if (type.equals(ColumnType.INTEGER) ||
                type.equals(ColumnType.FLOAT) ||
                type.equals(ColumnType.DOUBLE) ||
                type.equals(ColumnType.SHORT) ||
                type.equals(ColumnType.LONG) ||
                type.equals(ColumnType.BOOLEAN)) {
            return EColType.NUMERIC;
        }
        else if (type.equals(ColumnType.STRING) ||
                type.equals(ColumnType.TEXT)) {
            return EColType.TEXT;
        }
        else if (type.equals(ColumnType.LOCAL_DATE) ||
                type.equals(ColumnType.LOCAL_DATE_TIME) ||
                type.equals(ColumnType.LOCAL_TIME)) {
            return EColType.DATE;
        }
        return EColType.UNKNOWN;
    }

    protected String [] rowToStringArray(@NotNull Row row, @NotNull IAttribute[] attributes) {

        String [] stringRow = new String[row.columnCount()];

        for(int i=0 ; i<attributes.length ; i++) {

            String colName = attributes[i].getName();
            if(getCellType(row.getColumnType(colName)) == EColType.TEXT) {

                stringRow[i] = row.getString(colName);
            }
            else if(getCellType(row.getColumnType(colName)) == EColType.NUMERIC) {

                stringRow[i] = Double.toString(row.getNumber(colName));
            }
            else if(getCellType(row.getColumnType(colName)) == EColType.DATE) {

                stringRow[i] = Double.toString(row.getPackedDateTime(colName));
            }
        }

        return stringRow;
    }

    protected DataRow rowToDataRow(Row row, IAttribute[] attributes) {

        String [] strRow = rowToStringArray(row, attributes);

        return dataRowFactory.create(strRow, attributes);
    }
}
