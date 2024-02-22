package utils;

import adaa.analytics.rules.rm.example.IExampleSet;

import java.nio.file.Path;

public abstract class ExamplesetFileLoader {
    protected  abstract  IExampleSet loadExampleSet(String path, String labelParameterName, String survivalTimeParameter);

    public IExampleSet load(String data, String labelParameterName, String survivalTimeParameter) {
        return loadExampleSet(data, labelParameterName, survivalTimeParameter);
    }

    public IExampleSet load(String data, String labelParameterName) {
        return loadExampleSet(data, labelParameterName, null);
    }

    public IExampleSet load(Path path, String labelParameterName, String survivalTimeParameter) {
        return loadExampleSet(path.toString(), labelParameterName, survivalTimeParameter);
    }

    public  IExampleSet load(Path path, String labelParameterName) {
        return loadExampleSet(path.toString(), labelParameterName, null);
    }
}
