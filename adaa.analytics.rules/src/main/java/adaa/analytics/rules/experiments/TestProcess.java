package adaa.analytics.rules.experiments;

import adaa.analytics.rules.experiments.config.DatasetConfiguration;
import adaa.analytics.rules.experiments.config.ParamSetWrapper;
import adaa.analytics.rules.experiments.config.PredictElement;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.RulePerformanceEvaluator;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.*;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer5.operator.io.ModelLoader;
import utils.ArffFileLoader;
import utils.ArffFileWriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class TestProcess {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

    private ModelLoader modelLoader = null;

    private RoleConfigurator roleConfigurator;

    private AbstractPerformanceEvaluator evaluator;



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


    public void configure() throws OperatorCreationException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        evaluator = new RulePerformanceEvaluator(new OperatorDescription(
                "", "", RulePerformanceEvaluator.class, null, "", null));
        modelLoader = new ModelLoader(new OperatorDescription(
                "", "", ModelLoader.class, null, "", null));
        roleConfigurator = new RoleConfigurator(datasetConfiguration.label);

        List<String[]> roles = datasetConfiguration.generateRoles();

        if (datasetConfiguration.hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            String contrastAttr = datasetConfiguration.getOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

            // use annotation for storing contrast attribute info
            roleConfigurator.configureContrast(contrastAttr);

            evaluator.setEnabled(false);
        }

        if (roles.size() > 0) {
            roleConfigurator.configureRoles(roles);
        }
    }

    public void executeProcess() throws OperatorException, IOException, OperatorCreationException {

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

                long t1 = System.nanoTime();
                ExampleSet testEs = new ArffFileLoader().load(testFilePath, datasetConfiguration.label);
                roleConfigurator.apply(testEs);
                Model model = modelLoader.read();
                ExampleSet appliedEs = model.apply(testEs);

                PerformanceVector pv = evaluator.doWork(appliedEs);
                ArffFileWriter.write(appliedEs,predictionsFilePath);

                long t2 = System.nanoTime();
                double elapsedSec = (double) (t2 - t1) / 1e9;

                // Testing report
                generateTestReport(appliedEs, testFileName);

                // Performance log
                generatePerformanceReport((RuleSetBase) model, pv, testFileName, dateString, elapsedSec);

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
