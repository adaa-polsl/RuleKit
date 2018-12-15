package adaa.analytics.rules;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExitMode;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import adaa.analytics.rules.logic.induction.ActionFinder;
import adaa.analytics.rules.logic.induction.ActionFindingParameters;
import adaa.analytics.rules.logic.induction.ActionFindingParameters.RangeUsageStrategy;
import adaa.analytics.rules.logic.induction.ActionInductionParameters;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.BackwardActionSnC;
import adaa.analytics.rules.logic.induction.ClassPair;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.AnyValueSet;
import adaa.analytics.rules.logic.representation.CompressedCompoundCondition;
import adaa.analytics.rules.logic.representation.RuleSerializer;
import utils.ArffFileLoader;

@RunWith(Parameterized.class)
public class ActionTests {
	protected static String testDirectory =  "C:/Users/pmatyszok/desktop/action-rules/datasets/mixed";
	private final String outputExtension = ".rules";
	protected StopWatch stopwatch;
	protected ActionRuleSet actions;
	
	
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
	protected ActionRuleSet unprunedRules;
	protected boolean dumpUnprunedRules;
	
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
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
		
		outputFileName = testFileName.substring(0, testFileName.indexOf('.'));
		outputFileName += "-rules-" + measure.getName() + (enablePruning  ? "-pruned" : "") 
				+ "-" + (sourceClass.equals("*") ? "ALL" : sourceClass) + "-to-" + targetClass;
		
		ActionFindingParameters findingParams = new ActionFindingParameters();
		findingParams.setUseNotIntersectingRangesOnly(RangeUsageStrategy.EXCLUSIVE_ONLY);
		
		params = new ActionInductionParameters(findingParams);
		params.setInductionMeasure(measure);
		params.setPruningMeasure(measure);
		params.setEnablePruning(enablePruning);
		params.setIgnoreMissing(ignoreMissing);
		params.setMinimumCovered(minimumCovered);
		params.setMaximumUncoveredFraction(maximumUncoveredFraction);
		params.setMaxGrowingConditions(maxGrowingConditions);
		
		params.addClasswiseTransition(sourceClass, targetClass);
		
