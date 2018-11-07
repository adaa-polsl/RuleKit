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
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;

import adaa.analytics.rules.logic.actions.ActionMetaTable.AnalysisResult;
import adaa.analytics.rules.logic.induction.ActionFinder;
import adaa.analytics.rules.logic.induction.ActionFindingParameters;
import adaa.analytics.rules.logic.induction.ActionInductionParameters;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.ActionFindingParameters.RangeUsageStrategy;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import utils.ArffFileLoader;
import utils.InMemoryActionRuleRepository;
import utils.InMemoryDataSet;

public class ActionMetaTableTest {

	protected InMemoryActionRuleRepository repo;
	protected ExampleSet set;
	
	
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
	
		Set<MetaExample> examples = table.getExamples();
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
		AnalysisResult me = table.analyze(set.getExample(0), 0, 2, set);
		System.out.println(me.example);
		System.out.println(me.primeMetaExample);
		System.out.println(me.contraMetaExample);
	}
	
	@Test
	public void runOnMonk() throws OperatorCreationException, OperatorException {
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
		
		params.addClasswiseTransition("0", "1");
		
		RapidMiner.init();
		ExampleSet examples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "monk1_train.arff"), "class");
		
		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
		ActionRuleSet actions = (ActionRuleSet)snc.run(examples);
		
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, examples);
		dist.calculateActionDistribution();
		ActionMetaTable table = new ActionMetaTable(dist);
		
		ExampleSet testExamples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "monk1_test.arff"), "class");
		for (int i = 0; i < testExamples.size(); i++) {
			Example example = testExamples.getExample(i);
			if (example.getLabel() == 0.0) continue;
			AnalysisResult me = table.analyze(example, 1, 0, testExamples);
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
			AnalysisResult me = table.analyze(example, 0, 1, testExamples);
			System.out.println(me.example);
			System.out.println(me.primeMetaExample);
			System.out.println(me.contraMetaExample);
		}
		
		
		RapidMiner.quit(ExitMode.NORMAL);
	}

}
