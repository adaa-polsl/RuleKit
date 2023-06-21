package utils;

import adaa.analytics.rules.logic.representation.SurvivalRule;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.nio.CSVExampleSource;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.PlatformUtilities;
import com.rapidminer5.operator.io.ArffExampleSource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class ExamplesetFileLoader {
    protected  abstract  ExampleSet loadExampleSet(String filePath, String labelParameterName, String survivalTimeParameter) throws OperatorCreationException, OperatorException;

    public ExampleSet load(String filePath, String labelParameterName, String survivalTimeParameter) throws OperatorException, OperatorCreationException {
        return loadExampleSet(filePath, labelParameterName, survivalTimeParameter);
    }

    public ExampleSet load(String filePath, String labelParameterName) throws OperatorCreationException, OperatorException {
        return loadExampleSet(filePath, labelParameterName, null);
    }

    public  ExampleSet load(Path filePath, String labelParameterName) throws OperatorException, OperatorCreationException {
        return loadExampleSet(filePath.toString(), labelParameterName, null);
    }
}
