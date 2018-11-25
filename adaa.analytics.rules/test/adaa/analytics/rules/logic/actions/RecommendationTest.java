package adaa.analytics.rules.logic.actions;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExitMode;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.set.StratifiedPartitionBuilder;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;

import adaa.analytics.rules.logic.actions.ActionMetaTable.AnalysisResult;
import adaa.analytics.rules.logic.induction.ActionFinder;
import adaa.analytics.rules.logic.induction.ActionFindingParameters;
import adaa.analytics.rules.logic.induction.ActionInductionParameters;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.induction.ActionFindingParameters.RangeUsageStrategy;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import utils.ArffFileLoader;

///
/// This class takes different ActionRule induction algorithms and:
/// * generates action rules
/// * applies them on test set
///
public class RecommendationTest {

	public RecommendationTest() {
		// TODO Auto-generated constructor stub
	}
	
	@Test
	public void runOnFurnaceMeta() throws OperatorCreationException, OperatorException {
		
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
		
		params.addClasswiseTransition("3", "4");
		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
	
		RapidMiner.init();
		ExampleSet examples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "furnace_control.arff"), "class");
		
		double from = examples.getAttributes().get("class").getMapping().getIndex("3");
		double to = examples.getAttributes().get("class").getMapping().getIndex("4");
		
		Recommendation rec = new MetaRecommendation(snc, (int)from, (int)to);
		
		testInternal(examples, 0.95, rec);
		
		RapidMiner.quit(ExitMode.NORMAL);
	}
	
	
	public void runOnFurnaceRegular() throws OperatorCreationException, OperatorException {
		
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
		
		params.addClasswiseTransition("3", "4");
		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
	
		RapidMiner.init();
		ExampleSet examples = ArffFileLoader.load(Paths.get("C:/Users/pmatyszok/desktop/action-rules/datasets/mixed", "furnace_control.arff"), "class");
		
		double from = examples.getAttributes().get("class").getMapping().getIndex("3");
		double to = examples.getAttributes().get("class").getMapping().getIndex("4");
		
		Recommendation rec = new SnCRecommendation(snc);

	}

	private void testInternal(ExampleSet examples, double splitRatio, Recommendation rec) {
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
		rec.train(set);
		
		//test
		set.invertSelection();
		
		ActionRuleSet rules = rec.test(set);
		
		System.out.println(rules);

	}
	
}
