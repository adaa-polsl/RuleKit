package adaa.analytics.rules;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExitMode;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.OperatorService;

import adaa.analytics.rules.logic.induction.ActionFinder;
import adaa.analytics.rules.logic.induction.ActionInductionParameters;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.BackwardActionSnC;
import adaa.analytics.rules.logic.induction.ClassPair;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.RuleSerializer;

@RunWith(Parameterized.class)
public class ActionTests {
	protected static String testDirectory =  "C:/Users/pmatyszok/Desktop/dane/";
	private final String outputExtension = ".rules";
	
	protected ActionInductionParameters params;
	protected String testFile;
	protected String outputFileName;
	protected com.rapidminer.Process process;
	protected ExampleSet exampleSet;
	protected String labelParameter;
	protected int sourceId;
	protected int targetId;
	protected String sourceClass;
	protected String targetClass;
	
	@Parameters
	public static Collection<Object[]> testData(){
		return Arrays.asList(new Object[][]{
			//fileName, labelName, measure, pruningEnabled, ignoreMissing, minCov, maxUncov, maxGrowing, sourceID, targetID
	
			///
			/// car - reduced : only two classes
			///
			
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			
			///
			///	car - 4 classes
			///
			{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "unacc", "acc"},
			{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9,  "unacc", "acc"},
			
			////
			////  Wine dataset
			////
			
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "1", "2"},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "1", "2"},
			
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "2"},
			
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "1", "2"},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "1", "2"},
			
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "1", "2"},
			
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "1", "2"},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "1", "2"},
			
			///
			/// Monks 1 dataset
			///
			
			{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "0", "1"},
			{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "0", "1"},
			
			{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "0", "1"},
		
			{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "0", "1"},
			{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "0", "1"},
			{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "0", "1"},
			{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "0", "1"},
			{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "0", "1"},
			/*
			////
			////  Sonar dataset
			////
			/*{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9},
			*/
		});
	}
	
	private String getOutputFileName() {
		return outputFileName + outputExtension;
	}
	
	public ActionTests(String testFileName, String labelParameterName,
			ClassificationMeasure measure,
			boolean enablePruning, boolean ignoreMissing, double minimumCovered,
			double maximumUncoveredFraction, double maxGrowingConditions,
			String sourceClass, String targetClass) {
		testFile = testFileName;
		labelParameter = labelParameterName;
		
		outputFileName = testFileName.substring(0, testFileName.indexOf('.'));
		outputFileName += "-rules-" + measure.getName() + (enablePruning  ? "-pruned" : "");
		
		params = new ActionInductionParameters();
		params.setInductionMeasure(measure);
		params.setPruningMeasure(measure);
		params.setEnablePruning(enablePruning);
		params.setIgnoreMissing(ignoreMissing);
		params.setMinimumCovered(minimumCovered);
		params.setMaximumUncoveredFraction(maximumUncoveredFraction);
		params.setMaxGrowingConditions(maxGrowingConditions);
		
		params.addClasswiseTransition(sourceClass, targetClass);
		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RapidMiner.init();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RapidMiner.quit(ExitMode.NORMAL);
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
		
		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
		ActionRuleSet actions = (ActionRuleSet)snc.run(exampleSet);
	/*
		AbstractSeparateAndConquer snc = new ClassificationSnC(new ClassificationFinder(params), params);
		RuleSetBase set = snc.run(exampleSet);
		System.out.println(set.toString());
		*/
		dumpData(exampleSet, actions);
		
	}
	
	@Test
	public void testBackwardRules() throws OperatorCreationException, OperatorException, IOException {
		ExampleSet exampleSet = parseArffFile();
		
		params.reverseTransitions();
		
		BackwardActionSnC snc = new BackwardActionSnC(new ActionFinder(params), params);
		ActionRuleSet actions = (ActionRuleSet)snc.run(exampleSet);
		
		this.outputFileName += "-backward";
		dumpData(exampleSet, actions);
	}

	protected void dumpData(ExampleSet exampleSet, ActionRuleSet actions) throws IOException {
		File arffFile = Paths.get(testDirectory, getOutputFileName()).toFile();
		
		
		
		Long actionsCount = actions.getRules().stream().map(z -> (ActionRule)z).
				mapToLong(x -> x.getPremise()
								.getSubconditions()
								.stream()
								.map(y -> (Action)y)
								.mapToLong(v -> (v.getActionNil() || v.isLeftEqualRight()) ? 0L : 1L)
								.sum()
						 ).sum();
		
		int conditionCount = actions.calculateConditionsCount();
		
		FileWriter fw = new FileWriter(arffFile);
		fw.write("File name: " + testFile + "\r\n");
		
		List<ClassPair> pairs = params.generateClassPairs(exampleSet.getAttributes().getLabel().getMapping());
		fw.write("Transitions generated: \r\n");
		for (ClassPair pair : pairs) {
			fw.write(pair.getSourceLabel() + " -> " + pair.getTargetLabel() + "\r\n");
		}
		
		fw.write("Mincov: " + params.getMinimumCovered() + "\r\n");
		fw.write("Maximum uncovered fraction: " + params.getMaximumUncoveredFraction() + "\r\n");
		fw.write("Max growing: " + params.getMaxGrowingConditions() + "\r\n");
		fw.write("Induction measure used: " + ((ClassificationMeasure)params.getInductionMeasure()).getName() + "\r\n");
		fw.write("Pruning measure used: " + ((ClassificationMeasure)params.getPruningMeasure()).getName() + "\r\n");
		fw.write("Ruleset size: " + actions.getRules().size() + "\r\n");
		fw.write("Pruning: " + params.isPruningEnabled() + "\r\n");
		fw.write("Conditions count: " + conditionCount + "\r\n");
		fw.write("Actions count: " + actionsCount + "\r\n");
		fw.write("Average actions per rule: " + (double)actionsCount / (double)actions.getRules().size() + "\r\n");
		fw.write("Average conditions per rule: " + (double)conditionCount / (double)actions.getRules().size() + "\r\n");
		fw.write(actions.toString() + "\r\n");
		
		RuleSerializer serializer = new RuleSerializer(exampleSet, ';', "");
		fw.write(serializer.serializeToCsv(actions));
		
		
		fw.close();
		
		
		
		
		System.out.println("File name: " + testFile);
		System.out.println("Pruning: " + params.isPruningEnabled());
		//System.out.println("Loosed actions count" + loosedActionsCount);
	//	System.out.println("Measure: " + ((ClassificationMeasure)params.getPruningMeasure()).getName(params.getPruningMeasure()));
		System.out.println(actions.toString());
	}

}
