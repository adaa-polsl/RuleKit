package adaa.analytics.rules.logic.actions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import adaa.analytics.rules.logic.representation.*;
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
			
			//{"furnace_control.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "3", "4", 0.95},
	//		{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9,  "unacc", "acc", 0.95},
			//{"monk1_train.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9, "0", "1", 0.95},
			//{"credit-a.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "-", "+", 0.95},
		//	{"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "no", "yes", 0.95},
		//	{"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "Iris-setosa", "Iris-versicolor", 0.95},
		//	{"seeds.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "2", 0.95},
//			{"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "pos", "neg", ActionTests.stableAttributesForBMTCH, 0.95},
		//	{"blood-transfusion.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "1", "2", 0.95},
			//{"balance-scale.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "L", "R", 0.95},
			//{"breast-w.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "malignant", "benign", 0.95},
			//{"bupa-liver-disorders.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "2", "1", 0.95},
			//{"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "tested_negative", "tested_positive", 0.95},
		//	{"ionosphere.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9, "b", "g", 0.95},
				{"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9,  "republican", "democrat"}
		});
	}
	
	public RecommendationTest(String testFileName, String labelParameterName,
			ClassificationMeasure measure,
			boolean enablePruning, boolean ignoreMissing, double minimumCovered,
			double maximumUncoveredFraction, double maxGrowingConditions,
			String sourceClass, String targetClass, List<String> stableAttributes,
			double trainToTestRatio) {
		
		super(testFileName, labelParameterName, measure, 
				enablePruning, ignoreMissing, minimumCovered,
				maximumUncoveredFraction, maxGrowingConditions, 
				sourceClass, targetClass, stableAttributes);
		this.trainToTestRatio = trainToTestRatio;
	}
	
	@After
	public void afterTest() {
		stopwatch.stop();
		System.out.println(stopwatch.getTime());
	}
	
	@Test
	public void runMeta() {

		ActionSnC snc = new ActionSnC(new ActionFinder(params), params);
		
		double from = exampleSet.getAttributes().get(labelParameter).getMapping().getIndex(sourceClass);
		double to = exampleSet.getAttributes().get(labelParameter).getMapping().getIndex(targetClass);
		Logger.getInstance().addStream(System.out, Level.FINE);
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
		if (!params.getStableAttributes().isEmpty()) {
			for (Rule r : rules.getRules()) {
				ActionRule ar = (ActionRule)r;

				List<Action> violation = ar.getPremise().getSubconditions().stream()
						.map(Action.class::cast)
						.filter(x -> params.getStableAttributes().contains(x.getAttribute()))
						.filter(x -> !x.getActionNil() && !x.isLeftEqualRight())
						.collect(Collectors.toList());
				if (!violation.isEmpty()) {
					System.out.println("Following recommendation violates stable attributes: " + ar.toString());
					System.out.println(violation);
				}
			}
		}
		
	}
	
}
