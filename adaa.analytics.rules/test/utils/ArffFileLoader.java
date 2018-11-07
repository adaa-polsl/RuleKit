package utils;

import java.io.File;
import java.nio.file.Path;

import com.rapidminer.Process;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.OperatorService;
import com.rapidminer5.operator.io.ArffExampleSource;

import adaa.analytics.rules.utils.RapidMiner5;

public class ArffFileLoader {


	public static ExampleSet load(Path pathToArffFile, String labelParameterName) throws OperatorCreationException, OperatorException {
		ArffExampleSource arffSource = RapidMiner5.createOperator(ArffExampleSource.class);
		//role setter allows for deciding which attribute is class attribute
		ChangeAttributeRole roleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
		
		File arffFile = pathToArffFile.toFile();
		
		arffSource.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, arffFile.getAbsolutePath());
		roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelParameterName);
		roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
		
		Process process = new com.rapidminer.Process();
		process.getRootOperator().getSubprocess(0).addOperator(arffSource);
		process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
		
		arffSource.getOutputPorts().getPortByName("output").connectTo(
				roleSetter.getInputPorts().getPortByName("example set input"));
		
		roleSetter.getOutputPorts().getPortByName("example set output").connectTo(
				process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
		
		IOContainer c = process.run();
		//parsed arff file
		return (ExampleSet)c.getElementAt(0);
	}
}
