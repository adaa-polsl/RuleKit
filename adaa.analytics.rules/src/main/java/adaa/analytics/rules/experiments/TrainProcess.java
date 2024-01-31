package adaa.analytics.rules.experiments;

import adaa.analytics.rules.experiments.config.DatasetConfiguration;
import adaa.analytics.rules.experiments.config.ParamSetWrapper;
import adaa.analytics.rules.experiments.config.TrainElement;
import adaa.analytics.rules.logic.performance.RulePerformanceCounter;
import adaa.analytics.rules.logic.performance.MeasuredPerformance;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.RuleGenerator;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.PerformanceVector;
import org.apache.commons.lang.StringUtils;
import utils.ArffFileLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Level;

public class TrainProcess {
    private RoleConfigurator roleConfigurator;
    private ExpertRuleGenerator ruleGenerator = null;

    private DatasetConfiguration datasetConfiguration;

    private ParamSetWrapper paramSetWrapper;

    private SynchronizedReport trainingReport;

    private String outDirPath;

    public TrainProcess(DatasetConfiguration datasetConfiguration, ParamSetWrapper paramSetWrapper, SynchronizedReport trainingReport, String outDirPath) {
        this.datasetConfiguration = datasetConfiguration;
        this.paramSetWrapper = paramSetWrapper;
        this.trainingReport = trainingReport;
        this.outDirPath = outDirPath;
    }


    public void configure() throws  OperatorCreationException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        ruleGenerator = new ExpertRuleGenerator(new OperatorDescription(
                "", "", ExpertRuleGenerator.class, null, "", null));

        // configure role setter
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

        for (String key : paramSetWrapper.listKeys()) {
            Object o = paramSetWrapper.getParam(key);
            boolean paramOk = ruleGenerator.getParameters().getKeys().contains(key);

            if (paramOk)
                if (o instanceof String) {
                    ruleGenerator.setParameter(key, (String) o);
                } else if (o instanceof List) {
                    ruleGenerator.setListParameter(key, (List<String[]>) o);
                } else {
                    throw new InvalidParameterException("Invalid paramter type: " + key);
                }
            else {
                throw new InvalidParameterException("Undefined parameter: " + key);
            }
        }


    }

    public void executeProcess() throws OperatorException, IOException, OperatorCreationException {

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

                ExampleSet sourceEs = new ArffFileLoader().load(inFilePath, datasetConfiguration.label);
                roleConfigurator.apply(sourceEs);

                Model learnedModel = ruleGenerator.learn(sourceEs);
                ModelFileInOut.write(learnedModel, modelFilePath);

                writeModelToCsv(te.modelCsvFile, (RuleSetBase) learnedModel);

                ExampleSet appliedEs = learnedModel.apply(sourceEs);
                reportModelCharacteristic(learnedModel,  trainFileName);

                PerformanceVector pv = null;
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
    private void reportModelCharacteristic(Model model , String trainFileName) throws IOException {
        // training report
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.repeat("=", 80));
        sb.append("\n");
        sb.append(trainFileName);
        sb.append("\n\n");
        sb.append(model.toString());

        sb.append("\nModel characteristics:\n");

        RuleSetBase ruleModel = (RuleSetBase) model;
        PerformanceVector performance = RuleGenerator.recalculatePerformance(ruleModel);
        for (String name : performance.getCriteriaNames()) {
            double avg = performance.getCriterion(name).getAverage();
            sb.append(name).append(": ").append(avg).append("\n");
        }

        trainingReport.append(sb.toString());
    }

    private void reportTrainingPerformance(List<MeasuredPerformance> performanceData) throws IOException {
        // training report
        StringBuilder sb = new StringBuilder();
        // if evaluator is enabled
        sb.append("\nTraining set performance:\n");
        // add performance
        for (MeasuredPerformance pc : performanceData) {
            double avg = pc.getAverage();
            sb.append(pc.getName()).append(": ").append(avg).append("\n");
        }
        trainingReport.append(sb.toString());
    }

}
