package adaa.analytics.rules.consoles;

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.consoles.config.ParamSetConfiguration;
import adaa.analytics.rules.consoles.config.TrainElement;
import adaa.analytics.rules.logic.performance.PerformanceResult;
import adaa.analytics.rules.logic.performance.RulePerformanceCounter;
import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import adaa.analytics.rules.utils.Logger;
import adaa.analytics.rules.logic.representation.ruleset.RuleSetBase;
import adaa.analytics.rules.logic.rulegenerator.RuleGenerator;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.utils.OperatorException;
import org.apache.commons.lang3.StringUtils;
import ioutils.ArffFileLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class TrainProcess {
    private RuleGenerator ruleGenerator = null;

    private DatasetConfiguration datasetConfiguration;

    private ParamSetConfiguration paramSetWrapper;

    private SynchronizedReport trainingReport;

    private String outDirPath;

    public TrainProcess(DatasetConfiguration datasetConfiguration, ParamSetConfiguration paramSetWrapper, SynchronizedReport trainingReport, String outDirPath) {
        this.datasetConfiguration = datasetConfiguration;
        this.paramSetWrapper = paramSetWrapper;
        this.trainingReport = trainingReport;
        this.outDirPath = outDirPath;
    }


    public void executeProcess() throws IOException, OperatorException {
        ruleGenerator = new RuleGenerator();
        ruleGenerator.setRuleGeneratorParams(paramSetWrapper.generateRuleGeneratorParams());


        // Train process
        if (datasetConfiguration.trainElements.size() > 0) {
            Logger.log("TRAINING\n"
                    + "Log file: " + trainingReport.getFile() + "\n", Level.INFO);

            for (TrainElement te : datasetConfiguration.trainElements) {
                Logger.log("Building model " + te.modelFile + " from dataset " + te.inFile + "\n", Level.INFO);
                File f = new File(te.modelFile);
                String modelFilePath = f.isAbsolute() ? te.modelFile : (outDirPath + "/" + te.modelFile);
                f = new File(te.inFile);
                String inFilePath = f.isAbsolute() ? te.inFile : (System.getProperty("user.dir") + "/" + te.inFile);

                f = new File(inFilePath);
                String trainFileName = f.getName();

                Logger.log("Train params: \n   Model file path: " + modelFilePath + "\n" +
                        "   Input file path: " + inFilePath + "\n", Level.FINE);

                IExampleSet sourceEs = new ArffFileLoader().load(inFilePath, datasetConfiguration.label);
                datasetConfiguration.applyParametersToExempleSet(sourceEs);

                RuleSetBase learnedModel = ruleGenerator.learn(sourceEs);
                ModelFileInOut.write(learnedModel, modelFilePath);

                writeModelToCsv(te.modelCsvFile, (RuleSetBase) learnedModel);

                IExampleSet appliedEs = learnedModel.apply(sourceEs);
                reportModelCharacteristic(learnedModel,  trainFileName);

                if (!datasetConfiguration.hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
                    RulePerformanceCounter rpc = new RulePerformanceCounter(appliedEs);
                    rpc.countValues();
                    reportTrainingPerformance(rpc.getResult());
                }

                Logger.log(" [OK]\n", Level.INFO);
            }
        }
    }

    private void writeModelToCsv(String modelCsvFile, RuleSetBase model) throws IOException {
        if (modelCsvFile != null) {
            File f = new File(modelCsvFile);
            String csvFilePath = f.isAbsolute() ? modelCsvFile : (outDirPath + "/" + modelCsvFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
            writer.write(model.toTable());
            writer.close();
        }
    }
    private void reportModelCharacteristic(RuleSetBase ruleModel , String trainFileName) throws IOException {
        // training report
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.repeat("=", 80));
        sb.append("\n");
        sb.append(trainFileName);
        sb.append("\n\n");
        sb.append(ruleModel.toString());

        sb.append("\nModel characteristics:\n");

        List<PerformanceResult> performance = RulePerformanceCounter.recalculatePerformance(ruleModel);
        for (PerformanceResult mp: performance) {
            sb.append(mp.getName()).append(": ").append(mp.getValue()).append("\n");
        }

        trainingReport.append(sb.toString());
    }

    private void reportTrainingPerformance(List<PerformanceResult> performanceData) throws IOException {
        // training report
        StringBuilder sb = new StringBuilder();
        // if evaluator is enabled
        sb.append("\nTraining set performance:\n");
        // add performance
        for (PerformanceResult pc : performanceData) {
            double avg = pc.getValue();
            sb.append(pc.getName()).append(": ").append(avg).append("\n");
        }
        trainingReport.append(sb.toString());
    }

}
