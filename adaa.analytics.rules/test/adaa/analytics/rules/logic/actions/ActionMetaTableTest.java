package adaa.analytics.rules.logic.actions;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import adaa.analytics.rules.logic.actions.descriptors.*;
import adaa.analytics.rules.logic.actions.descriptors.ruleset.AverageCountOfElementaryActionDescriptor;
import adaa.analytics.rules.logic.actions.descriptors.ruleset.AverageCountOfEmptyActions;
import adaa.analytics.rules.logic.actions.descriptors.ruleset.AverageCountOfRulesDescriptor;
import adaa.analytics.rules.logic.actions.descriptors.ruleset.PerRuleDescriptor;
import adaa.analytics.rules.logic.actions.descriptors.singular.ActionCountDescriptor;
import adaa.analytics.rules.logic.actions.descriptors.singular.ConditionCountDescriptor;
import adaa.analytics.rules.logic.actions.descriptors.singular.QualityOfSubruleDescriptor;
import adaa.analytics.rules.logic.actions.recommendations.ClassificationRecommendationTask;
import adaa.analytics.rules.logic.actions.recommendations.RegressionRecommendationTask;
import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.representation.Rule;
import org.junit.Assert;
import org.junit.Test;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExitMode;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.set.StratifiedPartitionBuilder;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;

import adaa.analytics.rules.logic.induction.ActionFindingParameters.RangeUsageStrategy;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import utils.ArffFileLoader;
import utils.InMemoryActionRuleRepository;
import utils.InMemoryDataSet;

public class ActionMetaTableTest {

