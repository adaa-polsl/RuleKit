package adaa.analytics.rules.consoles;

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.consoles.config.ParamSetWrapper;
import adaa.analytics.rules.consoles.config.PredictElement;
import adaa.analytics.rules.logic.performance.MeasuredPerformance;
import adaa.analytics.rules.logic.performance.RulePerformanceCounter;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
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

    private RoleConfigurator roleConfigurator;

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

        roleConfigurator = new RoleConfigurator(datasetConfiguration.label);

        List<String[]> roles = datasetConfiguration.generateRoles();

        if (datasetConfiguration.hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            String contrastAttr = datasetConfiguration.getOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

            // use annotation for storing contrast attribute info
            roleConfigurator.configureContrast(contrastAttr);
        }

        if (roles.size() > 0) {
            roleConfigurator.configureRoles(roles);
        }
    }

    public void executeProcess() throws OperatorException, IOException, OperatorCreationException, ClassNotFoundException {

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


                long t1 = System.nanoTime();
                ExampleSet testEs = new ArffFileLoader().load(testFilePath, datasetConfiguration.label);
                roleConfigurator.apply(testEs);
                Model model = ModelFileInOut.read(modelFilePath);
                ExampleSet appliedEs = model.apply(testEs);

                List<MeasuredPerformance> pv = null;
                if (!datasetConfiguration.hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
                    RulePerformanceCounter rpc = new RulePerformanceCounter(appliedEs);
                    rpc.countValues();
                    pv = rpc.getResult();
                }
                ArffFileWriter.write(appliedEs,predictionsFilePath);

                long t2 = System.nanoTime();
                double elapsedSec = (double) (t2 - t1) / 1e9;

                // Testing report annotation header
                generateAnnotationHeader(appliedEs, testFileName);

                // Performance log
                generatePerformanceReport((RuleSetBase) model, pv, testFileName, dateString, elapsedSec);

                Logger.log(" [OK]\n", Level.INFO);
            }
        }
    }

    private void generateAnnotationHeader(ExampleSet predictions, String testFileName) throws IOException {
        if (predictions.getAnnotations().containsKey(RuleSetBase.ANNOTATION_TEST_REPORT)) {
            testingReport.append("================================================================================\n");
            testingReport.append(testFileName + "\n");
            testingReport.append(predictions.getAnnotations().get(RuleSetBase.ANNOTATION_TEST_REPORT));
            testingReport.append("\n\n");
        }

    }

    private void generatePerformanceReport(RuleSetBase model, List<MeasuredPerformance> performanceData, String testFileName, String dateString, double elapsedSec) throws IOException {
        List<MeasuredPerformance> performance = RulePerformanceCounter.recalculatePerformance(model);

        Logger.log(MeasuredPerformance.toString(performance) + "\n", Level.FINE);

        // generate headers
        StringBuilder performanceHeader = new StringBuilder("Dataset, time started, elapsed[s], ");
        StringBuilder row = new StringBuilder(testFileName + "," + dateString + "," + elapsedSec + ",");

        for(MeasuredPerformance pc: performance){
            performanceHeader.append(pc.getName()).append(",");
        }

        if (performanceData != null) {
            for(MeasuredPerformance pc: performanceData){
                performanceHeader.append(pc.getName()).append(",");
            }
        }

        for(MeasuredPerformance pc: performance){
            row.append(pc.getAverage()).append(", ");
        }

        if (performanceData != null) {
            for(MeasuredPerformance pc: performanceData){
                row.append(pc.getAverage()).append(", ");
            }
        }

        String configString = "Parameters: " + model.getParams().toString().replace("\n", "; ");
        performanceTable.add(new String[]{configString, performanceHeader.toString()}, row.toString());

    }
}
