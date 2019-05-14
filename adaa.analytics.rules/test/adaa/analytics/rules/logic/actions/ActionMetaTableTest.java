package adaa.analytics.rules.logic.actions;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExitMode;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.set.StratifiedPartitionBuilder;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;

import adaa.analytics.rules.logic.actions.ActionMetaTable.AnalysisResult;
import adaa.analytics.rules.logic.induction.ActionFinder;
import adaa.analytics.rules.logic.induction.ActionFindingParameters;
import adaa.analytics.rules.logic.induction.ActionFindingParameters.RangeUsageStrategy;
import adaa.analytics.rules.logic.induction.ActionInductionParameters;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import utils.ArffFileLoader;
import utils.InMemoryActionRuleRepository;
import utils.InMemoryDataSet;

public class ActionMetaTableTest {

	protected InMemoryActionRuleRepository repo;
	protected ExampleSet set;
	private ActionInductionParameters actionInductionParams;
	
	
	public void prepare() {
		List<Attribute> atrs = new ArrayList<Attribute>();
		atrs.add(AttributeFactory.createAttribute("class", Ontology.NOMINAL));
		atrs.add(AttributeFactory.createAttribute("numerical1", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("numerical2", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("nominal", Ontology.NOMINAL));
		
		List<String> data = Collections.unmodifiableList(Arrays.asList(
				"1, 15.0, 150.0, a",
				"1, 13.0, 151.0, a",
				"1, 12.0, 130.3, a",
				"2, 0.0, 150.0, b",
				"2, 1.0, 150.0, b",
				"2, -1.0, 150.0, b",
				"3, 0.0, 150.0, a",
				"3, 1.0, 150.0, a",
				"3, -1.0, 150.0, b"
				));
		
		set = (new InMemoryDataSet(atrs, data)).getExampleSet();
		
		repo = new InMemoryActionRuleRepository(set);
	}
	
	public void prepareCanonical() {
		List<Attribute> atrs = new ArrayList<Attribute>();
		atrs.add(AttributeFactory.createAttribute("class", Ontology.NOMINAL));
		atrs.add(AttributeFactory.createAttribute("a", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("b", Ontology.NOMINAL));
		
		List<String> data = Collections.unmodifiableList(Arrays.asList(
				"1, 15.0, 150.0, a",
				"1, 13.0, 151.0, a",
				"1, 12.0, 130.3, a",
				"2, 0.0, 150.0, b",
				"2, 1.0, 150.0, b",
				"2, -1.0, 150.0, b",
				"3, 0.0, 150.0, a",
				"3, 1.0, 150.0, a",
				"3, -1.0, 150.0, b"
				));
		
		set = (new InMemoryDataSet(atrs, data)).getExampleSet();
		
		repo = new InMemoryActionRuleRepository(set);
	}
	
	@Test
	public void testGetExamples() {
		
		prepare();
		
		ActionRuleSet actions = repo.getActionRulest();
		
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, set);
		dist.calculateActionDistribution();
		ActionMetaTable table = new ActionMetaTable(dist);
	
		Set<MetaExample> examples = table.getMetaExamples();
		System.out.println("Count:" + examples.size());
		
		for (MetaExample ex : examples) {
			System.out.println(ex);
		}
		
	}
	
	@Test
	public void testAnalyze() {
		
		prepare();
		
		ActionRuleSet actions = repo.getActionRulest();
		actions.getRules().stream().forEach(System.out::println);
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, set);
		dist.calculateActionDistribution();
		ActionMetaTable table = new ActionMetaTable(dist);
		AnalysisResult me = table.analyze(set.getExample(0), 0, 2);
		System.out.println(me.example);
		System.out.println(me.primeMetaExample);
		System.out.println(me.contraMetaExample);
	}
	
	@Test
	public void runOnMonk() throws OperatorCreationException, OperatorException {
		ActionFindingParameters findingParams = new ActionFindingParameters();
		findingParams.setUseNotIntersectingRangesOnly(RangeUsageStrategy.EXCLUSIVE_ONLY);
		
		actionInductionParams = new ActionInductionParameters(findingParams);
		actionInductionParams.setInductionMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		actionInductionParams.setPruningMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		//true, true, 5.0, 0.05, 0.9, "0", "1"
		actionInductionParams.setEnablePruning(true);
		actionInductionParams.setIgnoreMissing(true);
		actionInductionParams.setMinimumCovered(5.0);
		actionInductionParams.setMaximumUncoveredFraction(0.05);
		actionInductionParams.setMaxGrowingConditions(0.9);
		
		actionInductionParams.addClasswiseTransition("0", "1");
		
		RapidMiner.init();
		ExampleSet examples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "monk1_train.arff"), "class");
		
