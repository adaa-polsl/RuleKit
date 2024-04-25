package utils;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.EColumnRole;
import adaa.analytics.rules.data.EColumnType;
import adaa.analytics.rules.rm.comp.TsExampleSet;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import org.jetbrains.annotations.NotNull;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Row;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.util.List;

public abstract class TableSawLoader extends ExamplesetFileLoader {

//    @Deprecated
//    private static final DataRowFactory dataRowFactory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY);

    @Deprecated
    protected IExampleSet loadExampleSet(
            CsvReadOptions.Builder builder,
            String labelParameterName,
            String survivalTimeParameter
    ) {
        return loadExampleSet(builder, labelParameterName, survivalTimeParameter, null);
    }

    @Deprecated
    protected IExampleSet loadExampleSet(
            CsvReadOptions.Builder builder,
            String labelParameterName,
            String survivalTimeParameter,
            List<AttributeInfo> attributesInfo
    ) {

        DataTable dataTable = loadDataTable(builder, labelParameterName, survivalTimeParameter, attributesInfo);

        return new TsExampleSet(dataTable);

//        List<IAttribute> attributes = new ArrayList<>(attributesInfo.size());
//        for(AttributeInfo attributeInfo : attributesInfo) {
//
//            IAttribute attribute = new TsAttribute();
//
//            if(attributeInfo.getCellType() == EColumnType.TEXT) {
//
//                TsNominalMapping nominalMapping = new TsNominalMapping();
//                for(String value : attributeInfo.getValues()){
//                    nominalMapping.mapString(value);
//                }
//
//                int uniqueDataSize = 0;
//                if(attributeInfo.getValues() != null) {
//                    uniqueDataSize = attributeInfo.getValues().size();
//                }
//
//                if(uniqueDataSize <= 2) {
//                    attribute = new BinominalAttribute(attributeInfo.getName());
//                    BinominalMapping biMapping = new BinominalMapping();
//                    for(String value : attributeInfo.getValues()){
//                        biMapping.mapString(value);
//                    }
//                    attribute.setMapping(biMapping);
//                }
//                else {
//                    attribute = new PolynominalAttribute(attributeInfo.getName());
//                    PolynominalMapping polyMapping = new PolynominalMapping();
//                    for(String value : attributeInfo.getValues()){
//                        polyMapping.mapString(value);
//                    }
//                    attribute.setMapping(polyMapping);
//                }
//            }
//            else if(attributeInfo.getCellType() == EColumnType.NUMERIC) {
//
//                attribute = new NumericalAttribute(attributeInfo.getName());
//            }
//            else if(attributeInfo.getCellType() == EColumnType.DATE) {
//
//                attribute = new NumericalAttribute(attributeInfo.getName());
//            }
//            if(attribute != null){
//                attributes.add(attribute);
//            }
//        }
//        IAttribute[] attributeArray = attributes.toArray(new IAttribute[attributes.size()]);
//
//        MemoryExampleTable meTable = new MemoryExampleTable(attributes);
//
//        for(Row row : tsTable) {
//
//            DataRow dataRow = rowToDataRow(row, attributeArray);
//            meTable.addDataRow(dataRow);
//        }
//
//        SimpleExampleSet exampleSet = new SimpleExampleSet(meTable);
//
//        exampleSet.getAttributes().setLabel(exampleSet.getAttributes().get(labelParameterName));
//        exampleSet.getAttributes().setSpecialAttribute(exampleSet.getAttributes().get(survivalTimeParameter), SurvivalRule.SURVIVAL_TIME_ROLE);
//
//        return exampleSet;
    }

    protected DataTable loadDataTable(
            CsvReadOptions.Builder builder,
            String labelParameterName,
            String survivalTimeParameter,
            List<AttributeInfo> attributesInfo
    ) {
        // TableSaw CSV builder for reading
        builder = builder
                .separator(',')
                .quoteChar('\'')
                .missingValueIndicator("?");

        // Defined options for tablesaw csv loader
//        CsvReadOptions options = builder.build();

        DataTable dataTable = new DataTable(builder, attributesInfo);
        dataTable.setRole(labelParameterName, EColumnRole.label.name());
        dataTable.setRole(survivalTimeParameter, EColumnRole.survival_time.name());

//        if(attributesInfo != null) {
//
//            if(attributesInfo.size() != dataTable.columnCount()) {
//
//                throw new IllegalStateException("Niezgodna liczba atrybutów");
//            }
//            for(AttributeInfo attInfo : attributesInfo) {
//
//                EColumnRole role = EColumnRole.REGULAR;
//                if(attInfo.getName().equals(labelParameterName)) {
//                    role = EColumnRole.LABEL;
//                }
//                else if(attInfo.getName().equals(survivalTimeParameter)) {
//                    role = EColumnRole.SURVIVAL_TIME;
//                }
//                dataTable.addColumn(attInfo.getName(), attInfo.getCellType(), role, attInfo.getValues());
//            }
//        }

        return dataTable;
    }

    protected EColumnType getCellType(@NotNull ColumnType type) {

        if (type.equals(ColumnType.INTEGER) ||
                type.equals(ColumnType.FLOAT) ||
                type.equals(ColumnType.DOUBLE) ||
                type.equals(ColumnType.SHORT) ||
                type.equals(ColumnType.LONG) ||
                type.equals(ColumnType.BOOLEAN)) {
            return EColumnType.NUMERICAL;
        }
        else if (type.equals(ColumnType.STRING) ||
                type.equals(ColumnType.TEXT)) {
            return EColumnType.NOMINAL;
        }
        else if (type.equals(ColumnType.LOCAL_DATE) ||
                type.equals(ColumnType.LOCAL_DATE_TIME) ||
                type.equals(ColumnType.LOCAL_TIME)) {
            return EColumnType.DATE;
        }
        return EColumnType.OTHER;
    }

    protected String [] rowToStringArray(@NotNull Row row, @NotNull IAttribute[] attributes) {

        String [] stringRow = new String[row.columnCount()];

        for(int i=0 ; i<attributes.length ; i++) {

            String colName = attributes[i].getName();
            if(getCellType(row.getColumnType(colName)) == EColumnType.NOMINAL) {

                stringRow[i] = row.getString(colName);
            }
            else if(getCellType(row.getColumnType(colName)) == EColumnType.NUMERICAL) {

                stringRow[i] = Double.toString(row.getNumber(colName));
            }
            else if(getCellType(row.getColumnType(colName)) == EColumnType.DATE) {

                stringRow[i] = Double.toString(row.getPackedDateTime(colName));
            }
        }

        return stringRow;
    }

//    protected DataRow rowToDataRow(Row row, IAttribute[] attributes) {
//
//        String [] strRow = rowToStringArray(row, attributes);
//
//        return dataRowFactory.create(strRow, attributes);
//    }
}
