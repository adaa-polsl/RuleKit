package utils;

import adaa.analytics.rules.utils.RapidMiner5;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer5.operator.io.ArffExampleSetWriter;

public class ArffFileWriter {
    public static void write(ExampleSet exampleSet, String fileName) throws OperatorCreationException, OperatorException {
        ArffExampleSetWriter writer = RapidMiner5.createOperator(ArffExampleSetWriter.class);
        writer.setParameter(ArffExampleSetWriter.PARAMETER_EXAMPLE_SET_FILE, fileName);
        writer.write(exampleSet);
    }
}
