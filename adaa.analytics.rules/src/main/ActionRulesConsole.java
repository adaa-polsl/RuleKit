package main;
import adaa.analytics.rules.logic.actions.ActionMetaTable;
import adaa.analytics.rules.logic.actions.ActionRangeDistribution;
import adaa.analytics.rules.logic.actions.OptimizedActionMetaTable;
import adaa.analytics.rules.logic.actions.recommendations.ClassificationRecommendationTask;
import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import org.apache.commons.cli.*;
import org.renjin.invoke.codegen.ArgumentException;
import org.renjin.repackaged.guava.io.Files;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ActionRulesConsole {

    private static final String TRAIN_FILE_OPT_NAME = "train";
    private static final String TEST_FILE_OPT_NAME = "test";
    private static final String MEASURE_OPT_NAME = "measure";
    private static final String MINCOV_OPT_NAME = "mincov";
    private static final String SOURCE_OPT_NAME = "source";
    private static final String TARGET_OPT_NAME = "target";

    private static final String[] ALLOWED_MEASURES = new String[] {"C2", "Correlation", "RSS", "Precision", "InformationGain", "WeightedLaplace"};
    public static final String DEFAULT_MINCOV = "5";
    public static final String DEFAULT_MEASURE = "C2";
    public static final String LABEL_OPT_NAME = "label";
    public static final String BACKWARD_OPT_NAME = "backward";

    private static CommandLine parseCommandLineParams(String[] args) {
        Options options = new Options();

        Option trainFile = Option.builder()
            .required(true)
            .argName("tr")
            .longOpt(TRAIN_FILE_OPT_NAME)
            .hasArg()
            .desc("Path to train file. If test file is not provided, train file be used in test phase.")
            .build();
        options.addOption(trainFile);

        Option testFile = Option.builder()
                .required(false)
                .argName("ts")
                .longOpt(TEST_FILE_OPT_NAME)
                .hasArg()
                .desc("Path to test file ")
                .build();
        options.addOption(testFile);

        Option inductionMeasure = Option.builder()
                .required(false)
                .argName("m")
                .longOpt(MEASURE_OPT_NAME)
                .hasArg()
                .desc("Induction measure to use during induction and pruning. Defaults to C2.")
                .build();
        options.addOption(inductionMeasure);

        Option minCov = Option.builder()
                .required(false)
                .argName("c")
                .longOpt(MINCOV_OPT_NAME)
                .hasArg()
                .desc("Minimum coverage")
                .build();
        options.addOption(minCov);

        Option source = Option.builder()
                .required(true)
                .argName("s")
                .longOpt(SOURCE_OPT_NAME)
                .hasArg()
                .desc("Source class name")
                .build();
        options.addOption(source);

        Option target = Option.builder()
                .required(false)
                .argName("t")
                .longOpt(TARGET_OPT_NAME)
                .hasArg()
                .desc("Target class name")
                .build();
        options.addOption(target);

        Option label = Option.builder()
                .required(false)
                .argName("l")
                .longOpt(LABEL_OPT_NAME)
                .hasArg()
                .desc("Name of label attribute (default to \"class\"")
                .build();
        options.addOption(label);

        Option backward = Option.builder()
                .required(false)
                .argName("d")
                .longOpt(BACKWARD_OPT_NAME)
                .longOpt("If this switch is set, the algorithm works in backward mode")
                .build();
        options.addOption(backward);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            formatter.printHelp("AC-rules", options);

            System.exit(1);
        }
        return cmd;
    }

    private static void validateFilename(String name) {
        if (!name.endsWith(".arff")) {
            throw new ArgumentException("The data files must be in ARFF format and with .arff extension");
        }
    }

    private static String getTrainFilename(CommandLine cmd) {
        String name = cmd.getOptionValue(TRAIN_FILE_OPT_NAME);
        validateFilename(name);
        return name;
    }

    private static String getTestFilename(CommandLine cmd) {
        String name = cmd.getOptionValue(TEST_FILE_OPT_NAME);
        if (name == null) return getTrainFilename(cmd);
        validateFilename(name);
        return name;
    }

    private static ClassificationMeasure getMeasureName(CommandLine cmd) {
        String readValue = cmd.getOptionValue(MEASURE_OPT_NAME, DEFAULT_MEASURE);

        if (!Arrays.asList(ALLOWED_MEASURES).contains(readValue)) {
            throw new ArgumentException(readValue + "is not in allowed list of measures. Choose from: {" + String.join(", ", Arrays.asList(ALLOWED_MEASURES)) + "}");
        }

        int id = Arrays.asList(ClassificationMeasure.NAMES).indexOf(readValue);
        if (id < 0) throw new ArgumentException("unrecognized measure name");

        return new ClassificationMeasure(id);
    }

    private static int getMincov(CommandLine cmd) {
        String readValue = cmd.getOptionValue(MINCOV_OPT_NAME, DEFAULT_MINCOV);

        int parsedValue = Integer.parseInt(readValue);
        if (parsedValue < 1) {
            throw new ArgumentException("Mincov has to be at least 1");
        }
        return parsedValue;
    }

    private static boolean getBackward(CommandLine cmd) {
        return cmd.hasOption(BACKWARD_OPT_NAME);
    }

    private static String getClassname(CommandLine cmd) {
        return cmd.getOptionValue(LABEL_OPT_NAME, "class");
    }

    private static String getSourceClassname(CommandLine cmd, ExampleSet examples) {
        String read = cmd.getOptionValue(SOURCE_OPT_NAME);
        if (!examples.getAttributes().getLabel().getMapping().getValues().contains(read)) {
            throw new ArgumentException("The requested source class name: " + read + " was not found in train set examples.");
        }
        return read;
    }

    private static String getTargetClassname(CommandLine cmd, ExampleSet examples) {
        String read = cmd.getOptionValue(TARGET_OPT_NAME);

        if (read == null) {
            NominalMapping mapping = examples.getAttributes().getLabel().getMapping();
            if (mapping.size() > 2) throw new ArgumentException("The exampleset contains more then two class. Target class have to be specified");
            String source = getSourceClassname(cmd, examples);
            for (String value : mapping.getValues()) {
                if (!value.equals(source)) return value;
            }
        }

        if (!examples.getAttributes().getLabel().getMapping().getValues().contains(read)) {
            throw new ArgumentException("The requested target class name: " + read + " was not found in train set examples.");
        }
        return read;
    }

    private static ExampleSet loadExampleSet(String fileName, String labelName)  {
        ExampleSet set = null;
        ExamplesetFileLoader loader = null;
        if (fileName.endsWith(".arff")) {
            loader = new ArffFileLoader();
        } else if (fileName.endsWith(".csv")) {
            loader = new CsvFileLoader();
        } else {
            throw new ArgumentException("Only .arff or .csv files are supported");
        }

        try {
            set = loader.load(fileName, labelName);
        } catch (Exception ex) {
            System.out.println("Couldn't load file: " + fileName + ". Reason: " + ex.getMessage());
            System.exit(1);
        }
        return set;
    }

    private static ActionSnC getAlgorithm(boolean backward, ActionFinder finder, ActionInductionParameters params) {
        if (backward) {
            return new BackwardActionSnC(finder, params);
        } else {
            return new ActionSnC(finder, params);
        }
    }

    public static void main(String[] args) throws OperatorException, OperatorCreationException, IOException {

        CommandLine cmdParams = parseCommandLineParams(args);

        if (!RapidMiner.isInitialized()) RapidMiner.init();

        Mutator mutator;
        try {
            mutator = new Mutator();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't load RapidMiner operators. Reason: " + ex.getMessage());
        }

        ClassificationMeasure measure = getMeasureName(cmdParams);
        int mincov = getMincov(cmdParams);

        ExampleSet trainSet = loadExampleSet(getTrainFilename(cmdParams), getClassname(cmdParams));
        ExampleSet testSet = loadExampleSet(getTestFilename(cmdParams), getClassname(cmdParams));

        String source = getSourceClassname(cmdParams, trainSet);
        String target = getTargetClassname(cmdParams, trainSet);

        ActionFindingParameters findingParameters = new ActionFindingParameters();
        findingParameters.setUseNotIntersectingRangesOnly(ActionFindingParameters.RangeUsageStrategy.ALL);

        ActionInductionParameters params = new ActionInductionParameters(findingParameters);
        params.setMinimumCovered(mincov);
        params.setInductionMeasure(measure);
        params.setPruningMeasure(measure);
        params.setVotingMeasure(measure);

        params.setMaximumUncoveredFraction(0.05);
        params.setEnablePruning(true);
        params.setIgnoreMissing(true);


        //search for source -> target rules
        params.addClasswiseTransition(source, target);

        //Configure list of stable attributes with a code below
        //params.setStableAttributes(...);


        // Train action rules
        ActionFinder finder = new ActionFinder(params);
        ActionSnC snc = getAlgorithm(getBackward(cmdParams), finder, params);
        ActionRuleSet rulesOnTrain = (ActionRuleSet) snc.run(trainSet);

        // Use trained action rules to create the recommendation engine
        ActionRangeDistribution distribution = new ActionRangeDistribution(rulesOnTrain, trainSet);
        distribution.calculateActionDistribution();
        int sourceId = trainSet.getAttributes().getLabel().getMapping().getIndex(source);
        int targetId = trainSet.getAttributes().getLabel().getMapping().getIndex(target);
        ClassificationRecommendationTask crt = new ClassificationRecommendationTask(true, false, measure, sourceId, targetId);
        ActionMetaTable metaTable = new OptimizedActionMetaTable(distribution, params.getStableAttributes());

        //To run recommendations, we will extract source class examples only
        Condition cnd = new AttributeValueFilterSingleCondition(testSet.getAttributes().getLabel(), AttributeValueFilterSingleCondition.EQUALS, source);
        ExampleSet sourceExamplesInTestSet  = mutator.materializeExamples(new ConditionedExampleSet(testSet, cnd));

        // Mutate source test example according to action rules
        ExampleSet testSetMutatedByActionRules = mutator.mutateExamples(sourceExamplesInTestSet, rulesOnTrain, trainSet, target);


        //Mutate source test examples using recommendation method
        //create container for used recommendations to be presented later
        ActionRuleSet recommendations = new ActionRuleSet(sourceExamplesInTestSet, true, params, null);

        ExampleSet testSetMutatedByRecommendations = mutator.mutateExamples(sourceExamplesInTestSet, metaTable, source, target, recommendations, trainSet, crt);

        // Write results, using train file name as a basis

        String fileNameBase = String.join("_", Files.getNameWithoutExtension(getTrainFilename(cmdParams)), Integer.toString(mincov), measure.getName());

        String mutatedByActions = String.join("_", fileNameBase, "action_rules", "mutated") + ".examples";
        String mutatedByRecoms = String.join("_", fileNameBase, "recommendation", "mutated") + ".examples";
        String recommFile = fileNameBase + ".recommendations";
        String rulesFile = fileNameBase + ".rules";

        ArffFileWriter.write(testSetMutatedByActionRules, mutatedByActions);
        ArffFileWriter.write(testSetMutatedByRecommendations, mutatedByRecoms);

        Files.write(recommendations.toString().getBytes(), new File(recommFile));
        Files.write(rulesOnTrain.toString().getBytes(), new File(rulesFile));

    }
}
