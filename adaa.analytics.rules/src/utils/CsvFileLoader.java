package utils;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.rm.example.IExampleSet;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;

public class CsvFileLoader extends TableSawLoader {

    @Override
    protected IExampleSet loadExampleSet(String path, String labelParameterName, String survivalTimeParameter) {

        CsvReadOptions.Builder builder = CsvReadOptions.builder(new File(path))
                .header(true);

        return loadExampleSet(builder, labelParameterName, survivalTimeParameter);
    }

    @Override
    protected DataTable loadDataTable(String path, String labelParameterName, String survivalTimeParameter) {

        return loadDataTable(path, labelParameterName, survivalTimeParameter);
    }
}