	private InMemoryActionRuleRepository repo;
	protected ExampleSet set;
	private ActionInductionParameters actionInductionParams;
	private boolean pruningEnabled = false;
	
	
	private void prepare() {
		List<Attribute> atrs = new ArrayList<>();
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

	private void prepareRegressionProblem() {
		List<Attribute> atrs = new ArrayList<>();
		atrs.add(AttributeFactory.createAttribute("class", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("numerical1", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("numerical2", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("nominal", Ontology.NOMINAL));

		List<String> data = Collections.unmodifiableList(Arrays.asList(
				"-10, 15.0, 150.0, a",
				"-10, 13.0, 151.0, a",
				"-10, 12.0, 130.3, a",
				"25.5, 1.0, 150.0, b",
				"25.7, 1.0, 150.0, b",
				"25.9, -1.0, 150.0, b",
				"32, 0.0, 150.0, a",
				"32, 1.0, 150.0, a",
				"32.4, -1.0, 150.0, b"
		));

		set = (new InMemoryDataSet(atrs, data)).getExampleSet();

		repo = new InMemoryActionRuleRepository(set);
	}

	@Test
	public void testRegressionRecommendation_dryrun() {
		RapidMiner.init();
		prepareRegressionProblem();

		ClassificationMeasure measure = new ClassificationMeasure(ClassificationMeasure.Correlation);

		ActionRuleSet rules = repo.getActionRulest();
		ActionRangeDistribution ard = new ActionRangeDistribution(rules, set);
		ard.calculateActionDistribution();
		RegressionRecommendationTask task = new RegressionRecommendationTask(false, false, measure, RegressionActionInductionParameters.RegressionOrder.BETTER);

		ActionMetaTable table = new OptimizedActionMetaTable(ard, new ArrayList<>());
		List<MetaAnalysisResult> results = table.analyze(set.getExample(0), task);
		ActionRule r = results.get(0).getActionRule();
		System.out.println(results.get(0).getActionRule());
	}

	@Test
	public void testAnalyze() {
		RapidMiner.init();
		prepare();

		ClassificationMeasure measure = new ClassificationMeasure(ClassificationMeasure.Correlation);


		ActionRuleSet actions = repo.getActionRulest();
		actions.getRules().forEach(System.out::println);
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, set);
		dist.calculateActionDistribution();
		ActionMetaTable table = new OptimizedActionMetaTable(dist, new ArrayList<>());


		ClassificationRecommendationTask task = new ClassificationRecommendationTask(false, false, measure,0, 2);
		List<MetaAnalysisResult> me2 = table.analyze(set.getExample(0), task);
		//Assert.assertEquals(me.get(0).example, me2.get(0).example);
		System.out.println(me2.get(0).example);
		System.out.println(me2.get(0).primeMetaExample);
		System.out.println(me2.get(0).contraMetaExample);
	}
	

	private void runOnCar_internal() throws OperatorCreationException, OperatorException {
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
		ArffFileLoader loader = new ArffFileLoader();
		ExampleSet examples = loader.load(Paths.get("C:/Users/pawel/Desktop/action-rules/datasets/mixed", "car-reduced.arff"), "class");

		double from = examples.getAttributes().get("class").getMapping().getIndex("unacc");
		double to = examples.getAttributes().get("class").getMapping().getIndex("acc");


		testInternal(examples, 0.99, snc, (int)from, (int)to);
		RapidMiner.quit(ExitMode.NORMAL);
	}
	
	
	@Test
	public void runOnCar_pruningDisabled() throws OperatorCreationException, OperatorException {
		this.pruningEnabled = false;
		runOnCar_internal();
	}

	@Test
	public void runOnCar_pruningEnabled() throws OperatorCreationException, OperatorException {
		pruningEnabled = true;
		runOnCar_internal();
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
		ArffFileLoader loader = new ArffFileLoader();
		ExampleSet examples = loader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "furnace_control.arff"), "class");
		
		double from = examples.getAttributes().get("class").getMapping().getIndex("3");
		double to = examples.getAttributes().get("class").getMapping().getIndex("4");


		testInternal(examples, 0.99, snc, (int)from, (int)to);
		RapidMiner.quit(ExitMode.NORMAL);
	}
	
	private void testInternal(ExampleSet examples, double splitRatio, ActionSnC snc, int fromClass, int toClass) {
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

		ClassificationMeasure measure = new ClassificationMeasure(ClassificationMeasure.Correlation);
		ClassificationRecommendationTask crt = new ClassificationRecommendationTask(this.pruningEnabled, true, measure, fromClass, toClass);

		ActionMetaTable optimized = new OptimizedActionMetaTable(dist, snc.getStableAttributes());
		
		//test
		set.invertSelection();
		List<List<MetaAnalysisResult>> resultOpt = new ArrayList<>(set.size());

		for(int i = 0; i < set.size(); i++) {
			Example example = set.getExample(i);

		//	List<MetaAnalysisResult> res = table.analyze(example, fromClass, toClass, false);

			List<MetaAnalysisResult> resOptimized = optimized.analyze(example, crt);


		//	results.add(res);
			resultOpt.add(resOptimized);
			System.out.println("Processed rule " + i);
		}
		set.invertSelection();
		ActionRuleSet rules = new ActionRuleSet(set, false, actionInductionParams, null);
		ActionRuleSetDescriptors stats = new ActionRuleSetDescriptors();
		stats.add(new AverageCountOfRulesDescriptor());
		stats.add(new AverageCountOfElementaryActionDescriptor());
		stats.add(new AverageCountOfEmptyActions());
		stats.add(new PerRuleDescriptor(new ActionCountDescriptor()));
		stats.add(new PerRuleDescriptor(new ConditionCountDescriptor()));
		stats.add(new PerRuleDescriptor(new QualityOfSubruleDescriptor(QualityOfSubruleDescriptor.RuleSide.LEFT, new ClassificationMeasure(ClassificationMeasure.Precision), set)));
		stats.add(new PerRuleDescriptor(new QualityOfSubruleDescriptor(QualityOfSubruleDescriptor.RuleSide.RIGHT, new ClassificationMeasure(ClassificationMeasure.Precision), set)));
		stats.add(new PerRuleDescriptor(new QualityOfSubruleDescriptor(QualityOfSubruleDescriptor.RuleSide.LEFT, new ClassificationMeasure(ClassificationMeasure.Coverage), set)));
		stats.add(new PerRuleDescriptor(new QualityOfSubruleDescriptor(QualityOfSubruleDescriptor.RuleSide.RIGHT, new ClassificationMeasure(ClassificationMeasure.Coverage), set)));

		System.out.println(stats.generateReport(actions));
		for (int j = 0; j < resultOpt.size(); j++) {


			List<MetaAnalysisResult> result = resultOpt.get(j);
			if (result.size() == 0) {
				continue;
			}
			System.out.print(j + 1);
			System.out.println(result.get(0).example);
			int counter = 1;
			for (MetaAnalysisResult res : result) {
				ActionRule rule = res.getActionRule();
				Covering cov = rule.covers(set);
				rule.setCoveringInformation(cov);
				rules.addRule(rule);
				List<Rule> coverage = res.getRuleCoverage(dist.getSplittedRules());
				System.out.println("Recommendation " + counter + " :" + rule + rule.printStats());
				System.out.println("Right side only: " + res.contraMetaExample);
				System.out.println("Meta coverage: ");
				coverage.forEach(System.out::println);
				counter++;
			}
		}
	}
}
