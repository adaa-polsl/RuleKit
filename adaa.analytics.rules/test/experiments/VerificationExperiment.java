//package experiments;
//
//import adaa.analytics.rules.logic.actions.ActionMetaTable;
//import adaa.analytics.rules.logic.actions.ActionRangeDistribution;
//import adaa.analytics.rules.logic.actions.OptimizedActionMetaTable;
//import adaa.analytics.rules.logic.actions.recommendations.ClassificationRecommendationTask;
//import adaa.analytics.rules.logic.induction.*;
//import adaa.analytics.rules.logic.quality.ClassificationMeasure;
//import adaa.analytics.rules.logic.representation.*;
//import adaa.analytics.rules.logic.representation.model.ActionRuleSet;
//import adaa.analytics.rules.logic.representation.model.ClassificationRuleSet;
//import adaa.analytics.rules.logic.representation.model.RuleSetBase;
//import adaa.analytics.rules.rm.example.set.AttributeValueFilterSingleCondition;
//import adaa.analytics.rules.rm.example.set.ConditionedExampleSet;
//import adaa.analytics.rules.rm.example.set.ICondition;
//import adaa.analytics.rules.rm.example.table.INominalMapping;
//import com.rapidminer.RapidMiner;
//import adaa.analytics.rules.rm.example.IAttribute;
//import adaa.analytics.rules.rm.example.Example;
//import adaa.analytics.rules.rm.example.IExampleSet;
//import com.rapidminer.gui.tools.dialogs.wizards.dataimport.csv.CSVFileReader;
//import com.rapidminer.operator.OperatorCreationException;
//import com.rapidminer.operator.OperatorException;
//import com.rapidminer.operator.preprocessing.IdTagging;
//import com.rapidminer.operator.preprocessing.filter.AttributeAdd;
//import com.rapidminer.operator.tools.ExpressionEvaluationException;
//import com.rapidminer.tools.LineParser;
//import com.rapidminer.tools.LogService;
//import com.rapidminer.tools.OperatorService;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.renjin.repackaged.guava.base.Strings;
//import org.renjin.repackaged.guava.io.Files;
////import utils.ArffFileWriter;
//import utils.Mutator;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.text.NumberFormat;
//import java.util.*;
//import java.util.logging.Level;
//
//public class VerificationExperiment {
//
//    static String dataDirectory = "C:/Users/pawel/desktop/action-rules/datasets/mixed/";
//    static String regressionDirectory = "C:/Users/pawel/desktop/action-rules/datasets/regression/";
//    static String resultDir = "C:/Users/pawel/desktop/action-rules/results/classification/";
//
//    private ActionInductionParameters params;
//    private List<FileDescription> dataFiles;
//    private ClassificationMeasure measure;
//    static double trainSetPercentage = 0.8;
//    private final int number_of_folds = 10;
//    private final Mutator mutator = new Mutator();
//
//
//    public VerificationExperiment() throws OperatorCreationException {
//    }
//
//    private ActionFindingParameters.RangeUsageStrategy currentRangeStrategy = ActionFindingParameters.RangeUsageStrategy.NOT_INTERSECTING;
//
//    private ActionFindingParameters.RangeUsageStrategy getCurrentRangeStrategy() {
//        return currentRangeStrategy;
//    }
//
//    private void prepareParams(ClassificationMeasure qualityFunction, ExperimentTask task){
//        ActionFindingParameters findingParams = new ActionFindingParameters();
//        findingParams.setUseNotIntersectingRangesOnly(getCurrentRangeStrategy());
//
//        measure = qualityFunction;
//
//        params = task.createParamsObject(findingParams);
//        params.setInductionMeasure(measure);
//        params.setPruningMeasure(measure);
//        params.setVotingMeasure(measure);
//        params.setEnablePruning(true);
//        params.setIgnoreMissing(true);
//        params.setMinimumCovered(5);
//        params.setMaximumUncoveredFraction(0.05);
//        params.setMaxGrowingConditions(0);
//    }
//
//    @BeforeClass
//    public static void initRapidMinerSilently() {
//        if (RapidMiner.isInitialized()) {
//            return;
//        }
//        LogService.getRoot().setLevel(Level.OFF);
//        RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);
//        RapidMiner.init();
//    }
//
//    @Test
//    public void tets() throws OperatorException, OperatorCreationException, IOException {
//        prepareDataSets();
//        experiment(new FileDescription("credit-g.arff", "bad", "good", 10), new ClassificationMeasure(ClassificationMeasure.C2));
//     /*   experiment(new FileDescription("iris-reduced.arff", "Iris-setosa", "Iris-versicolor", number_of_folds),10,  new ClassificationMeasure(ClassificationMeasure.Correlation));
//        experiment(new FileDescription("wine-reduced.arff", "1", "2", number_of_folds),10,  new ClassificationMeasure(ClassificationMeasure.Precision));
//        experiment(new FileDescription("labor.arff", "bad", "good", number_of_folds),10,  new ClassificationMeasure(ClassificationMeasure.Precision));
//    */
//    }
//
//    private void runClassificaitonInternal(String resultBit) throws OperatorException, OperatorCreationException, IOException {
//        resultDir = "C:/Users/pawel/desktop/action-rules/results/" + resultBit + '/';
//        prepareDataSets();
//        ClassificationMeasure[] measures =
//                {
//                        new ClassificationMeasure(ClassificationMeasure.C2),
//                        new ClassificationMeasure(ClassificationMeasure.RSS),
//                        new ClassificationMeasure(ClassificationMeasure.InformationGain),
//                        new ClassificationMeasure(ClassificationMeasure.WeightedLaplace),
//                        new ClassificationMeasure(ClassificationMeasure.Correlation)
//                };
//
//        FileWriter fw = new FileWriter("C:\\Users\\pawel\\desktop\\action-rules\\analysis\\r-package\\action.rules\\results\\" + resultBit + "\\preResults.csv");
//        fw.write("dataset;fold_id;train_forest_acc;test_forest_acc;test_target_class_acc;test_source_class_acc;source_examples_in_test;target_examples_in_test;predicted_source_in_test;predicted_target_in_test;n_predicted_as_source;n_predicted_as_target;n_predicted_as_target_recom;n_predicted_as_source_recom;source_class;target_class;measure;direction;covered_by_rules;covered_by_recom;examples_in_mutated;classifier");
//        fw.write(System.lineSeparator());
//
//        for (FileDescription desc : dataFiles) {
//            for (ClassificationMeasure m : measures) {
//                System.out.println("Starting experiment for file " + desc.getFilePath() + " for function " + m.getName());
//                List<Map<String, Double>> results = experiment(desc, m);
//                for (Map<String,Double> entry : results) {
//
//                    //forward
//                    fw.write(desc.getFileNameWithoutExtension());fw.write(';');
//                    writeCommonCSVPart(fw, entry);
//                    fw.write(entry.get("n_predicted_as_source").toString()); fw.write(";");
//                    fw.write(entry.get("n_predicted_as_target").toString()); fw.write(";");
//                    fw.write(entry.get("n_predicted_as_target_recom").toString());fw.write(";");
//                    fw.write(entry.get("n_predicted_as_source_recom").toString());fw.write(";");
//                    fw.write(desc.getSourceClass());fw.write(";");
//                    fw.write(desc.getTargetClass());fw.write(";");
//                    fw.write(m.getName());fw.write(";");
//                    fw.write("forward");fw.write(";");
//                    fw.write(entry.get("covered_by_rules_f").toString());fw.write(";");
//                    fw.write(entry.get("covered_by_recom_f").toString());fw.write(";");
//                    fw.write(entry.get("examples_in_mutated_f").toString());fw.write(";");
//                    fw.write("rules");
//                    fw.write(System.lineSeparator());
//                    //backward
//                    fw.write(desc.getFileNameWithoutExtension());fw.write(';');
//                    writeCommonCSVPart(fw, entry);
//                    fw.write(entry.get("n_predicted_as_source_b").toString());fw.write(";");
//                    fw.write(entry.get("n_predicted_as_target_b").toString());fw.write(";");
//                    fw.write(entry.get("n_predicted_as_target_recom_b").toString());fw.write(";");
//                    fw.write(entry.get("n_predicted_as_source_recom_b").toString());fw.write(";");
//                    fw.write(desc.getSourceClass());fw.write(";");
//                    fw.write(desc.getTargetClass());fw.write(";");
//                    fw.write(m.getName());fw.write(";");
//                    fw.write("backward");fw.write(";");
//                    fw.write(entry.get("covered_by_rules_b").toString());fw.write(";");
//                    fw.write(entry.get("covered_by_recom_b").toString());fw.write(";");
//                    fw.write(entry.get("examples_in_mutated_b").toString());fw.write(";");
//                    fw.write("rules");
//                    fw.write(System.lineSeparator());
//
//                    //fb
//                    fw.write(desc.getFileNameWithoutExtension());fw.write(';');
//                    writeCommonCSVPart(fw, entry);
//                    fw.write(entry.get("n_predicted_as_source_fb").toString());fw.write(";");
//                    fw.write(entry.get("n_predicted_as_target_fb").toString());fw.write(";");
//                    fw.write(entry.get("n_predicted_as_target_recom_fb").toString());fw.write(";");
//                    fw.write(entry.get("n_predicted_as_source_recom_fb").toString());fw.write(";");
//                    fw.write(desc.getSourceClass());fw.write(";");
//                    fw.write(desc.getTargetClass());fw.write(";");
//                    fw.write(m.getName());fw.write(";");
//                    fw.write("fb");fw.write(";");
//                    fw.write(entry.get("covered_by_rules_fb").toString());fw.write(";");
//                    fw.write(entry.get("covered_by_recom_fb").toString());fw.write(";");
//                    fw.write(entry.get("examples_in_mutated_fb").toString());fw.write(";");
//                    fw.write("rules");
//                    fw.write(System.lineSeparator());
//                }
//
//            }
//        }
//        fw.close();
//    }
//
//    @Test
//    public void testMany() throws OperatorException, OperatorCreationException, IOException {
//        currentRangeStrategy = ActionFindingParameters.RangeUsageStrategy.NOT_INTERSECTING;
//        runClassificaitonInternal("not_intersecting");
//    }
//
//    @Test
//    public void testClassification_ExclusiveOnly() throws OperatorException, OperatorCreationException, IOException {
//        currentRangeStrategy = ActionFindingParameters.RangeUsageStrategy.EXCLUSIVE_ONLY;
//        prepareDataSets();
//        experiment(new FileDescription("horse-colic.arff", "no", "yes", 10), new ClassificationMeasure(ClassificationMeasure.C2));
//        //runClassificaitonInternal("exclusive_only");
//    }
//
//    @Test
//    public void testClassification_AllRanges() throws OperatorException, OperatorCreationException, IOException {
//        currentRangeStrategy = ActionFindingParameters.RangeUsageStrategy.ALL;
//        runClassificaitonInternal("all_ranges");
//    }
//
//    private void writeCommonCSVPart(FileWriter fw, Map<String, Double> entry) throws IOException {
//        fw.write(entry.get("fold_id").toString());fw.write(";");
//        fw.write(entry.get("train_forest_acc").toString());fw.write(";");
//        fw.write(entry.get("test_forest_acc").toString());fw.write(";");
//        fw.write(entry.get("test_target_class_acc").toString());fw.write(";");
//        fw.write(entry.get("test_source_class_acc").toString());fw.write(";");
//        fw.write(entry.get("source_examples_in_test").toString());fw.write(";");
//        fw.write(entry.get("target_examples_in_test").toString());fw.write(";");
//        fw.write(entry.get("predicted_source_in_test").toString());fw.write(";");
//        fw.write(entry.get("predicted_target_in_test").toString());fw.write(";");
//    }
//
//    private void prepareDataSets() throws OperatorException, OperatorCreationException {
//        List<String[]> datasets = getDatasetDescriptions();
//
//        dataFiles = new ArrayList<>();
//
//        for(String[] row : datasets){
//            if (row.length < 11)
//                continue;
//            if (Strings.isNullOrEmpty(row[9]) && Strings.isNullOrEmpty(row[10]))
//                continue;
//            dataFiles.add(new FileDescription(row[0] + ".arff", row[9], row[10], number_of_folds));
//        }
//
//        dataFiles.removeIf(x -> x.getSourceClass().isEmpty() && x.getTargetClass().isEmpty());
//    }
//
//    private List<String[]> getDatasetDescriptions() throws OperatorException {
//        if (!RapidMiner.isInitialized()) {
//            RapidMiner.init();
//        }
//
//        File datasetsFile = new File(dataDirectory + "_datasets.csv");
//        LineParser parser = new LineParser();
//        parser.setUseQuotes(true);
//        parser.setSplitExpression(LineParser.SPLIT_BY_SEMICOLON_EXPRESSION);
//        NumberFormat nf = NumberFormat.getInstance();
//        CSVFileReader reader = new CSVFileReader(datasetsFile, true, parser, nf);
//        List<String[]> datasets;
//        try {
//             datasets = reader.readData(200);
//        } catch (IOException ex) {
//            throw new RuntimeException("Couldn't load dataset description. Reason: " + ex.getMessage());
//        }
//        return datasets;
//    }
//
//
//    private File buildRulesFile(int iteration, String ruleSetType, FileDescription dataFileDesc) {
//        return new File(resultDir
//                + dataFileDesc.getFileNameWithoutExtension()
//                + "/"
//                + dataFileDesc.getPathModificator()
//                + dataFileDesc.getFileNameWithoutExtension()
//                + "fold-" + iteration
//                + "-" + ruleSetType + "-" + measure.getName() + ".rules" );
//    }
//
//    private String buildArffFileName(int iteration, String dataName, FileDescription dataFileDesc) {
//        return (resultDir
//                + dataFileDesc.getFileNameWithoutExtension()
//                + "/"
//                + dataFileDesc.getPathModificator()
//                + dataFileDesc.getFileNameWithoutExtension()
//                + "fold-" + iteration
//                + "-"
//                + dataName
//                + measure.getName()
//                + ".arff");
//    }
//
//    private String buildRecommendationsFileName(int iteration, String dataName, FileDescription dataFileDesc) {
//       return (resultDir
//                + dataFileDesc.getFileNameWithoutExtension()
//                + "/"
//                + dataFileDesc.getPathModificator()
//                + dataFileDesc.getFileNameWithoutExtension()
//                + "fold-" + iteration
//                + measure.getName()
//                + dataName
//                + ".recommendations");
//
//    }
//
//    private List<Map<String, Double>> experiment(FileDescription dataFileDesc, ClassificationMeasure qualityFunction) throws OperatorException, OperatorCreationException, IOException {
//        if (!RapidMiner.isInitialized()) {
//            RapidMiner.init();
//        }
//        List<Map<String,Double>> allResults = new ArrayList<>();
//        if (trainSetPercentage > 0.99 || trainSetPercentage < 0.01)
//            throw new IllegalArgumentException("The trainSetPercentage must be betwen 0.01 and 0.99");
//
//
//        ExperimentTask task = new ExperimentTask();
//        prepareParams(qualityFunction, task);
//        for (int iteration = 0; iteration < dataFileDesc.getFoldCount(); iteration++) {
//            Map<String, Double> results = new HashMap<>();
//            IExampleSet train = dataFileDesc.getTrainSetForFold(iteration);
//
//            IExampleSet testSet = dataFileDesc.getTestSetForFold(iteration);
//            IExampleSet test_no_id = mutator.materializeExamples(testSet);
//            IdTagging id = OperatorService.createOperator(IdTagging.class);
//            IExampleSet test = id.apply(test_no_id);
//
//            RuleSerializer ser = new RuleSerializer(train, ';', "nil");
//            //induce the action rules
//           // params.addClasswiseTransition(dataFileDesc.getSourceClass(), dataFileDesc.getTargetClass());
//
//            task.preprocessParams(params, dataFileDesc);
//
//            ActionSnC actionSnC = (ActionSnC)task.getActionSnC(params);
//            BackwardActionSnC backwardActionSnC = (BackwardActionSnC) task.getBackwardActionSnC(params);
//            ClassificationSnC classificationSnC = (ClassificationSnC) task.getRuleSnc(params);
//
//            long timeBefore = System.currentTimeMillis();
//
//            ActionRuleSet actionRuleSet = (ActionRuleSet)actionSnC.run(train);
//            System.out.println("Action rules took " + (System.currentTimeMillis() - timeBefore));
//
//            ActionRuleSet backwardRuleSet = (ActionRuleSet)backwardActionSnC.run(train);
//            ClassificationRuleSet cRuleSet = (ClassificationRuleSet) classificationSnC.run(train);
//
//            IExampleSet classifiedTrain = cRuleSet.apply(train);
//
//            ExperimentTask.ExampleSetEvaluation trainEval = new ExperimentTask.ExampleSetEvaluation(classifiedTrain, dataFileDesc);
//            double ruleTrainAcc = trainEval.balancedAcc;
//
//            IExampleSet predictedByClassificationRules = cRuleSet.apply(test);
//
//            ExperimentTask.ExampleSetEvaluation testEval = new ExperimentTask.ExampleSetEvaluation(predictedByClassificationRules, dataFileDesc);
//            double testAcc = testEval.balancedAcc;
//            int sourcesInTest = testEval.sourceExamplesCount;
//            int targetsInTest = testEval.targetExamplesCount;
//            double testTargetAcc = testEval.targetClassAcc;
//            double testSourceAcc = testEval.sourceClassAcc;
//
//            writeRuleSetsTestTrain(dataFileDesc, iteration, train, ser, actionRuleSet, backwardRuleSet, cRuleSet, predictedByClassificationRules);
//
//            ICondition cnd = new AttributeValueFilterSingleCondition(test.getAttributes().getLabel(), AttributeValueFilterSingleCondition.EQUALS, dataFileDesc.getSourceClass());
//            IExampleSet sourceExamplesInTestSet  = mutator.materializeExamples(new ConditionedExampleSet(test, cnd));
//
//            AttributeAdd adder = OperatorService.createOperator(AttributeAdd.class);
//            adder.setParameter(AttributeAdd.PARAMETER_NAME, "mutated_rule_id");
//            adder.setParameter(AttributeAdd.PARAMETER_VALUE_TYPE, "integer");
//
//            sourceExamplesInTestSet = adder.apply(sourceExamplesInTestSet);
//
//
//
//            timeBefore = System.currentTimeMillis();
//
//            ClassificationRecommendationTask recomTask = (ClassificationRecommendationTask) task.getRecommendationTask(params, dataFileDesc, train, measure);
//            ActionRangeDistribution dist = new ActionRangeDistribution(actionRuleSet, train);
//            dist.calculateActionDistribution();
//            ActionMetaTable optimized = new OptimizedActionMetaTable(dist,  actionSnC.getStableAttributes());
//            System.out.println("Wyliczenia rozkładu forward: " + (System.currentTimeMillis() - timeBefore));
//
//            timeBefore = System.currentTimeMillis();
//            ActionRangeDistribution backwardDistribution = new ActionRangeDistribution(backwardRuleSet, train);
//            backwardDistribution.calculateActionDistribution();
//            ActionMetaTable backwardMetaTable = new OptimizedActionMetaTable(backwardDistribution,  backwardActionSnC.getStableAttributes());
//            System.out.println("Wyliczenia rozkładu backward: " + (System.currentTimeMillis() - timeBefore));
//
//            // create combined rule set
//            ActionRuleSet fb = new ActionRuleSet(train, false, params, null);
//            actionRuleSet.getRules().stream().forEach(x -> fb.addRule(x));
//            backwardRuleSet.getRules().stream().forEach(x -> fb.addRule(x));
//            //filter for uniqueness ? will see, shouldn't matter
//
//            timeBefore = System.currentTimeMillis();
//            ActionRangeDistribution fbDistribution = new ActionRangeDistribution(fb, train);
//            fbDistribution.calculateActionDistribution();
//            ActionMetaTable fbMetaTable = new OptimizedActionMetaTable(fbDistribution, actionSnC.getStableAttributes());
//            System.out.println("Wyliczenia rozkładu forward-backward: " + (System.currentTimeMillis() - timeBefore));
//
//            ActionRuleSet recommendations = new ActionRuleSet(sourceExamplesInTestSet, true, params, null);
//            ActionRuleSet backwardRecommendations = new ActionRuleSet(sourceExamplesInTestSet, true, params, null);
//            ActionRuleSet FBRecommendations = new ActionRuleSet(sourceExamplesInTestSet, true, params, null);
//            timeBefore = System.currentTimeMillis();
//
//            IExampleSet mutatedBackwardRecom = mutator.mutateExamples(sourceExamplesInTestSet, backwardMetaTable, dataFileDesc.getSourceClass(), dataFileDesc.getTargetClass(), backwardRecommendations, train, recomTask);
//            System.out.println("Mutacja rekomendacjami backward: " + (System.currentTimeMillis() - timeBefore));
//
//            timeBefore = System.currentTimeMillis();
//            IExampleSet mutatedBackwardRules = mutator.mutateExamples(sourceExamplesInTestSet, backwardRuleSet, train, dataFileDesc.getTargetClass());
//            System.out.println("Mutacja regułami backward: " + (System.currentTimeMillis() - timeBefore));
//
//            timeBefore = System.currentTimeMillis();
//            IExampleSet mutatedRecom = mutator.mutateExamples(sourceExamplesInTestSet, optimized, dataFileDesc.getSourceClass(), dataFileDesc.getTargetClass(), recommendations, train, recomTask);
//            System.out.println("Mutacja rekomendacjami forward: " + (System.currentTimeMillis() - timeBefore));
//
//            timeBefore = System.currentTimeMillis();
//            IExampleSet mutated = mutator.mutateExamples(sourceExamplesInTestSet, actionRuleSet, train, dataFileDesc.getTargetClass());
//            System.out.println("Mutacja regułami forward: " + (System.currentTimeMillis() - timeBefore));
//
//            timeBefore = System.currentTimeMillis();
//            IExampleSet mutatedRecomFB = mutator.mutateExamples(sourceExamplesInTestSet, fbMetaTable, dataFileDesc.getSourceClass(), dataFileDesc.getTargetClass(), FBRecommendations, train, recomTask);
//            System.out.println("Mutacja rekomendacjami forward-backward: " + (System.currentTimeMillis() - timeBefore));
//
//            timeBefore = System.currentTimeMillis();
//            IExampleSet mutatedFBRules = mutator.mutateExamples(sourceExamplesInTestSet, fb, train, dataFileDesc.getTargetClass());
//            System.out.println("Mutacja regułami forward-backward: " + (System.currentTimeMillis() - timeBefore));
//
//
//            writeRecomendationsAndMutatedExamples(dataFileDesc, iteration, ser, recommendations,
//                    backwardRecommendations, FBRecommendations, mutatedBackwardRecom,
//                    mutatedBackwardRules, mutatedRecom, mutated,
//                    mutatedFBRules, mutatedRecomFB);
//
//            //for the verification we take only examples that were covered by action rules or recommendations
//
//            IExampleSet mutatedAndCoveredByRules = getCoveredByRules(mutated);
//            IExampleSet mutatedAndCoveredByBackwardRules = getCoveredByRules(mutatedBackwardRules);
//            IExampleSet mutatedAndCoveredByFBRules = getCoveredByRules(mutatedFBRules);
//
//            IExampleSet mutatedAndCoveredByRecom = getCoveredByRecom(mutatedRecom);
//            IExampleSet mutatedAndCoveredByBackwardRecom = getCoveredByRecom(mutatedBackwardRecom);
//            IExampleSet mutatedAndCoveredByFBRecom = getCoveredByRecom(mutatedRecomFB);
//
//            IExampleSet rules_mutated = cRuleSet.apply(mutatedAndCoveredByRules);
//            IExampleSet rules_mutatedBackwardRules = cRuleSet.apply(mutatedAndCoveredByBackwardRules);
//            IExampleSet rules_mutatedFBRules = cRuleSet.apply(mutatedAndCoveredByFBRules);
//
//            IExampleSet rules_mutatedRecom = cRuleSet.apply(mutatedAndCoveredByRecom);
//            IExampleSet rules_mutatedBackwardRecom = cRuleSet.apply(mutatedAndCoveredByBackwardRecom);
//            IExampleSet rules_mutatedFBRecom = cRuleSet.apply(mutatedAndCoveredByFBRecom);
//
//            int n_predicted_as_source_f = getNumberOfPredictedExamples(rules_mutated, dataFileDesc.getSourceClass());
//            int n_predicted_as_target_f = getNumberOfPredictedExamples(rules_mutated, dataFileDesc.getTargetClass());
//            int n_predicted_as_source_b = getNumberOfPredictedExamples(rules_mutatedBackwardRules, dataFileDesc.getSourceClass());
//            int n_predicted_as_target_b = getNumberOfPredictedExamples(rules_mutatedBackwardRules, dataFileDesc.getTargetClass());
//            int n_predicted_as_source_fb = getNumberOfPredictedExamples(rules_mutatedFBRules, dataFileDesc.getSourceClass());
//            int n_predicted_as_target_fb = getNumberOfPredictedExamples(rules_mutatedFBRules, dataFileDesc.getTargetClass());
//
//
//            int n_predicted_as_source_recom_f = getNumberOfPredictedExamples(rules_mutatedRecom, dataFileDesc.getSourceClass());
//            int n_predicted_as_target_recom_f = getNumberOfPredictedExamples(rules_mutatedRecom, dataFileDesc.getTargetClass());
//            int n_predicted_as_source_recom_b = getNumberOfPredictedExamples(rules_mutatedBackwardRecom, dataFileDesc.getSourceClass());
//            int n_predicted_as_target_recom_b = getNumberOfPredictedExamples(rules_mutatedBackwardRecom, dataFileDesc.getTargetClass());
//            int n_predicted_as_source_recom_fb = getNumberOfPredictedExamples(rules_mutatedFBRecom, dataFileDesc.getSourceClass());
//            int n_predicted_as_target_recom_fb = getNumberOfPredictedExamples(rules_mutatedFBRecom, dataFileDesc.getTargetClass());
//
//
//            results.put("fold_id", (double)iteration);
//            results.put("train_forest_acc", ruleTrainAcc);
//            results.put("test_forest_acc", testAcc);
//            results.put("test_target_class_acc", testTargetAcc);
//            results.put("test_source_class_acc", testSourceAcc);
//            results.put("source_examples_in_test", (double)sourceExamplesInTestSet.size());
//            results.put("target_examples_in_test", (double)(test.size() - sourceExamplesInTestSet.size()));
//            results.put("predicted_source_in_test", (double)sourcesInTest);
//            results.put("predicted_target_in_test", (double)targetsInTest);
//            results.put("n_predicted_as_source", (double)n_predicted_as_source_f);
//            results.put("n_predicted_as_target", (double)n_predicted_as_target_f);
//            results.put("n_predicted_as_target_recom", (double)n_predicted_as_target_recom_f);
//            results.put("n_predicted_as_source_recom", (double)n_predicted_as_source_recom_f);
//            results.put("examples_in_mutated_f", (double)mutated.size());
//            results.put("examples_in_mutated_b", (double)mutatedBackwardRules.size());
//            results.put("examples_in_mutated_fb", (double)mutatedFBRules.size());
//            results.put("n_predicted_as_source_b", (double)n_predicted_as_source_b);
//            results.put("n_predicted_as_target_b", (double)n_predicted_as_target_b);
//            results.put("n_predicted_as_target_recom_b", (double)n_predicted_as_target_recom_b);
//            results.put("n_predicted_as_source_recom_b", (double)n_predicted_as_source_recom_b);
//            results.put("n_predicted_as_source_fb", (double)n_predicted_as_source_fb);
//            results.put("n_predicted_as_target_fb", (double)n_predicted_as_target_fb);
//            results.put("n_predicted_as_target_recom_fb", (double)n_predicted_as_target_recom_fb);
//            results.put("n_predicted_as_source_recom_fb", (double)n_predicted_as_source_recom_fb);
//            results.put("covered_by_rules_f", (double)mutatedAndCoveredByRules.size());
//            results.put("covered_by_recom_f", (double)mutatedAndCoveredByRecom.size());
//            results.put("covered_by_rules_b", (double)mutatedAndCoveredByBackwardRules.size());
//            results.put("covered_by_recom_b", (double)mutatedAndCoveredByBackwardRecom.size());
//            results.put("covered_by_rules_fb", (double)mutatedAndCoveredByFBRules.size());
//            results.put("covered_by_recom_fb", (double)mutatedAndCoveredByFBRecom.size());
//
//            allResults.add(results);
//        }
//        return allResults;
//    }
//
//    private void writeRecomendationsAndMutatedExamples(FileDescription dataFileDesc, int iteration, RuleSerializer ser,
//                                                       ActionRuleSet recommendations,
//                                                       ActionRuleSet backwardRecommendations,
//                                                       ActionRuleSet fbRecommendations,
//                                                       IExampleSet mutatedBackwardRecom,
//                                                       IExampleSet mutatedBackwardRules,
//                                                       IExampleSet mutatedRecom,
//                                                       IExampleSet mutated,
//                                                       IExampleSet mutatedFB,
//                                                       IExampleSet mutatedFBRecom)
//            throws OperatorCreationException, OperatorException, IOException {
//
//
//        String mutatedDumpName = buildArffFileName(iteration, "mutated", dataFileDesc);
//        String mutatedFBDumpName = buildArffFileName(iteration, "mutatedFB-" , dataFileDesc);
//        String mutatedBackwardDumpName = buildArffFileName (iteration, "mutatedBackward-" , dataFileDesc);
//        String mutatedRecomDumpName = buildArffFileName(iteration, "mutated-recom" , dataFileDesc);
//        String mutatedFBRecomDumpName = buildArffFileName(iteration, "mutated-fb-recom-", dataFileDesc);
//        String mutatedRecomBackwardDumpName = buildArffFileName (iteration, "mutated-backward-recom-", dataFileDesc);
//
//        String recommendationsFileName = buildRecommendationsFileName(iteration, "", dataFileDesc);
//        String recommendationsBackwardFileName = buildRecommendationsFileName(iteration, "-backward", dataFileDesc);
//        String recommendationsFBFileName = buildRecommendationsFileName(iteration, "-fb", dataFileDesc);
//
//        ArffFileWriter.write(mutated, mutatedDumpName);
//        ArffFileWriter.write(mutatedRecom, mutatedRecomDumpName);
//        ArffFileWriter.write(mutatedBackwardRules, mutatedBackwardDumpName);
//        ArffFileWriter.write(mutatedBackwardRecom, mutatedRecomBackwardDumpName);
//        ArffFileWriter.write(mutatedFB, mutatedFBDumpName);
//        ArffFileWriter.write(mutatedFBRecom, mutatedFBRecomDumpName);
//
//        Files.write((recommendations.toString() +  System.lineSeparator() + ser.serializeToCsv(recommendations)).getBytes(), new File(recommendationsFileName));
//        Files.write((backwardRecommendations.toString()  + System.lineSeparator() + ser.serializeToCsv(backwardRecommendations)).getBytes(), new File(recommendationsBackwardFileName));
//        Files.write((fbRecommendations.toString() + System.lineSeparator() + ser.serializeToCsv(fbRecommendations)).getBytes(), new File(recommendationsFBFileName));
//    }
//
//    private void writeRuleSetsTestTrain(FileDescription dataFileDesc, int iteration, IExampleSet train, RuleSerializer ser, ActionRuleSet actionRuleSet, ActionRuleSet backwardRuleSet, ClassificationRuleSet cRuleSet, IExampleSet predictedByClassificationRules) throws IOException, OperatorCreationException, OperatorException {
//        File rulesFile = buildRulesFile(iteration, "rules", dataFileDesc);
//        File actionRulesFile = buildRulesFile(iteration, "action-rules", dataFileDesc);
//        File backwardActionRulesFile = buildRulesFile(iteration, "action-backward-rules", dataFileDesc);
//
//        if (!rulesFile.getParentFile().exists()) {
//            (new File(rulesFile.getParent())).mkdirs();
//
//        }
//
//        Files.write(cRuleSet.toString().getBytes(), rulesFile);
//
//        RuleSetBase.Significance sigFLFDR = actionRuleSet.getSourceRuleSet().calculateSignificanceFDR(0.05);
//        RuleSetBase.Significance sigFRFDR = actionRuleSet.getTargetRuleSet().calculateSignificanceFDR(0.05);
//        RuleSetBase.Significance sigFLFWER = actionRuleSet.getSourceRuleSet().calculateSignificanceFWER(0.05);
//        RuleSetBase.Significance sigFRFWER = actionRuleSet.getTargetRuleSet().calculateSignificanceFWER(0.05);
//
//        RuleSetBase.Significance sigBLFDR = backwardRuleSet.getSourceRuleSet().calculateSignificanceFDR(.05);
//        RuleSetBase.Significance sigBRFDR = backwardRuleSet.getTargetRuleSet().calculateSignificanceFDR(.05);
//        RuleSetBase.Significance sigBLFWER = backwardRuleSet.getSourceRuleSet().calculateSignificanceFWER(.05);
//        RuleSetBase.Significance sigBRFWER = backwardRuleSet.getTargetRuleSet().calculateSignificanceFWER(.05);
//
//        RuleSetBase.Significance sigFL = actionRuleSet.getSourceRuleSet().calculateSignificance(0.05);
//        RuleSetBase.Significance sigFR = actionRuleSet.getTargetRuleSet().calculateSignificance(0.05);
//        RuleSetBase.Significance sigBL = backwardRuleSet.getSourceRuleSet().calculateSignificance(0.05);
//        RuleSetBase.Significance sigBR = backwardRuleSet.getTargetRuleSet().calculateSignificance(0.05);
//
//        Files.write((actionRuleSet.toString() + formatSignificance(sigFL, sigFR, sigFLFDR, sigFRFDR, sigFLFWER, sigFRFWER) + System.lineSeparator()  + ser.serializeToCsv(actionRuleSet)).getBytes(), actionRulesFile);
//        Files.write((backwardRuleSet.toString() + formatSignificance(sigBL, sigBR, sigBLFDR, sigBRFDR, sigBLFWER, sigBRFWER) + System.lineSeparator() +ser.serializeToCsv(backwardRuleSet)).getBytes(), backwardActionRulesFile);
//
//        String trainDumpName = buildArffFileName (iteration, "train", dataFileDesc);
//        ArffFileWriter.write(train, trainDumpName);
//
//        String testDumpName = buildArffFileName(iteration, "test", dataFileDesc);
//        ArffFileWriter.write(predictedByClassificationRules, testDumpName);
//    }
//
//    private String formatSignificance(RuleSetBase.Significance sigFL, RuleSetBase.Significance sigFR, RuleSetBase.Significance sigFLFDR, RuleSetBase.Significance sigFRFDR, RuleSetBase.Significance sigFLFWER, RuleSetBase.Significance sigFRFWER) {
//        return "Significant source rules: " + sigFL.fraction + System.lineSeparator() +
//                "Significant target rules: " + sigFR.fraction + System.lineSeparator() +
//                "Significant source rules FDR: " + sigFLFDR.fraction + System.lineSeparator() +
//                "Significant target rules FDR: " + sigFRFDR.fraction + System.lineSeparator() +
//                "Significant source rules FWER: " + sigFLFWER.fraction + System.lineSeparator() +
//                "Significant target rules FWER: " + sigFRFWER.fraction + System.lineSeparator();
//    }
//
//    private IExampleSet getCoveredByRules(IExampleSet mutated) throws ExpressionEvaluationException {
//        IAttribute attribute = mutated.getAttributes().get("mutated_rule_id");
//        ICondition cnd = new AttributeValueFilterSingleCondition(attribute, AttributeValueFilterSingleCondition.NEQ1, "?");
//        return new ConditionedExampleSet(mutated, cnd);
//    }
//
//    private IExampleSet getCoveredByRecom(IExampleSet mutated) throws ExpressionEvaluationException {
//        IAttribute attribute = mutated.getAttributes().get("mutated_rule_id");
//        ICondition cnd = new AttributeValueFilterSingleCondition(attribute, AttributeValueFilterSingleCondition.NEQ1, "-1964.0");
//        return new ConditionedExampleSet(mutated, cnd);
//    }
//
//
//    private int getNumberOfPredictedExamples(IExampleSet set, String className){
//        IAttribute clazz = set.getAttributes().getLabel();
//        IAttribute pred = set.getAttributes().getPredictedLabel();
//        INominalMapping mapping = clazz.getMapping();
//        double classIdx = mapping.getIndex(className);
//        int cnt = 0;
//        for (Example ex : set) {
//            if (Double.compare(ex.getValue(pred), classIdx) == 0){
//                cnt++;
//            }
//        }
//        return cnt;
//    }
//
//}
