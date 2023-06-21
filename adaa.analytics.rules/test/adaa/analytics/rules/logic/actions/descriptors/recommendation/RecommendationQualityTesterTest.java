package adaa.analytics.rules.logic.actions.descriptors.recommendation;

import adaa.analytics.rules.logic.actions.ActionMetaTable;
import adaa.analytics.rules.logic.actions.ActionRangeDistribution;
import adaa.analytics.rules.logic.actions.MetaAnalysisResult;
import adaa.analytics.rules.logic.actions.OptimizedActionMetaTable;
import adaa.analytics.rules.logic.actions.descriptors.singular.QualityOfSubruleDescriptor;
import adaa.analytics.rules.logic.actions.recommendations.ClassificationRecommendationTask;
import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.set.StratifiedPartitionBuilder;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.renjin.repackaged.guava.io.Files;
import sun.tools.asm.Cover;
import utils.ArffFileLoader;
import utils.PrettyExamplePrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class RecommendationQualityTesterTest {

    private final String dataSetDirectory = "C:/Users/pawel/Desktop/action-rules/datasets/mixed";
    private final String resultDirectory = "C:/Users/pawel/desktop/action-rules/results/recommendation";

    private ClassificationMeasure qualityFunction;
    private String dataFileName;
    private String labelAttributeName;
    private String fromClassName;
    private String toClassName;
    private String outputFileName;

    public RecommendationQualityTesterTest(String dataFileName, String labelAttributeName, ClassificationMeasure qualityFunction,
                                           String fromClass, String toClass) {
        this.dataFileName = dataFileName;
        this.labelAttributeName = labelAttributeName;
        this.qualityFunction = qualityFunction;
        fromClassName = fromClass;
        toClassName = toClass;
        StringJoiner joiner = new StringJoiner("_");
        joiner
                .add(Files.getNameWithoutExtension(dataFileName))
                .add(qualityFunction.getName())
                .add("from")
                .add(fromClass)
                .add("to")
                .add(toClass);
        outputFileName = joiner.toString() + ".recommendations";
    }


    @Parameters
    public static Collection<Object[]> testData(){
        return Arrays.asList(new Object[][]{
                //fileName, labelName, measure, sourceID, targetID
          //      {"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "pos", "neg"},
              //  {"bmt-ch-class.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "pos", "neg"},
             //   {"titanic.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "no", "yes"},
             //   {"horse-colic.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "yes", "no"},
             //   {"hepatitis.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "DIE", "LIVE"},
              //  {"heart-statlog.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "present", "absent"},
               // {"diabetes-c.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "tested_positive", "tested_negative"},
               // {"breast-cancer.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace),  "recurrence-events", "no-recurrence-events"},
               // {"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "unacc", "acc"},
                //{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "1", "2"},
      //          {"vote.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), "republican", "democrat"},
                {"seismic-bumps.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), "1", "0"}
               // {"labor.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "bad", "good"},
        //        {"monk1_test.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "0", "1"},
         //       {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "Iris-setosa","Iris-versicolor"},
      /*          {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "Iris-setosa","Iris-virginica"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "Iris-virginica","Iris-versicolor"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "Iris-virginica", "Iris-setosa"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "Iris-versicolor","Iris-setosa"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.WeightedLaplace), "Iris-versicolor","Iris-virginica"},

                {"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), "unacc", "acc"},
                {"monk1_test.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), "0", "1"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), "Iris-setosa","Iris-versicolor"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), "Iris-setosa","Iris-virginica"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), "Iris-virginica","Iris-versicolor"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), "Iris-virginica", "Iris-setosa"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), "Iris-versicolor","Iris-setosa"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), "Iris-versicolor","Iris-virginica"},

                {"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), "unacc", "acc"},
                {"monk1_test.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), "0", "1"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), "Iris-setosa","Iris-versicolor"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), "Iris-setosa","Iris-virginica"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), "Iris-virginica","Iris-versicolor"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), "Iris-virginica", "Iris-setosa"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), "Iris-versicolor","Iris-setosa"},
                {"iris.arff", "class", new ClassificationMeasure(ClassificationMeasure.InformationGain), "Iris-versicolor","Iris-virginica"},
 */
        });
    }


    @BeforeClass
    public static void initialize(){
        RapidMiner.init();
    }

    @AfterClass
    public static void cleanUp() {
        RapidMiner.quit(RapidMiner.ExitMode.NORMAL);
    }

    @Test
    @Ignore
    public void testRecommendationQualityDataSet() throws OperatorException, OperatorCreationException {
        ActionFindingParameters findingParams = new ActionFindingParameters();
        findingParams.setUseNotIntersectingRangesOnly(ActionFindingParameters.RangeUsageStrategy.EXCLUSIVE_ONLY);

        ActionInductionParameters actionInductionParams = new ActionInductionParameters(findingParams);
        actionInductionParams.setInductionMeasure(this.qualityFunction);
        actionInductionParams.setPruningMeasure(this.qualityFunction);
        //true, true, 5.0, 0.05, 0.9, "0", "1"
        actionInductionParams.setEnablePruning(true);
        actionInductionParams.setIgnoreMissing(true);
        actionInductionParams.setMinimumCovered(5.0);
        actionInductionParams.setMaximumUncoveredFraction(0.05);
        actionInductionParams.setMaxGrowingConditions(0.9);

        actionInductionParams.addClasswiseTransition(this.fromClassName, this.toClassName);
        ActionSnC snc = new BackwardActionSnC(new ActionFinder(actionInductionParams), actionInductionParams);

        ArffFileLoader loader = new ArffFileLoader();

        boolean pruningEnabled = false;
        Path datasetFilePath = Paths.get(this.dataSetDirectory, this.dataFileName);
        ExampleSet examples = loader.load(datasetFilePath, this.labelAttributeName);

        double from = examples.getAttributes().get(this.labelAttributeName).getMapping().getIndex(this.fromClassName);
        double to = examples.getAttributes().get(this.labelAttributeName).getMapping().getIndex(this.toClassName);

        final int TRAIN_IDX = 0;
        final int TEST_IDX = 1;
        StratifiedPartitionBuilder partitionBuilder = new StratifiedPartitionBuilder(examples, true, 1337);
        double[] ratio = new double[2];
        double splitRatio = 0.90;
        ratio[TRAIN_IDX] = splitRatio;
        ratio[TEST_IDX] = 1 - splitRatio;
        Partition partition = new Partition(ratio, examples.size(), partitionBuilder);
        SplittedExampleSet set = new SplittedExampleSet(examples, partition);

        set.selectSingleSubset(TRAIN_IDX);
        //train
        ActionRuleSet actions = (ActionRuleSet) snc.run(set);

        ActionRangeDistribution dist = new ActionRangeDistribution(actions, set);
        dist.calculateActionDistribution();

        ClassificationRecommendationTask recomTask = new ClassificationRecommendationTask(pruningEnabled, false, this.qualityFunction, (int)from, (int)to);

        ActionMetaTable optimized = new OptimizedActionMetaTable(dist, snc.getStableAttributes());

        //test
        set.invertSelection();
        List<List<MetaAnalysisResult>> resultOpt = new ArrayList<>(set.size());

        int fromCount = 0, toCount = 0;

        for(int i = 0; i < set.size(); i++) {
            Example example = set.getExample(i);

            if (Double.compare(example.getLabel(), from) == 0) {
                fromCount++;
            } else if (Double.compare(example.getLabel(), to) == 0) {
                toCount++;
                //skip examples already in target class
                continue;
            }
            List<MetaAnalysisResult> resOptimized = optimized.analyze(example, recomTask);


            //	results.add(res);
            resultOpt.add(resOptimized);
          //  System.out.println("Processed rule " + i);

        }
        //set is still test...


        StringJoiner builder = new StringJoiner("\r\n");
        System.out.println(this.dataFileName);
        builder.add("Dataset filename: " + this.dataFileName);
        builder.add("Number of examples: " + examples.size());
        builder.add("Train to test ratio: " + splitRatio);
        builder.add("Examples in test set: " + set.size());
        builder.add("Source class: " + examples.getAttributes().getLabel().getMapping().getValues().get((int)from));
        builder.add("Target class: " + examples.getAttributes().getLabel().getMapping().getValues().get((int)to));
        builder.add("Source class examples count: " + fromCount);
        builder.add("Target class examples count: " + toCount);

        StringBuilder adhoc = new StringBuilder();

        //train
        set.invertSelection();
        builder.add("Train set size: " + set.size());
        fromCount = 0;
        toCount = 0;

        for (int i = 0; i < set.size(); i++) {
            Example curr = set.getExample(i);
            if (Double.compare(curr.getLabel(), from) == 0) {
                fromCount++;
            } else {
                if (Double.compare(curr.getLabel(), to) == 0) {
                    toCount++;
                }
            }
        }

        builder.add("Source class examples count: " + fromCount + System.lineSeparator());
        builder.add("Target class examples count: " + toCount + System.lineSeparator());
        builder.add("\r\n");

        boolean dropOnlyFirstRecommendation = true;
        RecommendationQualityTester tester = new RecommendationQualityTester(set, new QualityOfSubruleDescriptor(QualityOfSubruleDescriptor.RuleSide.RIGHT,
                this.qualityFunction, set));

        List<Double> precRightRec = new ArrayList<>();
        List<Double> precRightSet = new ArrayList<>();

        for (List<MetaAnalysisResult> curr : resultOpt) {
            if (curr.size() < 1) {
                builder.add("No recommendations generated.");
                continue;
            }
            builder.add("Example " + PrettyExamplePrinter.format(curr.get(0).example));

            int loopLimit = 0;
            if (dropOnlyFirstRecommendation) {
                loopLimit = 1;
            } else {
                loopLimit = curr.size();
            }

            for (int i = 0; i < loopLimit; i++) {

                MetaAnalysisResult res = curr.get(i);
                ActionRule currRecommendation = res.getActionRule();
                RecommendationQualityTester.RecommendationTestResult testResult = tester.runTest(currRecommendation);

                Rule right = res.getActionRule().getRightRule();

                ClassificationMeasure precision = new ClassificationMeasure(ClassificationMeasure.Precision);
                ClassificationMeasure coverage = new ClassificationMeasure(ClassificationMeasure.Coverage);
                ClassificationMeasure other = new ClassificationMeasure(ClassificationMeasure.RSS);

                List<ActionRule> originalRules = actions.getRules().stream()
                        .map(ActionRule.class::cast)
                        .collect(Collectors.toList());

                double bestPrec = 0.0;
                for (ActionRule ar : originalRules) {

                    Covering c = ar.getRightRule().covers(set);
                    double p = other.calculate(set, c);
                    if (p > bestPrec) {
                        bestPrec = p;
                    }
                }

                Covering rightCoverage = right.covers(set);
                adhoc.append("Precision of right recommendation: ").append(other.calculate(set, rightCoverage)).append(System.lineSeparator());
                precRightRec.add(other.calculate(set, rightCoverage));
                adhoc.append("Precision of right best from set: ").append(bestPrec).append(System.lineSeparator());
                precRightSet.add(bestPrec);


                List<Rule> targetMetaCoverage = res.getRuleCoverage(dist.getSplittedRules());
                Map<String, List<Rule>> groupedByClass = targetMetaCoverage
                        .stream()
                        .collect(Collectors.groupingBy(
                                x -> {
                                    SingletonSet clause = (SingletonSet)x.getConsequence().getValueSet();
                                    return clause.getMapping().get((int)clause.getValue());
                                }));
                currRecommendation.setCoveringInformation(currRecommendation.covers(set));
                builder.add("Recommendation #" + (i+1));
                builder.add("Recommendation body " + currRecommendation + currRecommendation.printStats());
                builder.add("Right subrule on train set precision=" + precision.calculate(set, rightCoverage) + " coverage=" + coverage.calculate(set, rightCoverage));
                builder.add(testResult.printResults());
                builder.add("Supersedes?: " + testResult.sucessful);

                if (dropOnlyFirstRecommendation && curr.size() > 1) {
                    List<MetaAnalysisResult> skipped = curr.subList(1, curr.size()-1);
                    double nextMaxPrecision = skipped.stream()
                            .map(MetaAnalysisResult::getActionRule)
                            .map(x -> x.getRightRule().covers(set))
                            .map(x -> precision.calculate(set, x))
                            .max(Comparator.naturalOrder())
                            .orElse(Double.MIN_VALUE);
                    if (precision.calculate(set, rightCoverage) >= nextMaxPrecision) {
                        builder.add("First recommendation is the best");
                    } else {
                        builder.add("Next recommendations were better");
                    }
                }
                builder.add("Meta coverage value:");
                groupedByClass.forEach((key, value) -> builder.add(key + " = " + value.size()));
                builder.add("Meta coverage: ");
                groupedByClass.entrySet().stream().map(Object::toString).forEach(builder::add);

                StringBuilder cov = new StringBuilder();
                Covering cover = right.covers(set);
                cov.append("P=").append(cover.weighted_P).append('\n');
                cov.append("N=").append(cover.weighted_N).append('\n');
                cov.append("p=").append(cover.weighted_p).append('\n');
                cov.append("n=").append(cover.weighted_n).append('\n');

                builder.add("Right subrule coverage: ").add(cov);
            }
        }

        System.out.println("Avg of right side of recommendations: " + precRightRec.stream().mapToDouble(x -> x).average());
        System.out.println("Avg precision of right side of best rule: " + precRightSet.stream().mapToDouble(x -> x).average());
        //System.out.println(adhoc);
        //System.out.println(builder);
        File resultFile = Paths.get(this.resultDirectory, this.outputFileName).toFile();
        try (FileWriter fw = new FileWriter(resultFile)) {
            fw.write(builder.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            Assert.fail("Couldn't write result file");
            return;
        }
        Assert.assertTrue(true);
    }
}