package adaa.analytics.rules.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import adaa.analytics.rules.logic.Logger;
import adaa.analytics.rules.logic.SurvivalRule;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.SurvivalPerformanceEvaluator;

import com.rapidminer.example.Attributes;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.MultiClassificationPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.performance.PolynominalClassificationPerformanceEvaluator;
import com.rapidminer.operator.performance.RegressionPerformanceEvaluator;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.OperatorService;

public class SplittedXValidationExperiment extends ExperimentBase {

	protected File arffDir;
		
	protected String modelFile;
	
	protected ArffExampleSource trainArff;
	
	protected ArffExampleSource testArff;
	
	public SplittedXValidationExperiment(
		File arffDir,
		Report report,
		String labelAttribute,
		Type experimentType,
		Map<String, Object> params,
		String modelFile) {
		
		super(report, experimentType, params); 
		
		try {
			this.arffDir = arffDir;
			this.modelFile = modelFile;
			this.paramsSets = new ArrayList<Map<String, Object>>();
			paramsSets.add(params);
			
			trainArff = (ArffExampleSource)OperatorService.createOperator(ArffExampleSource.class);
	    	testArff = (ArffExampleSource)OperatorService.createOperator(ArffExampleSource.class);
	    	ChangeAttributeRole trainRoleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
	    	ChangeAttributeRole testRoleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
	    	ModelApplier applier = (ModelApplier)OperatorService.createOperator(ModelApplier.class);


	    	// configure main process
	    	process = new com.rapidminer.Process();
	    	process.getRootOperator().getSubprocess(0).addOperator(trainArff);
	    	process.getRootOperator().getSubprocess(0).addOperator(testArff);
	    	process.getRootOperator().getSubprocess(0).addOperator(trainRoleSetter);
	    	process.getRootOperator().getSubprocess(0).addOperator(testRoleSetter);
	    	process.getRootOperator().getSubprocess(0).addOperator(ruleGenerator);
	    	process.getRootOperator().getSubprocess(0).addOperator(applier);
	    	process.getRootOperator().getSubprocess(0).addOperator(validationEvaluator);
	    	
	    	trainArff.getOutputPorts().getPortByName("output").connectTo(trainRoleSetter.getInputPorts().getPortByName("example set input"));	
	    	trainRoleSetter.getOutputPorts().getPortByName("example set output").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
	    	
	    	testArff.getOutputPorts().getPortByName("output").connectTo(testRoleSetter.getInputPorts().getPortByName("example set input"));	
	    	testRoleSetter.getOutputPorts().getPortByName("example set output").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
	        	
	    	ruleGenerator.getOutputPorts().getPortByName("model").connectTo(applier.getInputPorts().getPortByName("model"));
		 
	    	applier.getOutputPorts().getPortByName("labelled data").connectTo(
	    			validationEvaluator.getInputPorts().getPortByName("labelled data"));
	   	
	    	// pass estimated performance to 
	    	ruleGenerator.getOutputPorts().getPortByName("estimated performance").connectTo(
	    			validationEvaluator.getInputPorts().getPortByName("performance"));
	    	
	    	validationEvaluator.getOutputPorts().getPortByName("performance").connectTo(
	    			process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
	    	applier.getOutputPorts().getPortByName("model").connectTo(
	    			process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));
	    	
	    
	    	// configure role setter
	    	trainRoleSetter.setParameter(trainRoleSetter.PARAMETER_NAME, labelAttribute);
	    	trainRoleSetter.setParameter(trainRoleSetter.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
	    	
	    	testRoleSetter.setParameter(testRoleSetter.PARAMETER_NAME, labelAttribute);
	    	testRoleSetter.setParameter(testRoleSetter.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
	    	
	    	// this is only set for survival experiment
	    	if (experimentType == Type.SURVIVAL_BY_REGRESSION) {
	    		List<String[]> roles = new ArrayList<String[]>();
	    		roles.add(new String[]{"survival_time", SurvivalRule.SURVIVAL_TIME_ROLE});
	    		trainRoleSetter.setListParameter(trainRoleSetter.PARAMETER_CHANGE_ATTRIBUTES, roles);
	    		testRoleSetter.setListParameter(testRoleSetter.PARAMETER_CHANGE_ATTRIBUTES, roles);
	    	}
	    	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    	
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
			
			Logger.log("Processing: " + arffDir.getName() + "\n", Level.FINE);
		
			File[] filesListing = arffDir.listFiles();
			if (filesListing == null) {
	    		throw new IOException();
	    	}
			
			for (File child : filesListing) {
				if (!child.isFile() | !child.getName().contains("train")) {
					continue;
				} 
			
				String trainFile = child.getName();
				String testFile = trainFile.replace("train", "test");
					
				File f = new File(arffDir.getAbsolutePath() + "/" + testFile);
				if (!f.exists()) {
					Logger.log("TRAIN: " + trainFile + ", TEST: " + testFile + " NOT FOUND!\n" , Level.FINE);
					continue;
				}
	
				Logger.log("TRAIN: " + trainFile + ", TEST: " + testFile + "\n" , Level.FINE);
				
				Date begin = new Date();
		    	String dateString = dateFormat.format(begin);

				
				Logger.log("started!\n", Level.FINE);
				trainArff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, arffDir.getAbsolutePath() + "/" + trainFile);
				testArff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, arffDir.getAbsolutePath() + "/" + testFile);
		    	
				long t1 = System.nanoTime();
				IOContainer out = process.run();
				IOObject[] objs = out.getIOObjects();
				long t2 = System.nanoTime();
		    	double elapsedSec = (double)(t2 - t1) / 1e9;
		    	
		    	if (modelFile.length() > 0) {
		    		FileWriter fw = new FileWriter(modelFile);
					BufferedWriter bw = new BufferedWriter(fw);
		    		Model model = (Model)objs[1];
		    		bw.write(model.toString());
		    		bw.close();
		    	}
		    	
		    	PerformanceVector performance = (PerformanceVector)objs[0];	
		    	String[] columns = performance.getCriteriaNames();
		    	
		    	Logger.log(performance + "\n", Level.FINE);
		    	
		    	// generate headers
	    		String performanceHeader = "Dataset, time started, elapsed[s], ";
	    		String row = testFile + "," + dateString + "," + elapsedSec + ",";
	    		
	    		for (String name : columns) {
	    			performanceHeader +=  name + ",";
	    		}
	
		    	for (String name : performance.getCriteriaNames()) {
		    		double avg = performance.getCriterion(name).getAverage();
		    		row +=  avg + ", ";
		    	}
		
				report.add(new String[] {ruleGenerator.toString(), performanceHeader}, row);	
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
