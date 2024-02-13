package utils;

import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.utils.RapidMiner5;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import adaa.analytics.rules.rm.example.IExampleSet;
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

public class CsvFileLoader extends ExamplesetFileLoader {

    protected  IExampleSet loadExampleSet(String filePath, String labelParameterName, String survivalTimeParameter) throws OperatorCreationException, OperatorException {
        System.setProperty(PlatformUtilities.PROPERTY_RAPIDMINER_HOME, Paths.get("").toAbsolutePath().toString());
        LogService.getRoot().setLevel(Level.OFF);
        RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);

        RapidMiner.init();
        CSVExampleSource exampleSource = OperatorService.createOperator(CSVExampleSource.class);

        //role setter allows for deciding which attribute is class attribute
        ChangeAttributeRole roleSetter = (ChangeAttributeRole) OperatorService.createOperator(ChangeAttributeRole.class);

        File arffFile = Paths.get(filePath).toFile();

        exampleSource.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, arffFile.getAbsolutePath());
        roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelParameterName);
        roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);

        if (survivalTimeParameter != null) {
            List<String[]> roles = new ArrayList<>();
            roles.add(new String[]{survivalTimeParameter, SurvivalRule.SURVIVAL_TIME_ROLE});
            roleSetter.setListParameter(roleSetter.PARAMETER_CHANGE_ATTRIBUTES, roles);
        }

        Process process = new Process();
        process.getRootOperator().getSubprocess(0).addOperator(exampleSource);
        process.getRootOperator().getSubprocess(0).addOperator(roleSetter);

        exampleSource.getOutputPorts().getPortByName("output").connectTo(
                roleSetter.getInputPorts().getPortByName("example set input"));

        roleSetter.getOutputPorts().getPortByName("example set output").connectTo(
                process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));

        IOContainer c = process.run();

        return (IExampleSet)c.getElementAt(0);
    }

}


