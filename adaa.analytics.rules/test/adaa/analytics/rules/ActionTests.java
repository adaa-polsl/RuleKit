package adaa.analytics.rules;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.Test;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.OperatorService;

import adaa.analytics.rules.logic.induction.AbstractSeparateAndConquer;
import adaa.analytics.rules.logic.induction.ActionFinder;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.ClassificationFinder;
import adaa.analytics.rules.logic.induction.ClassificationSnC;
import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;

@RunWith(Parameterized.class)
public class ActionTests {
	protected static String testDirectory =  "C:/Users/pmatyszok/Desktop/dane/";
	
	protected InductionParameters params;
	protected String testFile;
	protected String outputFileName;
	protected com.rapidminer.Process process;
	protected ExampleSet exampleSet;
	protected String labelParameter;
	
	@Parameters
	public static Collection<Object[]> testData(){
		return Arrays.asList(new Object[][]{
			//fileName, labelName, measure, pruningEnabled, ignoreMissing, minCov, maxUncov, maxGrowing
		
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Accuracy), true, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Accuracy), false, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9},
			
			////
			////  Wine dataset
			////
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9},
			
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Accuracy), true, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Accuracy), false, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9},
			////
			////  Sonar dataset
			////
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Accuracy), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Accuracy), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9},
			
		});
	}
	
	public ActionTests(String testFileName, String labelParameterName,
			ClassificationMeasure measure,
			boolean enablePruning, boolean ignoreMissing, double minimumCovered,
			double maximumUncoveredFraction, double maxGrowingConditions) {
		testFile = testFileName;
		labelParameter = labelParameterName;
		
		outputFileName = testFileName.substring(0, testFileName.indexOf('.'));
		outputFileName += "-rules-" + measure.getName() + (enablePruning  ? "-pruned" : "")  + ".arff";
		
		params = new InductionParameters();
		params.setInductionMeasure(measure);
		params.setPruningMeasure(measure);
		params.setEnablePruning(enablePruning);
		params.setIgnoreMissing(ignoreMissing);
		params.setMinimumCovered(minimumCovered);
		params.setMaximumUncoveredFraction(maximumUncoveredFraction);
		params.setMaxGrowingConditions(maxGrowingConditions);
		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RapidMiner.init();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	protected ExampleSet parseArffFile() throws OperatorException, OperatorCreationException {
		ArffExampleSource arffSource = (ArffExampleSource)OperatorService.createOperator(ArffExampleSource.class);
		//role setter allows for deciding which attribute is class attribute
		ChangeAttributeRole roleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
		
		File arffFile = Paths.get(testDirectory, testFile).toFile();
		
		arffSource.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, arffFile.getAbsolutePath());
		roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelParameter);
    	roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
		
    	process = new com.rapidminer.Process();
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
	
	@Test
	public void test() throws OperatorCreationException, OperatorException, IOException {
		
		ExampleSet exampleSet = parseArffFile();
		
		AbstractSeparateAndConquer snc = new ActionSnC(new ActionFinder(params), params);
		ActionRuleSet actions = (ActionRuleSet)snc.run(exampleSet);
		
		//AbstractSeparateAndConquer snc = new ClassificationSnC(new ClassificationFinder(params), params);
		//RuleSetBase set = snc.run(exampleSet);
		//System.out.println(set.toString());
		File arffFile = Paths.get(testDirectory, this.outputFileName).toFile();
		
		Long loosedActionsCount = actions.getRules().stream().map(x -> (ActionRule)x).
			mapToLong(x -> x.getPremise().
							getSubconditions().
							stream().
							map(y -> (Action)y).mapToLong(z -> (z.getActionNil() ? 1L : 0L)).sum()).sum();
		
		FileWriter fw = new FileWriter(arffFile);
		fw.write("File name: " + testFile + "\r\n");
		fw.write("Pruning: " + params.isPruningEnabled() + "\r\n");
		fw.write("Loosed actions count" + loosedActionsCount + "\r\n");
		fw.write(actions.toString());
		fw.close();
		
		
		System.out.println("File name: " + testFile);
		System.out.println("Pruning: " + params.isPruningEnabled());
		//System.out.println("Loosed actions count" + loosedActionsCount);
	//	System.out.println("Measure: " + ((ClassificationMeasure)params.getPruningMeasure()).getName(params.getPruningMeasure()));
		System.out.println(actions.toString());
	}

}