		ActionSnC snc = new ActionSnC(new ActionFinder(actionInductionParams), actionInductionParams);
		ActionRuleSet actions = (ActionRuleSet)snc.run(examples);
		
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, examples);
		dist.calculateActionDistribution();
		ActionMetaTable table = new ActionMetaTable(dist);
		
		ExampleSet testExamples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "monk1_test.arff"), "class");
		for (int i = 0; i < testExamples.size(); i++) {
			Example example = testExamples.getExample(i);
			if (example.getLabel() == 0.0) continue;
			AnalysisResult me = table.analyze(example, 1, 0);
			System.out.println(me.example);
			System.out.println(me.primeMetaExample);
			System.out.println(me.contraMetaExample);
		}
		
		
		RapidMiner.quit(ExitMode.NORMAL);
	}
	
	@Test
	public void runOnCar() throws OperatorCreationException, OperatorException {
		ActionFindingParameters findingParams = new ActionFindingParameters();
		findingParams.setUseNotIntersectingRangesOnly(RangeUsageStrategy.EXCLUSIVE_ONLY);
		
		ActionInductionParameters params = new ActionInductionParameters(findingParams);
		params.setInductionMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		params.setPruningMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		//true, true, 5.0, 0.05, 0.9, "0", "1"
		params.setEnablePruning(true);
		params.setIgnoreMissing(true);
		params.setMinimumCovered(5.0);
		params.setMaximumUncoveredFraction(0.05);
		params.setMaxGrowingConditions(0.9);
		
		params.addClasswiseTransition("unacc", "acc");
		
		RapidMiner.init();
		ExampleSet examples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "car-reduced.arff"), "class");
		
		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
		ActionRuleSet actions = (ActionRuleSet)snc.run(examples);
		
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, examples);
		dist.calculateActionDistribution();
		ActionMetaTable table = new ActionMetaTable(dist);
		
		ExampleSet testExamples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "car-reduced.arff"), "class");
		for (int i = 0; i < testExamples.size(); i++) {
			Example example = testExamples.getExample(i);
			if (example.getLabel() == 1.0) continue;
			AnalysisResult me = table.analyze(example, 0, 1);
			System.out.println(me.example);
			System.out.println(me.primeMetaExample);
			System.out.println(me.contraMetaExample);
		}
		
		
		RapidMiner.quit(ExitMode.NORMAL);
	}
	
	
	@Test
	public void runOnCarNew() throws OperatorCreationException, OperatorException {
		
		ActionFindingParameters findingParams = new ActionFindingParameters();
		findingParams.setUseNotIntersectingRangesOnly(RangeUsageStrategy.EXCLUSIVE_ONLY);
		
		actionInductionParams = new ActionInductionParameters(findingParams);
		actionInductionParams.setInductionMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		actionInductionParams.setPruningMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		//true, true, 5.0, 0.05, 0.9, "0", "1"
		actionInductionParams.setEnablePruning(true);
		actionInductionParams.setIgnoreMissing(true);
		actionInductionParams.setMinimumCovered(5.0);
		actionInductionParams.setMaximumUncoveredFraction(0.05);
		actionInductionParams.setMaxGrowingConditions(0.9);
		
		actionInductionParams.addClasswiseTransition("unacc", "acc");
		ActionSnC snc = new ActionSnC(new ActionFinder(actionInductionParams), actionInductionParams);
		
		
		RapidMiner.init();
		ExampleSet examples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "car-reduced.arff"), "class");
		
		double from = examples.getAttributes().get("class").getMapping().getIndex("unacc");;
		double to = examples.getAttributes().get("class").getMapping().getIndex("acc");;
		
		
		testInternal(examples, 0.99, snc, (int)from, (int)to);
		RapidMiner.quit(ExitMode.NORMAL);
	}

	@Test
	public void runOnFurnaceNew() throws OperatorCreationException, OperatorException {
		
		ActionFindingParameters findingParams = new ActionFindingParameters();
		findingParams.setUseNotIntersectingRangesOnly(RangeUsageStrategy.EXCLUSIVE_ONLY);
		
		actionInductionParams = new ActionInductionParameters(findingParams);
		actionInductionParams.setInductionMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		actionInductionParams.setPruningMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		//true, true, 5.0, 0.05, 0.9, "0", "1"
		actionInductionParams.setEnablePruning(true);
		actionInductionParams.setIgnoreMissing(true);
		actionInductionParams.setMinimumCovered(5.0);
		actionInductionParams.setMaximumUncoveredFraction(0.05);
		actionInductionParams.setMaxGrowingConditions(0.9);
		
		actionInductionParams.addClasswiseTransition("3", "4");
		ActionSnC snc = new ActionSnC(new ActionFinder(actionInductionParams), actionInductionParams);
	
		
		RapidMiner.init();
		ExampleSet examples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "furnace_control.arff"), "class");
		
		double from = examples.getAttributes().get("class").getMapping().getIndex("3");;
		double to = examples.getAttributes().get("class").getMapping().getIndex("4");;
		
		
		testInternal(examples, 0.99, snc, (int)from, (int)to);
		RapidMiner.quit(ExitMode.NORMAL);
	}
	
	public void testInternal(ExampleSet examples, double splitRatio, ActionSnC snc, int fromClass, int toClass) {
		final int TRAIN_IDX = 0;
		final int TEST_IDX = 1;
		StratifiedPartitionBuilder partitionBuilder = new StratifiedPartitionBuilder(examples, true, 1337);
		double[] ratio = new double[2];
		ratio[TRAIN_IDX] = splitRatio;
		ratio[TEST_IDX] = 1 - splitRatio;
		Partition partition = new Partition(ratio, examples.size(), partitionBuilder);
		SplittedExampleSet set = new SplittedExampleSet(examples, partition);
		
		set.selectSingleSubset(TRAIN_IDX);
		//train
		ActionRuleSet actions = (ActionRuleSet) snc.run(set);
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, set);
		dist.calculateActionDistribution();
		ActionMetaTable table = new ActionMetaTable(dist);
		
		//test
		set.invertSelection();
		List<AnalysisResult> results = new ArrayList<AnalysisResult>(set.size());
		
		for(int i = 0; i < set.size(); i++) {
			Example example = set.getExample(i);
			
			AnalysisResult res = table.analyze(example, fromClass, toClass);
			results.add(res);
			System.out.println("Processed rule " + i);
		}
		set.invertSelection();
		ActionRuleSet rules = new ActionRuleSet(set, false, actionInductionParams, null);
		
		for (int j = 0; j < results.size(); j++) {
			
			AnalysisResult res = results.get(j);
			ActionRule rule = res.getActionRule();
			Covering cov = rule.covers(set);
			rule.setCoveringInformation(cov);
			rules.addRule(rule);	
			System.out.print(j+1);
			System.out.println(res.example);
			System.out.println(rule + rule.printStats());
		}
		
		//System.out.println(rules);
		
		
		
	}
	
	@Test
	public void runOnFurnace() throws OperatorCreationException, OperatorException {
		ActionFindingParameters findingParams = new ActionFindingParameters();
		findingParams.setUseNotIntersectingRangesOnly(RangeUsageStrategy.EXCLUSIVE_ONLY);
		
		actionInductionParams = new ActionInductionParameters(findingParams);
		actionInductionParams.setInductionMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		actionInductionParams.setPruningMeasure(new ClassificationMeasure(ClassificationMeasure.Correlation));
		//true, true, 5.0, 0.05, 0.9, "0", "1"
		actionInductionParams.setEnablePruning(true);
		actionInductionParams.setIgnoreMissing(true);
		actionInductionParams.setMinimumCovered(5.0);
		actionInductionParams.setMaximumUncoveredFraction(0.05);
		actionInductionParams.setMaxGrowingConditions(0.9);
		
		actionInductionParams.addClasswiseTransition("3", "4");
		
		RapidMiner.init();
		ExampleSet examples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "furnace_control.arff"), "class");
		
		ActionSnC snc = new ActionSnC(new ActionFinder(actionInductionParams), actionInductionParams);
		ActionRuleSet actions = (ActionRuleSet)snc.run(examples);
		
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, examples);
		dist.calculateActionDistribution();
		ActionMetaTable table = new ActionMetaTable(dist);
		
		ExampleSet testExamples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "furnace_control.arff"), "class");
		for (int i = 0; i < testExamples.size(); i++) {
			Example example = testExamples.getExample(i);
			if (example.getLabel() == 3.0) continue;
			AnalysisResult me = table.analyze(example, 2, 3);
			System.out.println(me.example);
			System.out.println(me.primeMetaExample);
			System.out.println(me.contraMetaExample);
		}
		
		
		RapidMiner.quit(ExitMode.NORMAL);
	}
	
	
}
