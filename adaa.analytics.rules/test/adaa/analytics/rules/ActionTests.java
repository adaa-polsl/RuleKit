package adaa.analytics.rules;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.model.ActionRuleSet;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExitMode;
import adaa.analytics.rules.rm.example.IExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import adaa.analytics.rules.logic.induction.ActionFindingParameters.RangeUsageStrategy;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import utils.ArffFileLoader;

@RunWith(Parameterized.class)
public class ActionTests {
	protected static String testDirectory =  "C:/Users/pawel/desktop/action-rules/datasets/mixed";
	private final String outputExtension = ".rules";
	protected StopWatch stopwatch;
	protected ActionRuleSet actions;
	
	
	protected ActionInductionParameters params;
	protected String testFile;
	protected String outputFileName;
	protected com.rapidminer.Process process;
	protected IExampleSet exampleSet;
	protected String labelParameter;
	protected int sourceId;
	protected int targetId;
	protected String sourceClass;
	protected String targetClass;
	protected ActionRuleSet unprunedRules;
	protected boolean dumpUnprunedRules;
	protected boolean isBackwardTask;
	protected ArffFileLoader arffFileLoader;

	private String getOutputFileName() {
		return outputFileName + outputExtension;
	}
	
	public ActionTests(String testFileName, String labelParameterName,
			ClassificationMeasure measure,
			boolean enablePruning, boolean b, double minimumCovered,
			double maximumUncoveredFraction, double maxGrowingConditions,
			String sourceClass, String targetClass, List<String> stableAttributes) {
		testFile = testFileName;
		labelParameter = labelParameterName;
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;

		arffFileLoader = new ArffFileLoader();

		outputFileName = testFileName.substring(0, testFileName.indexOf('.'));
		outputFileName += "-rules-" + measure.getName() + (enablePruning  ? "-pruned" : "") 
				+ "-" + (sourceClass.equals("*") ? "ALL" : sourceClass) + "-to-" + targetClass;

		ActionFindingParameters findingParams = new ActionFindingParameters();
		findingParams.setUseNotIntersectingRangesOnly(RangeUsageStrategy.ALL);
		
		params = new ActionInductionParameters(findingParams);
		params.setInductionMeasure(measure);
		params.setPruningMeasure(measure);
		params.setEnablePruning(enablePruning);
		params.setIgnoreMissing(false);
		params.setMinimumCovered(minimumCovered);
		params.setMaximumUncoveredFraction(maximumUncoveredFraction);
		params.setMaxGrowingConditions(maxGrowingConditions);
		
		params.addClasswiseTransition(sourceClass, targetClass);

		params.setStableAttributes(stableAttributes);
		
		dumpUnprunedRules = true;
		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() {
		RapidMiner.init();
	}

	@AfterClass
	public static void tearDownAfterClass()  {
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
	@Ignore
	public void test() throws Exception {
		isBackwardTask = false;
		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
		ClassificationSnC csnc = new ClassificationSnC(new ClassificationFinder(params), params);
		csnc.run(exampleSet);
        Logger.getInstance().addStream(System.out, Level.ALL);
		actions = (ActionRuleSet)snc.run(exampleSet);
		
		if (params.isPruningEnabled()) {
			unprunedRules = snc.getUnprunedRules();
		}
			
	}
	
	@Test
	@Ignore
	public void testBackwardRules2() throws Exception {
		isBackwardTask = true;
		BackwardActionSnC snc = new BackwardActionSnC(new ActionFinder(params), params);
		Logger.getInstance().addStream(System.out, Level.ALL);
		actions = (ActionRuleSet)snc.run(exampleSet);

		this.outputFileName += "-backward2";
		if (params.isPruningEnabled()) {
			unprunedRules = snc.getUnprunedRules();
		}
	}
	
	@Test
	@Ignore
	public void testBackwardRules() throws Exception {		
		isBackwardTask = true;
		BackwardActionSnC snc = new BackwardActionSnC(new ActionFinder(params), params);
		actions = (ActionRuleSet)snc.run(exampleSet);
		
		this.outputFileName += "-backward";
	
		if (params.isPruningEnabled()) {
			unprunedRules = snc.getUnprunedRules();
		}
	}

	protected IExampleSet parseArffFile() throws OperatorException, OperatorCreationException {
		return arffFileLoader.load(Paths.get(testDirectory, testFile), labelParameter);
	}

	protected void dumpData(IExampleSet exampleSet, ActionRuleSet actions) throws IOException {
		File arffFile = Paths.get(testDirectory, getOutputFileName()).toFile();
		
		List<CompressedCompoundCondition> premises = new ArrayList<>();


		actions.getRules().stream().forEach(x -> premises.add(new CompressedCompoundCondition(x.getPremise())));
		
		long actionsCount = premises.stream().
				mapToLong(x -> x.getSubconditions()
								.stream()
								.map(y -> (Action)y)
								.mapToLong(v -> (v.getActionNil() || v.isLeftEqualRight()) ? 0L : 1L)
								.sum()
						 ).sum();
		
		long conditionCount = premises
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
		fw.write("File name: " + testFile + System.lineSeparator());
		
		List<ClassPair> pairs = params.generateClassPairs(exampleSet.getAttributes().getLabel().getMapping());
		fw.write("Transitions generated: \r\n");
		for (ClassPair pair : pairs) {
			if (isBackwardTask) {
				params.reverseTransitions();
			}
			fw.write(pair.getSourceLabel() + " -> " + pair.getTargetLabel() + System.lineSeparator());
		}
		fw.write("Time taken in ms: " + stopwatch.getTime() + System.lineSeparator());
		fw.write("Stable attributes: " + params.getStableAttributes().toString() + System.lineSeparator());
		fw.write("Mincov: " + params.getMinimumCovered() + System.lineSeparator());
		fw.write("Maximum uncovered fraction: " + params.getMaximumUncoveredFraction() + System.lineSeparator());
		fw.write("Max growing: " + params.getMaxGrowingConditions() + System.lineSeparator());
		fw.write("Induction measure used: " + params.getInductionMeasure().getName() + System.lineSeparator());
		fw.write("Pruning measure used: " + params.getPruningMeasure().getName() + System.lineSeparator());
		fw.write("Ruleset size: " + actions.getRules().size() + System.lineSeparator());
		fw.write("Pruning: " + params.isPruningEnabled() + System.lineSeparator());
		fw.write("Conditions count: " + conditionCount + System.lineSeparator());
		fw.write("Actions count: " + actionsCount + System.lineSeparator());
		fw.write("Average actions per rule: " + (double)actionsCount / (double)actions.getRules().size() + System.lineSeparator());
		System.out.println((double)actionsCount / (double)actions.getRules().size());
		fw.write("Average conditions per rule: " + (double)conditionCount / (double)actions.getRules().size() + System.lineSeparator());
		fw.write("\"Any\" actions count: " + anyActionCount + System.lineSeparator());
		fw.write("\"Any\" actions average" + (double)anyActionCount / (double)actions.getRules().size() + System.lineSeparator());
		fw.write("Rules with only \"ANY\" actions count: " + onlyAny + System.lineSeparator());
		fw.write(actions.toString() + System.lineSeparator());
		
		RuleSerializer serializer = new RuleSerializer(exampleSet, ';', "");
		fw.write(serializer.serializeToCsv(actions));
		
		if (params.isPruningEnabled() && dumpUnprunedRules) {
	
			fw.write("\r\n\r\n");
			fw.write("******UNPRUNED RULES******");
			fw.write(this.unprunedRules.toString() + System.lineSeparator());
			serializer = new RuleSerializer(exampleSet, ';', "");
			fw.write(serializer.serializeToCsv(unprunedRules));
		}
		
		fw.close();
		
		System.out.println("File name: " + testFile);
		System.out.println("Pruning: " + params.isPruningEnabled());
		System.out.println(actions.toString());
	}

	public static List<String> stableAttributesForBMTCH = Arrays.asList("RecipientABO", "Riskgroup", "RecipientCMV", "Recipientageint", "Txpostrelapse", "RecipientRh", "Diseasegroup", "Recipientage", "Recipientage10", "time_to_aGvHD_III_IV", "extcGvHD", "Disease", "Relapse", "Recipientgender", "aGvHDIIIIV");
    public static List<String> stableAttributesForNursery = Arrays.asList("form", "children", "health", "has_nurs");
	@Parameters
	public static Collection<Object[]> testData(){
		return Arrays.asList(new Object[][]{
			//fileName, labelName, measure, pruningEnabled, ignoreMissing, minCov, maxUncov, maxGrowing, sourceID, targetID
		//	{"seeds.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "2"},
/*
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},

		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},
		{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "pos", "neg", stableAttributesForBMTCH},

		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-versicolor", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-versicolor", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-versicolor", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-versicolor", Collections.EMPTY_LIST},

		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-versicolor", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-versicolor", Collections.EMPTY_LIST},


		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-versicolor", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-versicolor", Collections.EMPTY_LIST},

		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-versicolor", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-versicolor", Collections.EMPTY_LIST},

		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-versicolor", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "Iris-versicolor", "Iris-virginica", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-setosa", Collections.EMPTY_LIST},
		{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "Iris-virginica", "Iris-versicolor", Collections.EMPTY_LIST},

		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "3", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "3", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "5", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "5", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "4", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "4", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "*", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "*", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9,  "3", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9,  "3", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9,  "3", "4", Collections.EMPTY_LIST},
		{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9,  "3", "4", Collections.EMPTY_LIST},


		///
		/// car - reduced : only two classes
		///

		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},


        {"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9,  "not_recom", "priority", stableAttributesForNursery},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9,  "*", "priority", stableAttributesForNursery},
/*	{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		{"nursery.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9,  "not_recom", "recommend", Collections.EMPTY_LIST},
		///
		///	car - 4 classes
		///
*/
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
		{"car.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9,  "unacc", "acc", Collections.EMPTY_LIST},
/*

		////
		////  Wine dataset
		////

		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},
		{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "1", "2", Collections.EMPTY_LIST},



                    ///
                    /// Monks 1 dataset
                    ///
                    ///

	*/	{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 10.0, "0", "1", Collections.EMPTY_LIST},
/*
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 2.0, 0.05, 1.0, "0", "1", Collections.EMPTY_LIST},

			
			///
			///
			/// BENCHMARK DATA SETS 
			///

		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},
		{"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "recurrence-events", "no-recurrence-events", Collections.EMPTY_LIST},

		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},/*
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},
		{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "-", "+", Collections.EMPTY_LIST},

		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"credit-g.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},

		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},
		{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "tested_positive", "tested_negative", Collections.EMPTY_LIST},

		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},
		{"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "present", "absent", Collections.EMPTY_LIST},

		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},
		{"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "DIE", "LIVE", Collections.EMPTY_LIST},

		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},
		{"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "yes", "no", Collections.EMPTY_LIST},

		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"hungarian-heart-disease.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},

		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},
		{"kr-vs-kp.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "nowin", "won", Collections.EMPTY_LIST},

		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},
		{"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "bad", "good", Collections.EMPTY_LIST},

		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},
		{"prnn-synth.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "0", "1", Collections.EMPTY_LIST},

		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},
		{"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "1", "0", Collections.EMPTY_LIST},

		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},
		{"sick-euthyroid.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "sick", "negative", Collections.EMPTY_LIST},

		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},
		{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "no", "yes", Collections.EMPTY_LIST},

		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), true, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), false, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), true, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
		{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), false, true, 5.0, 0.05, 0.9, "democrat", "republican", Collections.EMPTY_LIST},
	*/
		});
	}

}
