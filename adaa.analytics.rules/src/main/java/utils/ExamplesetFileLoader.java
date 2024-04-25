package utils;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.nio.file.Path;

public abstract class ExamplesetFileLoader {

    @Deprecated
    protected  abstract  IExampleSet loadExampleSet(String path, String labelParameterName, String survivalTimeParameter);

    protected  abstract  DataTable loadDataTable(String path, String labelParameterName, String survivalTimeParameter);

    @Deprecated
    public IExampleSet load(String path, String labelParameterName, String survivalTimeParameter) {
        return loadExampleSet(path, labelParameterName, survivalTimeParameter);
    }

    @Deprecated
    public IExampleSet load(String path, String labelParameterName) {
        return loadExampleSet(path, labelParameterName, null);
    }

    @Deprecated
    public IExampleSet load(Path path, String labelParameterName, String survivalTimeParameter) {
        return loadExampleSet(path.toString(), labelParameterName, survivalTimeParameter);
    }

    @Deprecated
    public  IExampleSet load(Path path, String labelParameterName) {
        return loadExampleSet(path.toString(), labelParameterName, null);
    }

    public DataTable loadAsDataTable(String path, String labelParameterName, String survivalTimeParameter) {
        return loadDataTable(path, labelParameterName, survivalTimeParameter);
    }
}
