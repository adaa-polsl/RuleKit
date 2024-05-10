package adaa.analytics.rules.consoles;

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.consoles.config.ParamSetConfiguration;
import adaa.analytics.rules.consoles.config.PredictElement;
import adaa.analytics.rules.logic.performance.PerformanceResult;
import adaa.analytics.rules.logic.performance.RulePerformanceCounter;
import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import adaa.analytics.rules.utils.Logger;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.utils.OperatorException;
import ioutils.ArffFileLoader;
import ioutils.ArffFileWriter;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class TestProcess {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

    private DatasetConfiguration datasetConfiguration;

    private ParamSetConfiguration paramSetWrapper;

    private SynchronizedReport testingReport;

    private SynchronizedReport performanceTable;

    private String outDirPath;


    public TestProcess(DatasetConfiguration datasetConfiguration, ParamSetConfiguration paramSetWrapper, SynchronizedReport testingReport, SynchronizedReport performanceTable, String outDirPath) {
        this.datasetConfiguration = datasetConfiguration;
        this.paramSetWrapper = paramSetWrapper;
        this.testingReport = testingReport;
        this.performanceTable = performanceTable;
        this.outDirPath = outDirPath;
    }


    public void executeProcess() throws OperatorException, IOException, ClassNotFoundException {

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
                IExampleSet testEs = new ArffFileLoader().load(testFilePath, datasetConfiguration.label);
                datasetConfiguration.applyParametersToExempleSet(testEs);

                RuleSetBase model = ModelFileInOut.read(modelFilePath);
                IExampleSet appliedEs = model.apply(testEs);

                List<PerformanceResult> pv = null;
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

    private void generateAnnotationHeader(IExampleSet predictions, String testFileName) throws IOException {
        if (predictions.getAnnotations().containsKey(RuleSetBase.ANNOTATION_TEST_REPORT)) {
            testingReport.append("================================================================================\n");
            testingReport.append(testFileName + "\n");
            testingReport.append(predictions.getAnnotations().getAnnotation(RuleSetBase.ANNOTATION_TEST_REPORT));
            testingReport.append("\n\n");
        }

    }

    private void generatePerformanceReport(RuleSetBase model, List<PerformanceResult> performanceData, String testFileName, String dateString, double elapsedSec) throws IOException {
        List<PerformanceResult> performance = RulePerformanceCounter.recalculatePerformance(model);

        Logger.log(PerformanceResult.toString(performance) + "\n", Level.FINE);

        // generate headers
        StringBuilder performanceHeader = new StringBuilder("Dataset, time started, elapsed[s], ");
        StringBuilder row = new StringBuilder(testFileName + "," + dateString + "," + elapsedSec + ",");

        for(PerformanceResult pc: performance){
            performanceHeader.append(pc.getName()).append(",");
        }

        if (performanceData != null) {
            for(PerformanceResult pc: performanceData){
                performanceHeader.append(pc.getName()).append(",");
            }
        }

        for(PerformanceResult pc: performance){
            row.append(pc.getValue()).append(", ");
        }

        if (performanceData != null) {
            for(PerformanceResult pc: performanceData){
                row.append(pc.getValue()).append(", ");
            }
        }

        String configString = "Parameters: " + model.getParams().toString().replace("\n", "; ");
        performanceTable.add(new String[]{configString, performanceHeader.toString()}, row.toString());

    }
}
