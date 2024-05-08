package ioutils;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.IExampleSet;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.File;

public class CsvFileLoader extends TableSawLoader {

    @Override
    protected DataTable loadDataTable(String path, String labelParameterName, String survivalTimeParameter) {

        return loadDataTable(path, labelParameterName, survivalTimeParameter);
    }
}
