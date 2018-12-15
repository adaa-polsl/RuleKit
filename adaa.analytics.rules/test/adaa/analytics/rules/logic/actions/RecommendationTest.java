package adaa.analytics.rules.logic.actions;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.set.StratifiedPartitionBuilder;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;

import adaa.analytics.rules.ActionTests;
import adaa.analytics.rules.logic.induction.ActionFinder;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRuleSet;

///
/// This class takes different ActionRule induction algorithms and:
/// * generates action rules
/// * applies them on test set
///
@RunWith(Parameterized.class)
public class RecommendationTest extends ActionTests {

	protected double trainToTestRatio;
	
	@Parameters
	public static Collection<Object[]> testData(){
		return Arrays.asList(new Object[][]{
			//fileName, labelName, measure, pruningEnabled, ignoreMissing, minCov, maxUncov, maxGrowing, sourceID, targetID
	
			/// furnace control
			///
			///
			
		//	{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "3", "4", 0.8},
			//{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", 0.95},
			//{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "0", "1", 0.95}
			//{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "-", "+", 0.95},
			{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "no", "yes", 0.95},
			{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-versicolor", 0.95},
		});
	}
	
	public RecommendationTest(String testFileName, String labelParameterName,
			ClassificationMeasure measure,
			boolean enablePruning, boolean ignoreMissing, double minimumCovered,
			double maximumUncoveredFraction, double maxGrowingConditions,
			String sourceClass, String targetClass,
			double trainToTestRatio) {
		
		super(testFileName, labelParameterName, measure, 
				enablePruning, ignoreMissing, minimumCovered,
				maximumUncoveredFraction, maxGrowingConditions, 
				sourceClass, targetClass);
		this.trainToTestRatio = trainToTestRatio;
	}
	
	@After
	public void afterTest() {
		stopwatch.stop();
		System.out.println(stopwatch.getTime());
	}
	
	@Test
	public void runMeta() throws OperatorCreationException, OperatorException {

		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
		
		double from = exampleSet.getAttributes().get(labelParameter).getMapping().getIndex(sourceClass);
		double to = exampleSet.getAttributes().get(labelParameter).getMapping().getIndex(targetClass);
		
		Recommendation rec = new MetaRecommendation(snc, (int)from, (int)to);
		
		testInternal(exampleSet, trainToTestRatio, rec);
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
		
		
	}
	
}
