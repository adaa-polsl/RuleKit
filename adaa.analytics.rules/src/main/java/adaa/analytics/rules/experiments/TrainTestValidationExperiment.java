/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.experiments;

import adaa.analytics.rules.consoles.ExperimentalConsole;
import adaa.analytics.rules.logic.representation.DoubleFormatter;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.RulePerformanceEvaluator;
import adaa.analytics.rules.utils.RapidMiner5;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.*;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer5.operator.io.ArffExampleSetWriter;
import com.rapidminer5.operator.io.ArffExampleSource;
import com.rapidminer5.operator.io.ModelWriter;
import com.rapidminer5.operator.io.ModelLoader;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.sun.tools.javac.util.Pair;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TrainTestValidationExperiment implements Runnable{

    public static final String RULES_SIGNIFICANT_FIGURES = "rules_significant_figures";


    // Train proces operators
    private class TrainProcessWrapper {
        public ArffExampleSource arff;
        public ArffExampleSource arff2;
        public ModelWriter modelWriter = null;
        public ChangeAttributeRole roleSetter = null;
        public ChangeAttributeRole roleSetter2 = null;
        public RuleGenerator ruleGenerator = null;

        public com.rapidminer.Process process;


        public TrainProcessWrapper() throws OperatorCreationException {
            arff = RapidMiner5.createOperator(ArffExampleSource.class);
            arff2 = RapidMiner5.createOperator(ArffExampleSource.class);
            ModelApplier trainApplier = OperatorService.createOperator(ModelApplier.class);
            roleSetter2 = OperatorService.createOperator(ChangeAttributeRole.class);
            roleSetter = OperatorService.createOperator(ChangeAttributeRole.class);
            RulePerformanceEvaluator trainValidationEvaluator2 = RapidMiner5.createOperator(RulePerformanceEvaluator.class);
            modelWriter = RapidMiner5.createOperator(ModelWriter.class);
            ruleGenerator = RapidMiner5.createOperator(ExpertRuleGenerator.class);

            // configure train process
            process = new com.rapidminer.Process();
            process.getRootOperator().getSubprocess(0).addOperator(arff);
            process.getRootOperator().getSubprocess(0).addOperator(arff2);
            process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
            process.getRootOperator().getSubprocess(0).addOperator(roleSetter2);
            process.getRootOperator().getSubprocess(0).addOperator(trainValidationEvaluator2);
            process.getRootOperator().getSubprocess(0).addOperator(ruleGenerator);
            process.getRootOperator().getSubprocess(0).addOperator(trainApplier);
            process.getRootOperator().getSubprocess(0).addOperator(modelWriter);

            process.getRootOperator().setParameter(ProcessRootOperator.PARAMETER_LOGVERBOSITY, "" + LogService.OFF);

            arff.getOutputPorts().getPortByName("output").connectTo(roleSetter.getInputPorts().getPortByName("example set input"));
            roleSetter.getOutputPorts().getPortByName("example set output").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
            ruleGenerator.getOutputPorts().getPortByName("model").connectTo(modelWriter.getInputPorts().getPortByName("input"));
            arff2.getOutputPorts().getPortByName("output").connectTo(roleSetter2.getInputPorts().getPortByName("example set input"));
            roleSetter2.getOutputPorts().getPortByName("example set output").connectTo(trainApplier.getInputPorts().getPortByName("unlabelled data"));
            modelWriter.getOutputPorts().getPortByName("through").connectTo(trainApplier.getInputPorts().getPortByName("model"));
            trainApplier.getOutputPorts().getPortByName("labelled data").connectTo(trainValidationEvaluator2.getInputPorts().getPortByName("labelled data"));
            trainValidationEvaluator2.getOutputPorts().getPortByName("performance").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
            trainApplier.getOutputPorts().getPortByName("model").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));

            // configure role setter
            roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelAttribute);
            roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
            roleSetter2.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelAttribute);
            roleSetter2.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);

            modelWriter.setParameter(ModelWriter.PARAMETER_OUTPUT_TYPE, "2");
        }

    }

    private class TestProcessWrapper {
        public ArffExampleSource arff;
        public ModelLoader modelLoader = null;
        public ChangeAttributeRole roleSetter = null;
        public ArffExampleSetWriter writeArff = null;

        public com.rapidminer.Process process;

        public TestProcessWrapper() throws OperatorCreationException {

            arff = RapidMiner5.createOperator(ArffExampleSource.class);
            roleSetter = OperatorService.createOperator(ChangeAttributeRole.class);
            ModelApplier applier = OperatorService.createOperator(ModelApplier.class);
            modelLoader = RapidMiner5.createOperator(ModelLoader.class);
            writeArff = RapidMiner5.createOperator(ArffExampleSetWriter.class);
            AbstractPerformanceEvaluator validationEvaluator = RapidMiner5.createOperator(RulePerformanceEvaluator.class);

            process = new com.rapidminer.Process();
            process.getRootOperator().getSubprocess(0).addOperator(arff);
            process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
            process.getRootOperator().getSubprocess(0).addOperator(applier);
            process.getRootOperator().getSubprocess(0).addOperator(validationEvaluator);
            process.getRootOperator().getSubprocess(0).addOperator(writeArff);
            process.getRootOperator().getSubprocess(0).addOperator(modelLoader);

            process.getRootOperator().setParameter(ProcessRootOperator.PARAMETER_LOGVERBOSITY, "" + LogService.OFF);


            arff.getOutputPorts().getPortByName("output").connectTo(roleSetter.getInputPorts().getPortByName("example set input"));
            roleSetter.getOutputPorts().getPortByName("example set output").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
            modelLoader.getOutputPorts().getPortByName("output").connectTo(applier.getInputPorts().getPortByName("model"));
            applier.getOutputPorts().getPortByName("labelled data").connectTo(validationEvaluator.getInputPorts().getPortByName("labelled data"));
            validationEvaluator.getOutputPorts().getPortByName("performance").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
            validationEvaluator.getOutputPorts().getPortByName("example set").connectTo(writeArff.getInputPorts().getPortByName("input"));
            applier.getOutputPorts().getPortByName("model").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));
            writeArff.getOutputPorts().getPortByName("through").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(2));

            roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelAttribute);
            roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);

        }
    }

    // General parameters
    protected SynchronizedReport performanceTable;

    protected SynchronizedReport trainingReport;

    protected SynchronizedReport testingReport;

    private final String outDirPath;
    private final String labelAttribute;
    private final List<ExperimentalConsole.TrainElement> trainElements;
    private final List<ExperimentalConsole.PredictElement> predictElements;

    private Pair<String,Map<String,Object>> paramSet;
    private Map<String, String> options;
    
    private boolean isVerbose = false;
    
    public void setVerbose(boolean v) { isVerbose = v; }
    public boolean getVerbose() { return isVerbose; }
    
    public TrainTestValidationExperiment(SynchronizedReport trainingReport, SynchronizedReport testingReport, SynchronizedReport predictionPerformance,
                                         String labelAttribute, Map<String, String> options, Pair<String,Map<String, Object>> paramSet,
                                         String outDirPath, List<ExperimentalConsole.TrainElement> trainElements,
                                         List<ExperimentalConsole.PredictElement> predictElements){

        File f = new File(outDirPath);
        this.outDirPath = f.isAbsolute() ? outDirPath : (System.getProperty("user.dir") + "/" + outDirPath);
        this.labelAttribute = labelAttribute;
        this.trainElements = trainElements;
        this.predictElements = predictElements;
        this.options = options;
        this.paramSet = paramSet;

        this.performanceTable = predictionPerformance;
        this.testingReport = testingReport;
        this.trainingReport = trainingReport;
    }

    @Override
    public void run() {

        try {

            TrainProcessWrapper train =  new TrainProcessWrapper();
            TestProcessWrapper test = new TestProcessWrapper();

            // survival dataset - set proper role
            List<String[]> roles = new ArrayList<>();

            if (options.containsKey(SurvivalRule.SURVIVAL_TIME_ROLE)) {
                roles.add(new String[]{options.get(SurvivalRule.SURVIVAL_TIME_ROLE), SurvivalRule.SURVIVAL_TIME_ROLE});
            }

            if (options.containsKey(Attributes.WEIGHT_NAME)) {
                roles.add(new String[]{options.get(Attributes.WEIGHT_NAME), Attributes.WEIGHT_NAME});
            }

            if (roles.size() > 0) {
                train.roleSetter.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
                train.roleSetter2.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
                test.roleSetter.setListParameter(ChangeAttributeRole.PARAMETER_CHANGE_ATTRIBUTES, roles);
            }

            // set rule generator parameters
        	Logger.log("\nPARAMETER SET: " + paramSet.fst + "\n", Level.INFO);
            Map<String, Object> params = paramSet.snd;

            if(params.containsKey(RULES_SIGNIFICANT_FIGURES)) {
                int numberOfRulesSignificantFigures = (int) params.get(RULES_SIGNIFICANT_FIGURES);
                params.remove(RULES_SIGNIFICANT_FIGURES);
                DoubleFormatter.configure(numberOfRulesSignificantFigures);
            } else {
                DoubleFormatter.defaultConfigure();
            }

            for (String key: params.keySet()) {
                Object o = params.get(key);

                boolean paramOk = train.ruleGenerator.getParameters().getKeys().contains(key);
                
                if (paramOk)   
	                if (o instanceof String) {
	                    train.ruleGenerator.setParameter(key, (String)o);
	                } else if (o instanceof List) {
	                    train.ruleGenerator.setListParameter(key, (List<String[]>)o);
	                } else {
                    throw new InvalidParameterException("Invalid paramter type: " + key);
                } else {
                	throw new UndefinedParameterError(key, "Undefined parameter: " + key);
                }
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

            // Train process
            Logger.log("TRAINING\n"
            		+ "Log file: " + trainingReport.getFile() + "\n",  Level.INFO);
            
            for(ExperimentalConsole.TrainElement te : trainElements){
            	Logger.log("Building model " + te.modelFile + " from dataset " + te.inFile + "\n", Level.INFO);
                File f = new File(te.modelFile);
                String modelFilePath = f.isAbsolute() ? te.modelFile : (outDirPath + "/" + te.modelFile);
                f = new File(te.inFile);
                String inFilePath = f.isAbsolute() ? te.inFile : (System.getProperty("user.dir") + "/" + te.inFile);

                f = new File(inFilePath);
                String trainFileName = f.getName();

                Logger.log("Train params: \n   Model file path: " + modelFilePath + "\n" +
                        "   Input file path: " + inFilePath + "\n", Level.FINE);

                train.modelWriter.setParameter(ModelWriter.PARAMETER_MODEL_FILE, modelFilePath);
                train.arff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, inFilePath);
                train.arff2.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, inFilePath);
                IOContainer out = train.process.run();
                IOObject[] objs = out.getIOObjects();

                PerformanceVector performance;

                if (te.modelCsvFile != null) {
                    RuleSetBase model = (RuleSetBase)objs[1];
                    f = new File(te.modelCsvFile);
                    String csvFilePath = f.isAbsolute() ? te.modelCsvFile : (outDirPath + "/" + te.modelCsvFile);

                    BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
                    writer.write(model.toTable());
                    writer.close();
                }

                // training report
                if (trainingReport != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringUtils.repeat("=", 80));
                    sb.append("\n");
                    sb.append(trainFileName);
                    sb.append("\n\n");
                    Model model = (Model)objs[1];
                    sb.append(model.toString());

                    sb.append("\nModel characteristics:\n");
                    	
                    RuleSetBase ruleModel = (RuleSetBase)model;
                    performance = RuleGenerator.recalculatePerformance(ruleModel);
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
                    trainingReport.append(sb.toString());
                    Logger.log(" [OK]\n", Level.INFO);
                }
            }

            // Test process
            Logger.log("PREDICTION\n"
            		+ "Performance file: " + performanceTable.getFile() + "\n", Level.INFO);
            for(ExperimentalConsole.PredictElement pe : predictElements) {
            	Logger.log("Applying model " + pe.modelFile + " on " + pe.testFile + ", saving predictions in " +  pe.testFile, Level.INFO);
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

                test.modelLoader.setParameter(ModelLoader.PARAMETER_MODEL_FILE, modelFilePath);
                test.arff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, testFilePath);
                test.writeArff.setParameter(ArffExampleSetWriter.PARAMETER_EXAMPLE_SET_FILE, predictionsFilePath);

                long t1 = System.nanoTime();
                IOContainer out = test.process.run();
                IOObject[] objs = out.getIOObjects();
                long t2 = System.nanoTime();
                double elapsedSec = (double)(t2 - t1) / 1e9;

                // Testing report
                if (testingReport != null) {
                    ExampleSet predictions = (ExampleSet)objs[2];
                    if (predictions.getAnnotations().containsKey(RuleSetBase.ANNOTATION_TEST_REPORT)) {
                        testingReport.append("================================================================================\n");
                        testingReport.append(testFileName + "\n");
                        testingReport.append(predictions.getAnnotations().get(RuleSetBase.ANNOTATION_TEST_REPORT));
                        testingReport.append("\n\n");
                    }
                }

                // Performance log
                if(performanceTable != null){

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
                    
                    String configString = "Parameters: " + model.getParams().toString().replace("\n", "; ");
                    performanceTable.add(new String[] { configString, performanceHeader.toString()}, row.toString());
                }
                
                Logger.log(" [OK]\n", Level.INFO);
            }
        }  catch (Exception e) {
           if (isVerbose) {
        	   e.printStackTrace();
           } else {
        	   Logger.log(e.getMessage() + "\n", Level.SEVERE);
           }
        	
        }
    }
}
