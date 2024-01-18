package adaa.analytics.rules.experiments;

import adaa.analytics.rules.experiments.config.DatasetConfiguration;
import adaa.analytics.rules.experiments.config.ParamSetWrapper;
import adaa.analytics.rules.experiments.config.TrainElement;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.operator.ChangeAttributeRoleAndAnnotate;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.RulePerformanceEvaluator;
import adaa.analytics.rules.utils.RapidMiner5;
import com.rapidminer.example.Attributes;
import com.rapidminer.operator.*;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer5.operator.io.ArffExampleSource;
import com.rapidminer5.operator.io.ModelWriter;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Level;

public class TrainProcess {
    private ArffExampleSource arff;
    private ArffExampleSource arff2;
    private ModelWriter modelWriter = null;
    private ModelApplier applier;
    private ChangeAttributeRoleAndAnnotate roleSetter = null;
    private ChangeAttributeRoleAndAnnotate roleSetter2 = null;
    private RuleGenerator ruleGenerator = null;
    private RulePerformanceEvaluator evaluator;

    private com.rapidminer.Process process;

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

    private void prepareProcess() throws OperatorCreationException {

        arff = RapidMiner5.createOperator(ArffExampleSource.class);
        arff2 = RapidMiner5.createOperator(ArffExampleSource.class);
        applier = OperatorService.createOperator(ModelApplier.class);
        roleSetter2 = RapidMiner5.createOperator(ChangeAttributeRoleAndAnnotate.class);
        roleSetter = RapidMiner5.createOperator(ChangeAttributeRoleAndAnnotate.class);
        evaluator = RapidMiner5.createOperator(RulePerformanceEvaluator.class);
        modelWriter = RapidMiner5.createOperator(ModelWriter.class);
        ruleGenerator = RapidMiner5.createOperator(ExpertRuleGenerator.class);

        // configure train process
        process = new com.rapidminer.Process();
        process.getRootOperator().getSubprocess(0).addOperator(arff);
        process.getRootOperator().getSubprocess(0).addOperator(arff2);
        process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
        process.getRootOperator().getSubprocess(0).addOperator(roleSetter2);
        process.getRootOperator().getSubprocess(0).addOperator(evaluator);
        process.getRootOperator().getSubprocess(0).addOperator(ruleGenerator);
        process.getRootOperator().getSubprocess(0).addOperator(applier);
        process.getRootOperator().getSubprocess(0).addOperator(modelWriter);

        process.getRootOperator().setParameter(ProcessRootOperator.PARAMETER_LOGVERBOSITY, "" + LogService.OFF);

        arff.getOutputPorts().getPortByName("output").connectTo(roleSetter.getInputPorts().getPortByName("example set input"));
        roleSetter.getOutputPorts().getPortByName("example set output").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
        ruleGenerator.getOutputPorts().getPortByName("model").connectTo(modelWriter.getInputPorts().getPortByName("input"));
        arff2.getOutputPorts().getPortByName("output").connectTo(roleSetter2.getInputPorts().getPortByName("example set input"));
        roleSetter2.getOutputPorts().getPortByName("example set output").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
        modelWriter.getOutputPorts().getPortByName("through").connectTo(applier.getInputPorts().getPortByName("model"));
        applier.getOutputPorts().getPortByName("labelled data").connectTo(evaluator.getInputPorts().getPortByName("labelled data"));

        applier.getOutputPorts().getPortByName("model").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
        evaluator.getOutputPorts().getPortByName("performance").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));

        // configure role setter
        roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, datasetConfiguration.label);
        roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
        roleSetter2.setParameter(ChangeAttributeRole.PARAMETER_NAME, datasetConfiguration.label);
        roleSetter2.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);

        modelWriter.setParameter(ModelWriter.PARAMETER_OUTPUT_TYPE, "2");
    }

    public void configure() throws UndefinedParameterError, OperatorCreationException {
        prepareProcess();

        List<String[]> roles = datasetConfiguration.generateRoles();

        if (datasetConfiguration.hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            String contrastAttr = datasetConfiguration.getOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

            // use annotation for storing contrast attribute info
            roleSetter.setParameter(ChangeAttributeRoleAndAnnotate.PARAMETER_ANNOTATION_NAME, ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
            roleSetter.setParameter(ChangeAttributeRoleAndAnnotate.PARAMETER_ANNOTATION_VALUE, contrastAttr);
            roleSetter2.setParameter(ChangeAttributeRoleAndAnnotate.PARAMETER_ANNOTATION_NAME, ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
            roleSetter2.setParameter(ChangeAttributeRoleAndAnnotate.PARAMETER_ANNOTATION_VALUE, contrastAttr);

            evaluator.setEnabled(false);
        }

        if (roles.size() > 0) {
            roleSetter.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
            roleSetter2.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
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
                throw new UndefinedParameterError(key, "Undefined parameter: " + key);
            }
        }


    }

    public void executeProcess() throws OperatorException, IOException {

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

                modelWriter.setParameter(ModelWriter.PARAMETER_MODEL_FILE, modelFilePath);
                arff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, inFilePath);
                arff2.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, inFilePath);
                IOContainer out = process.run();
                IOObject[] objs = out.getIOObjects();

                generateModelReport(te, (RuleSetBase) objs[0]);
                generateTrainingReport(objs, objs.length > 1 ? (PerformanceVector) objs[1] : null, trainFileName);
            }
        }
    }

    private void generateModelReport(TrainElement te, RuleSetBase model) throws IOException {
        if (te.modelCsvFile != null) {
            File f = new File(te.modelCsvFile);
            String csvFilePath = f.isAbsolute() ? te.modelCsvFile : (outDirPath + "/" + te.modelCsvFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
            writer.write(model.toTable());
            writer.close();
        }
    }

    private void generateTrainingReport(IOObject[] objs, PerformanceVector trainingSetPerformance, String trainFileName) throws IOException {
        // training report
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.repeat("=", 80));
        sb.append("\n");
        sb.append(trainFileName);
        sb.append("\n\n");
        Model model = (Model) objs[0];
        sb.append(model.toString());

        sb.append("\nModel characteristics:\n");

        RuleSetBase ruleModel = (RuleSetBase) model;
        PerformanceVector performance = RuleGenerator.recalculatePerformance(ruleModel);
        for (String name : performance.getCriteriaNames()) {
            double avg = performance.getCriterion(name).getAverage();
            sb.append(name).append(": ").append(avg).append("\n");
        }

        if (trainingSetPerformance != null) {
            // if evaluator is enabled
            sb.append("\nTraining set performance:\n");
            // add performance
            for (String name : trainingSetPerformance.getCriteriaNames()) {
                double avg = trainingSetPerformance.getCriterion(name).getAverage();
                sb.append(name).append(": ").append(avg).append("\n");
            }
        }

        sb.append("\n\n");
        trainingReport.append(sb.toString());
        Logger.log(" [OK]\n", Level.INFO);

    }
}
