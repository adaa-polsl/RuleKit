package adaa.analytics.rules.experiments;

import adaa.analytics.rules.consoles.ExperimentalConsole;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.RulePerformanceEvaluator;
import adaa.analytics.rules.utils.RapidMiner5;
import com.rapidminer.example.Attributes;
import com.rapidminer.operator.*;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.OperatorService;
import com.rapidminer5.operator.io.ArffExampleSetWriter;
import com.rapidminer5.operator.io.ArffExampleSource;
import com.rapidminer5.operator.io.ModelWriter;
import com.rapidminer5.operator.io.ModelLoader;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TrainTestValidationExperiment extends ExperimentBase {

    // General parameters
    private final String outDirPath;
    private final String labelAttribute;
    private final List<ExperimentalConsole.TrainElement> trainElements;
    private final List<ExperimentalConsole.PredictElement> predictElements;

    // Train proces operators
    private ArffExampleSource trainArff;
    private ArffExampleSource trainArff2;
    private ArffExampleSource testArff;
    private ModelWriter trainModelWriter = null;
    private ChangeAttributeRole trainRoleSetter = null;
    private ChangeAttributeRole trainRoleSetter2 = null;

    // Train process
    private com.rapidminer.Process processTest;

    // Test process operators
    private ModelLoader modelLoader = null;
    private ChangeAttributeRole testRoleSetter = null;
    private ArffExampleSetWriter testWriteArff = null;

    public TrainTestValidationExperiment(SynchronizedReport trainingReport, SynchronizedReport predictionPerformance,
                                         String labelAttribute, Map<String, String> options, Map<String, Object> params,
                                         String outDirPath, List<ExperimentalConsole.TrainElement> trainElements,
                                         List<ExperimentalConsole.PredictElement> predictElements){
        super(predictionPerformance, trainingReport, params);

        File f = new File(outDirPath);
        this.outDirPath = f.isAbsolute() ? outDirPath : (System.getProperty("user.dir") + "/" + outDirPath);
        this.labelAttribute = labelAttribute;
        this.trainElements = trainElements;
        this.predictElements = predictElements;

        try {
            this.paramsSets = new ArrayList<>();
            paramsSets.add(params);

            prepareTrainProcess();
            prepareTestProcess();

            // survival dataset - set proper role
            List<String[]> roles = new ArrayList<>();

            if (options.containsKey(SurvivalRule.SURVIVAL_TIME_ROLE)) {
                roles.add(new String[]{options.get(SurvivalRule.SURVIVAL_TIME_ROLE), SurvivalRule.SURVIVAL_TIME_ROLE});
            }

            if (options.containsKey(Attributes.WEIGHT_NAME)) {
                roles.add(new String[]{options.get(Attributes.WEIGHT_NAME), Attributes.WEIGHT_NAME});
            }

            if (roles.size() > 0) {
                trainRoleSetter.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
                trainRoleSetter2.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
                testRoleSetter.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void prepareTrainProcess() throws OperatorCreationException {

        // Training process
        trainArff = RapidMiner5.createOperator(ArffExampleSource.class);
        trainArff2 = RapidMiner5.createOperator(ArffExampleSource.class);
        ModelApplier trainApplier = OperatorService.createOperator(ModelApplier.class);
        trainRoleSetter2 = OperatorService.createOperator(ChangeAttributeRole.class);
        trainRoleSetter = OperatorService.createOperator(ChangeAttributeRole.class);
        RulePerformanceEvaluator trainValidationEvaluator2 = RapidMiner5.createOperator(RulePerformanceEvaluator.class);
        trainModelWriter = RapidMiner5.createOperator(ModelWriter.class);

        // configure train process
        process = new com.rapidminer.Process();
        process.getRootOperator().getSubprocess(0).addOperator(trainArff);
        process.getRootOperator().getSubprocess(0).addOperator(trainArff2);
        process.getRootOperator().getSubprocess(0).addOperator(trainRoleSetter);
        process.getRootOperator().getSubprocess(0).addOperator(trainRoleSetter2);
        process.getRootOperator().getSubprocess(0).addOperator(trainValidationEvaluator2);
        process.getRootOperator().getSubprocess(0).addOperator(ruleGenerator);
        process.getRootOperator().getSubprocess(0).addOperator(trainApplier);
        process.getRootOperator().getSubprocess(0).addOperator(trainModelWriter);

        trainArff.getOutputPorts().getPortByName("output").connectTo(trainRoleSetter.getInputPorts().getPortByName("example set input"));
        trainRoleSetter.getOutputPorts().getPortByName("example set output").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
        ruleGenerator.getOutputPorts().getPortByName("model").connectTo(trainModelWriter.getInputPorts().getPortByName("input"));
        trainArff2.getOutputPorts().getPortByName("output").connectTo(trainRoleSetter2.getInputPorts().getPortByName("example set input"));
        trainRoleSetter2.getOutputPorts().getPortByName("example set output").connectTo(trainApplier.getInputPorts().getPortByName("unlabelled data"));
        trainModelWriter.getOutputPorts().getPortByName("through").connectTo(trainApplier.getInputPorts().getPortByName("model"));
        trainApplier.getOutputPorts().getPortByName("labelled data").connectTo(trainValidationEvaluator2.getInputPorts().getPortByName("labelled data"));
        trainValidationEvaluator2.getOutputPorts().getPortByName("performance").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
        trainApplier.getOutputPorts().getPortByName("model").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));

        // configure role setter
        trainRoleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelAttribute);
        trainRoleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
        trainRoleSetter2.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelAttribute);
        trainRoleSetter2.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);

        trainModelWriter.setParameter(ModelWriter.PARAMETER_OUTPUT_TYPE, "2");
    }

    private void prepareTestProcess() throws OperatorCreationException {

        // Testing process
        testArff = RapidMiner5.createOperator(ArffExampleSource.class);
        testRoleSetter = OperatorService.createOperator(ChangeAttributeRole.class);
        ModelApplier applier = OperatorService.createOperator(ModelApplier.class);
        modelLoader = RapidMiner5.createOperator(ModelLoader.class);
        testWriteArff = RapidMiner5.createOperator(ArffExampleSetWriter.class);

        processTest = new com.rapidminer.Process();
        processTest.getRootOperator().getSubprocess(0).addOperator(testArff);
        processTest.getRootOperator().getSubprocess(0).addOperator(testRoleSetter);
        processTest.getRootOperator().getSubprocess(0).addOperator(applier);
        processTest.getRootOperator().getSubprocess(0).addOperator(validationEvaluator);
        processTest.getRootOperator().getSubprocess(0).addOperator(testWriteArff);
        processTest.getRootOperator().getSubprocess(0).addOperator(modelLoader);

        testArff.getOutputPorts().getPortByName("output").connectTo(testRoleSetter.getInputPorts().getPortByName("example set input"));
        testRoleSetter.getOutputPorts().getPortByName("example set output").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
        modelLoader.getOutputPorts().getPortByName("output").connectTo(applier.getInputPorts().getPortByName("model"));
        applier.getOutputPorts().getPortByName("labelled data").connectTo(validationEvaluator.getInputPorts().getPortByName("labelled data"));
        validationEvaluator.getOutputPorts().getPortByName("performance").connectTo(processTest.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
        validationEvaluator.getOutputPorts().getPortByName("example set").connectTo(testWriteArff.getInputPorts().getPortByName("input"));
        applier.getOutputPorts().getPortByName("model").connectTo(processTest.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));

        testRoleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelAttribute);
        testRoleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
    }

    @Override
    public void run() {

        try {

            Map<String, Object> params = paramsSets.get(0);

            for (String key: params.keySet()) {
                Object o = params.get(key);

                if (o instanceof String) {
                    ruleGenerator.setParameter(key, (String)o);
                } else if (o instanceof List) {
                    ruleGenerator.setListParameter(key, (List<String[]>)o);
                } else {
                    throw new InvalidParameterException();
                }
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

            // Train process
            for(ExperimentalConsole.TrainElement te : trainElements){

                File f = new File(te.modelFile);
                String modelFilePath = f.isAbsolute() ? te.modelFile : (outDirPath + "/" + te.modelFile);
                f = new File(te.inFile);
                String inFilePath = f.isAbsolute() ? te.inFile : (System.getProperty("user.dir") + "/" + te.inFile);

                f = new File(inFilePath);
                String trainFileName = f.getName();

                Logger.log("Train params: \n   Model file path: " + modelFilePath + "\n" +
                        "   Input file path: " + inFilePath + "\n", Level.FINE);

                trainModelWriter.setParameter(ModelWriter.PARAMETER_MODEL_FILE, modelFilePath);
                trainArff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, inFilePath);
                trainArff2.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, inFilePath);
                IOContainer out = process.run();
                IOObject[] objs = out.getIOObjects();

                PerformanceVector performance;

                // training report
                if (modelReport != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringUtils.repeat("=", 80));
                    sb.append("\n");
                    sb.append(trainFileName);
                    sb.append("\n\n");
                    Model model = (Model)objs[1];
                    sb.append(model.toString());

                    sb.append("\nModel characteristics:\n");
                    	
                    performance = RuleGenerator.recalculatePerformance((RuleSetBase)model);
                    for (String name : performance.getCriteriaNames()) {
                        double avg = performance.getCriterion(name).getAverage();
                        sb.append(name).append(": ").append(avg).append("\n");
                    }

                    sb.append("\nTraining set performance:\n");

                    performance = (PerformanceVector)objs[0];
                    // add performance
                    for (String name : performance.getCriteriaNames()) {
                        double avg = performance.getCriterion(name).getAverage();
                        sb.append(name).append(": ").append(avg).append("\n");
                    }

                    sb.append("\n\n");
                    modelReport.append(sb.toString());
                }
            }

            // Test process
            for(ExperimentalConsole.PredictElement pe : predictElements){

                Date begin = new Date();
                String dateString = dateFormat.format(begin);

                File f = new File(pe.modelFile);
                String modelFilePath = f.isAbsolute() ? pe.modelFile :
                        (outDirPath + "/" + pe.modelFile);
                f = new File(pe.predictionsFile);
                String predictionsFilePath = f.isAbsolute() ? pe.predictionsFile :
                        (outDirPath + "/" + pe.predictionsFile);
                f = new File(pe.testFile);
                String testFilePath = f.isAbsolute() ? pe.testFile :
                        (System.getProperty("user.dir") + "/" + pe.testFile);

                f = new File(testFilePath);
                String testFileName = f.getName();

                Logger.log("Test params: \n   Model file path:       " + modelFilePath + "\n" +
                        "   Predictions file path: " + predictionsFilePath + "\n" +
                        "   Test file path:        " + testFilePath + "\n", Level.FINE);

                modelLoader.setParameter(ModelLoader.PARAMETER_MODEL_FILE, modelFilePath);
                testArff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, testFilePath);
                testWriteArff.setParameter(ArffExampleSetWriter.PARAMETER_EXAMPLE_SET_FILE, predictionsFilePath);

                long t1 = System.nanoTime();
                IOContainer out = processTest.run();
                IOObject[] objs = out.getIOObjects();
                long t2 = System.nanoTime();
                double elapsedSec = (double)(t2 - t1) / 1e9;

                // Performance log
                if(qualityReport != null){

                    PerformanceVector testPerformance = (PerformanceVector)objs[0];
                	RuleSetBase model = (RuleSetBase)objs[1];
                	PerformanceVector performance = RuleGenerator.recalculatePerformance(model); 
                        	
                	for (String name : testPerformance.getCriteriaNames()) {
                		performance.addCriterion(testPerformance.getCriterion(name));
                	}
                	
                    String[] columns = performance.getCriteriaNames();

                    Logger.log(performance + "\n", Level.FINE);

                    // generate headers
                    StringBuilder performanceHeader = new StringBuilder("Dataset, time started, elapsed[s], ");
                    StringBuilder row = new StringBuilder(testFileName + "," + dateString + "," + elapsedSec + ",");

                    for (String name : columns) {
                        performanceHeader.append(name).append(",");
                    }

                    for (String name : performance.getCriteriaNames()) {
                        double avg = performance.getCriterion(name).getAverage();
                        row.append(avg).append(", ");
                    }

                    qualityReport.add(new String[] { ruleGenerator.toString(), performanceHeader.toString()}, row.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
