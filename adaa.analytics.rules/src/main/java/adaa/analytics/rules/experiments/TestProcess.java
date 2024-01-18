package adaa.analytics.rules.experiments;

import adaa.analytics.rules.experiments.config.DatasetConfiguration;
import adaa.analytics.rules.experiments.config.ParamSetWrapper;
import adaa.analytics.rules.experiments.config.PredictElement;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.operator.ChangeAttributeRoleAndAnnotate;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.RulePerformanceEvaluator;
import adaa.analytics.rules.utils.RapidMiner5;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.*;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer5.operator.io.ArffExampleSetWriter;
import com.rapidminer5.operator.io.ArffExampleSource;
import com.rapidminer5.operator.io.ModelLoader;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class TestProcess {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
    private ArffExampleSource arff;
    private ModelLoader modelLoader = null;
    private ChangeAttributeRole roleSetter = null;
    private ArffExampleSetWriter writeArff = null;
    private AbstractPerformanceEvaluator evaluator;

    private com.rapidminer.Process process;


    private DatasetConfiguration datasetConfiguration;

    private ParamSetWrapper paramSetWrapper;

    private SynchronizedReport testingReport;

    private SynchronizedReport performanceTable;

    private String outDirPath;


    public TestProcess(DatasetConfiguration datasetConfiguration, ParamSetWrapper paramSetWrapper, SynchronizedReport testingReport, SynchronizedReport performanceTable, String outDirPath) {
        this.datasetConfiguration = datasetConfiguration;
        this.paramSetWrapper = paramSetWrapper;
        this.testingReport = testingReport;
        this.performanceTable = performanceTable;
        this.outDirPath = outDirPath;
    }

    private void prepareProcess() throws OperatorCreationException {
        arff = RapidMiner5.createOperator(ArffExampleSource.class);
        roleSetter = OperatorService.createOperator(ChangeAttributeRole.class);
        ModelApplier applier = OperatorService.createOperator(ModelApplier.class);
        modelLoader = RapidMiner5.createOperator(ModelLoader.class);
        writeArff = RapidMiner5.createOperator(ArffExampleSetWriter.class);
        evaluator = RapidMiner5.createOperator(RulePerformanceEvaluator.class);

        process = new com.rapidminer.Process();
        process.getRootOperator().getSubprocess(0).addOperator(arff);
        process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
        process.getRootOperator().getSubprocess(0).addOperator(applier);
        process.getRootOperator().getSubprocess(0).addOperator(evaluator);
        process.getRootOperator().getSubprocess(0).addOperator(writeArff);
        process.getRootOperator().getSubprocess(0).addOperator(modelLoader);

        process.getRootOperator().setParameter(ProcessRootOperator.PARAMETER_LOGVERBOSITY, "" + LogService.OFF);


        arff.getOutputPorts().getPortByName("output").connectTo(roleSetter.getInputPorts().getPortByName("example set input"));
        roleSetter.getOutputPorts().getPortByName("example set output").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
        modelLoader.getOutputPorts().getPortByName("output").connectTo(applier.getInputPorts().getPortByName("model"));
        applier.getOutputPorts().getPortByName("labelled data").connectTo(evaluator.getInputPorts().getPortByName("labelled data"));
        applier.getOutputPorts().getPortByName("model").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));

        evaluator.getOutputPorts().getPortByName("performance").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));
        evaluator.getOutputPorts().getPortByName("example set").connectTo(writeArff.getInputPorts().getPortByName("input"));
        writeArff.getOutputPorts().getPortByName("through").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(2));

        roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, datasetConfiguration.label);
        roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);

    }

    public void configure() throws OperatorCreationException {
        prepareProcess();
        List<String[]> roles = datasetConfiguration.generateRoles();

        if (datasetConfiguration.hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            String contrastAttr = datasetConfiguration.getOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

            // use annotation for storing contrast attribute info
            roleSetter.setParameter(ChangeAttributeRoleAndAnnotate.PARAMETER_ANNOTATION_NAME, ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
            roleSetter.setParameter(ChangeAttributeRoleAndAnnotate.PARAMETER_ANNOTATION_VALUE, contrastAttr);

            evaluator.setEnabled(false);
            writeArff.setEnabled(false);
        }

        if (roles.size() > 0) {
            roleSetter.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
        }
    }

    public void executeProcess() throws OperatorException, IOException {

        // Test process
        if (datasetConfiguration.predictElements.size() > 0) {
            Logger.log("PREDICTION\n" + "Performance file: " + performanceTable.getFile() + "\n", Level.INFO);
            for (PredictElement pe : datasetConfiguration.predictElements) {
                Logger.log("Applying model " + pe.modelFile + " on " + pe.testFile + ", saving predictions in " + pe.testFile, Level.INFO);
                Date begin = new Date();
                String dateString = dateFormat.format(begin);

                File f = new File(pe.modelFile);
                String modelFilePath = f.isAbsolute() ? pe.modelFile : (outDirPath + "/" + pe.modelFile);
                f = new File(pe.predictionsFile);
                String predictionsFilePath = f.isAbsolute() ? pe.predictionsFile : (outDirPath + "/" + pe.predictionsFile);
                f = new File(pe.testFile);
                String testFilePath = f.isAbsolute() ? pe.testFile : (System.getProperty("user.dir") + "/" + pe.testFile);

                f = new File(testFilePath);
                String testFileName = f.getName();

                Logger.log("Test params: \n   Model file path:       " + modelFilePath + "\n" + "   Predictions file path: " + predictionsFilePath + "\n" + "   Test file path:        " + testFilePath + "\n", Level.FINE);

                modelLoader.setParameter(ModelLoader.PARAMETER_MODEL_FILE, modelFilePath);
                arff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, testFilePath);
                writeArff.setParameter(ArffExampleSetWriter.PARAMETER_EXAMPLE_SET_FILE, predictionsFilePath);

                long t1 = System.nanoTime();
                IOContainer out = process.run();
                IOObject[] objs = out.getIOObjects();
                long t2 = System.nanoTime();
                double elapsedSec = (double) (t2 - t1) / 1e9;

                // Testing report
                generateTestReport((ExampleSet) objs[2], testFileName);

                // Performance log
                generatePerformanceReport((RuleSetBase) objs[0], (PerformanceVector) objs[1], testFileName, dateString, elapsedSec);

                Logger.log(" [OK]\n", Level.INFO);
            }
        }
    }

    private void generateTestReport(ExampleSet predictions, String testFileName) throws IOException {
        if (predictions.getAnnotations().containsKey(RuleSetBase.ANNOTATION_TEST_REPORT)) {
            testingReport.append("================================================================================\n");
            testingReport.append(testFileName + "\n");
            testingReport.append(predictions.getAnnotations().get(RuleSetBase.ANNOTATION_TEST_REPORT));
            testingReport.append("\n\n");
        }

    }

    private void generatePerformanceReport(RuleSetBase model, PerformanceVector testPerformance, String testFileName, String dateString, double elapsedSec) throws IOException {
        PerformanceVector performance = RuleGenerator.recalculatePerformance(model);

        // if evaluator is enabled
        if (testPerformance != null) {
            for (String name : testPerformance.getCriteriaNames()) {
                performance.addCriterion(testPerformance.getCriterion(name));
            }
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

        String configString = "Parameters: " + model.getParams().toString().replace("\n", "; ");
        performanceTable.add(new String[]{configString, performanceHeader.toString()}, row.toString());

    }
}
