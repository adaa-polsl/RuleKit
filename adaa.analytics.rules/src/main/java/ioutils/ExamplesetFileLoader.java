package ioutils;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.IExampleSet;

import java.nio.file.Path;

public abstract class ExamplesetFileLoader {

    protected  abstract  DataTable loadDataTable(String path, String labelParameterName, String survivalTimeParameter);

    @Deprecated
    public IExampleSet load(String path, String labelParameterName, String survivalTimeParameter) {
        return loadDataTable(path, labelParameterName, survivalTimeParameter);
    }

    @Deprecated
    public IExampleSet load(String path, String labelParameterName) {
        return loadDataTable(path, labelParameterName, null);
    }

    @Deprecated
    public IExampleSet load(Path path, String labelParameterName, String survivalTimeParameter) {
        return loadDataTable(path.toString(), labelParameterName, survivalTimeParameter);
    }

    @Deprecated
    public  IExampleSet load(Path path, String labelParameterName) {
        return loadDataTable(path.toString(), labelParameterName, null);
    }

    public DataTable loadAsDataTable(String path, String labelParameterName, String survivalTimeParameter) {
        return loadDataTable(path, labelParameterName, survivalTimeParameter);
    }
}