		dumpUnprunedRules = true;
		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RapidMiner.init();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RapidMiner.quit(ExitMode.NORMAL);
	}
	
	@Before
	public void beforeTest() throws OperatorException, OperatorCreationException {
		exampleSet = parseArffFile();
		stopwatch = new StopWatch();
		stopwatch.start();
		actions = null;
	}

	@After
	public void afterTest() throws IOException {
		stopwatch.stop();
		dumpData(exampleSet, actions);
	}
	
	@Test
	public void test() throws Exception {
		
		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
		actions = (ActionRuleSet)snc.run(exampleSet);
		
		if (params.isPruningEnabled()) {
			unprunedRules = snc.getUnprunedRules();
		}
			
	}
	
	@Test
	public void testBackwardRules2() throws Exception {
		
		BackwardActionSnC snc = new BackwardActionSnC(new ActionFinder(params), params);

		actions = (ActionRuleSet)snc.run2(exampleSet);

		this.outputFileName += "-backward2";
		if (params.isPruningEnabled()) {
			unprunedRules = snc.getUnprunedRules();
		}
	}
	
	@Test
	public void testBackwardRules() throws Exception {		
		
		BackwardActionSnC snc = new BackwardActionSnC(new ActionFinder(params), params);
		actions = (ActionRuleSet)snc.run(exampleSet);
		
		this.outputFileName += "-backward";
	
		if (params.isPruningEnabled()) {
			unprunedRules = snc.getUnprunedRules();
		}
	}

	protected ExampleSet parseArffFile() throws OperatorException, OperatorCreationException {
		return ArffFileLoader.load(Paths.get(testDirectory, testFile), labelParameter);
	}

	protected void dumpData(ExampleSet exampleSet, ActionRuleSet actions) throws IOException {
		File arffFile = Paths.get(testDirectory, getOutputFileName()).toFile();
		
		List<CompressedCompoundCondition> premises = new ArrayList<CompressedCompoundCondition>();
		
		actions.getRules().stream().forEach(x -> premises.add(new CompressedCompoundCondition(x.getPremise())));
		
		Long actionsCount = premises.stream().
				mapToLong(x -> x.getSubconditions()
								.stream()
								.map(y -> (Action)y)
								.mapToLong(v -> (v.getActionNil() || v.isLeftEqualRight()) ? 0L : 1L)
								.sum()
						 ).sum();
		
		Long conditionCount = premises
				.stream()
				.mapToLong(x -> x.getSubconditions()
						.stream()
						.map(Action.class::cast)
						.mapToLong(v -> (v.getActionNil() || v.isLeftEqualRight()) ? 1L : 0L)
						.sum()
						).sum();
						
		conditionCount += actionsCount;

		int anyActionCount = 0;
		int onlyAny = 0;
		for (CompressedCompoundCondition p : premises) {
			int actionCount = p.getSubconditions().stream()
				.map(Action.class::cast)
				.mapToInt(x -> x.getLeftValue() instanceof AnyValueSet ? 1 : 0)
				.sum();
			
			if (actionCount == p.getSubconditions().size()) {
				onlyAny++;
			}
			
			anyActionCount += actionCount;
		}
		
		FileWriter fw = new FileWriter(arffFile);
		fw.write("File name: " + testFile + "\r\n");
		
		List<ClassPair> pairs = params.generateClassPairs(exampleSet.getAttributes().getLabel().getMapping());
		fw.write("Transitions generated: \r\n");
		for (ClassPair pair : pairs) {
			fw.write(pair.getSourceLabel() + " -> " + pair.getTargetLabel() + "\r\n");
		}
		fw.write("Time taken in ms: " + stopwatch.getTime() + "\r\n");
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
		fw.write("\"Any\" actions count: " + anyActionCount + "\r\n");
		fw.write("\"Any\" actions average" + (double)anyActionCount / (double)actions.getRules().size() + "\r\n");
		fw.write("Rules with only \"ANY\" actions count: " + onlyAny + "\r\n");
		fw.write(actions.toString() + "\r\n");
		
		RuleSerializer serializer = new RuleSerializer(exampleSet, ';', "");
		fw.write(serializer.serializeToCsv(actions));
		
		if (params.isPruningEnabled() && dumpUnprunedRules) {
	
			fw.write("\r\n\r\n");
			fw.write("******UNPRUNED RULES******");
			fw.write(this.unprunedRules.toString() + "\r\n");
			serializer = new RuleSerializer(exampleSet, ';', "");
			fw.write(serializer.serializeToCsv(unprunedRules));
		}
		
		fw.close();
		
		System.out.println("File name: " + testFile);
		System.out.println("Pruning: " + params.isPruningEnabled());
		System.out.println(actions.toString());
	}

	@Parameters
	public static Collection<Object[]> testData(){
		return Arrays.asList(new Object[][]{
			//fileName, labelName, measure, pruningEnabled, ignoreMissing, minCov, maxUncov, maxGrowing, sourceID, targetID
	
			/// furnace control
			///
			///
			
			{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "3", "4"},
			{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "3", "4"},
			{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "5", "4"},
			{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "5", "4"},
			{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "4", "4"},
			{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "4", "4"},
			{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "*", "4"},
			{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "*", "4"}/*,
			
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
			
			
			///
			///
			/// BENCHMARK DATA SETS 
			///
			
			{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events"},
			{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events"},
			{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events"},
			{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events"},
			{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events"},
			{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events"},
			{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events"},
			{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events"},
			
			
			{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "-", "+"},
			{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "-", "+"},
			{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "-", "+"},
			{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "-", "+"},
			{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "-", "+"},
			{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "-", "+"},
			{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "-", "+"},
			{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "-", "+"},
			
			{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "bad", "good"},
			
			{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative"},
			{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative"},
			{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative"},
			{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative"},
			{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative"},
			{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative"},
			{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative"},
			{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative"},
			
			{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "present", "absent"},
			{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "present", "absent"},
			{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "present", "absent"},
			{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "present", "absent"},
			{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "present", "absent"},
			{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "present", "absent"},
			{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "present", "absent"},
			{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "present", "absent"},
			
			{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "die", "live"},
			{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "die", "live"},
			{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "die", "live"},
			{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "die", "live"},
			{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "die", "live"},
			{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "die", "live"},
			{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "die", "live"},
			{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "die", "live"},
			
			{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "yes", "no"},
			{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "yes", "no"},
			{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "yes", "no"},
			{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "yes", "no"},
			{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "yes", "no"},
			{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "yes", "no"},
			{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "yes", "no"},
			{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "yes", "no"},
			
			{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "1", "0"},
			{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "1", "0"},
			{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "1", "0"},
			{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "1", "0"},
			{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "0"},
			{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "1", "0"},
			{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "1", "0"},
			{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "1", "0"},
			
			{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "nowin", "won"},
			{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "nowin", "won"},
			{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "nowin", "won"},
			{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "nowin", "won"},
			{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "nowin", "won"},
			{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "nowin", "won"},
			{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "nowin", "won"},
			{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "nowin", "won"},
			
			{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "bad", "good"},
			{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "bad", "good"},
			
			{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "0", "1"},
			{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "0", "1"},
			{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "0", "1"},
			{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "0", "1"},
			{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "0", "1"},
			{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "0", "1"},
			{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "0", "1"},
			{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "0", "1"},
			
			{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "1", "0"},
			{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "1", "0"},
			{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "1", "0"},
			{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "1", "0"},
			{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "0"},
			{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "1", "0"},
			{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "1", "0"},
			{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "1", "0"},
			
			{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "sick", "negative"},
			{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "sick", "negative"},
			{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "sick", "negative"},
			{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "sick", "negative"},
			{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "sick", "negative"},
			{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "sick", "negative"},
			{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "sick", "negative"},
			{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "sick", "negative"},
			
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "no", "yes"},
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "no", "yes"},
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "no", "yes"},
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "no", "yes"},
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "no", "yes"},
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "no", "yes"},
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "no", "yes"},
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "no", "yes"},
			
			{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "democrat", "republican"},
			{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "democrat", "republican"},
			{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "democrat", "republican"},
			{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "democrat", "republican"},
			{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "democrat", "republican"},
			{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "democrat", "republican"},
			{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "democrat", "republican"},
			{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "democrat", "republican"},
		*/
		});
	}

}
