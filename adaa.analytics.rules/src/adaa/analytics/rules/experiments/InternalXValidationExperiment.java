package adaa.analytics.rules.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.PerformanceEvaluator;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.performance.PolynominalClassificationPerformanceEvaluator;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.operator.preprocessing.filter.NumericToBinominal;
import com.rapidminer.operator.validation.XValidation;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.RandomGenerator;

public class InternalXValidationExperiment extends ExperimentBase {

	protected String modelFile; 
	
	protected File arffFile;

	public InternalXValidationExperiment(
			File arffFile, 
			Report report,
			String labelParameter,
			int foldCount,
			Type experimentType,
			Map<String,Object> params,
			String modelFile) {
		this(arffFile, report, labelParameter, foldCount, experimentType, (List<Map<String,Object>>)null, modelFile);
		
		this.paramsSets = new ArrayList<Map<String, Object>>();
		this.modelFile = modelFile;
		paramsSets.add(params);
	}
	
	
	public InternalXValidationExperiment(
			File arffFile, 
			Report report,
			String labelParameter,
			int foldCount,
			Type experimentType,
			List<Map<String,Object>> paramsSets,
			String modelFile) {
		
		super(report, experimentType, paramsSets);
		
		try {
			this.arffFile = arffFile;
			this.modelFile = modelFile;

			ArffExampleSource arffSource = (ArffExampleSource)OperatorService.createOperator(ArffExampleSource.class);
	    	ChangeAttributeRole roleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
	    	XValidation validation = (XValidation)OperatorService.createOperator(XValidation.class);
	    	validation.setParameter(XValidation.PARAMETER_NUMBER_OF_VALIDATIONS, Integer.toString(foldCount));
	    	validation.setParameter(XValidation.PARAMETER_CREATE_COMPLETE_MODEL, Boolean.toString(true));
	    	
	    	ModelApplier globalApplier = (ModelApplier)OperatorService.createOperator(ModelApplier.class);
	
	    	// configure main process
	    	process.getRootOperator().getSubprocess(0).addOperator(arffSource);
	    	process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
	    	process.getRootOperator().getSubprocess(0).addOperator(validation);
	    	process.getRootOperator().getSubprocess(0).addOperator(globalApplier);
	    	process.getRootOperator().getSubprocess(0).addOperator(globalEvaluator);
	    	
	    	arffSource.getOutputPorts().getPortByName("output").connectTo(roleSetter.getInputPorts().getPortByName("example set input"));	
	    	
	    	// configure role setter
	    	roleSetter.setParameter(roleSetter.PARAMETER_NAME, labelParameter);
	    	roleSetter.setParameter(roleSetter.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
	    	
	    	if (experimentType == Type.SURVIVAL_BY_CLASSIFICATION || experimentType == Type.SURVIVAL_BY_REGRESSION) {
	    		List<String[]> roles = new ArrayList<String[]>();
	        	roles.add(new String[]{"survival_time", SurvivalRule.SURVIVAL_TIME_ROLE});
	        	roleSetter.setListParameter(roleSetter.PARAMETER_CHANGE_ATTRIBUTES, roles);
	    	} 
	    	
	    	roleSetter.getOutputPorts().getPortByName("example set output").connectTo(validation.getInputPorts().getPortByName("training"));
	    		
	    	// use stratified CV in all cases beside regression
	    	validation.setParameter(XValidation.PARAMETER_SAMPLING_TYPE,  
		    	experimentType == Type.REGRESSION  || experimentType == Type.SURVIVAL_BY_REGRESSION
		    		? SplittedExampleSet.SHUFFLED_SAMPLING + ""  
		    		: SplittedExampleSet.STRATIFIED_SAMPLING + "");
	    	
	    	validation.setParameter(RandomGenerator.PARAMETER_USE_LOCAL_RANDOM_SEED, "true");
	    	validation.setParameter(RandomGenerator.PARAMETER_LOCAL_RANDOM_SEED, "1");
		    
	    	// configure training subprocess
	      	ExecutionUnit trainer = validation.getSubprocess(0);
	      	trainer.addOperator(ruleGenerator);
	      	trainer.getInnerSources().getPortByName("training").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
	    	ruleGenerator.getOutputPorts().getPortByName("model").connectTo(trainer.getInnerSinks().getPortByName("model"));
	    	ruleGenerator.getOutputPorts().getPortByName("estimated performance").connectTo(trainer.getInnerSinks().getPortByName("through 1"));
	    
	    	// configure testing subprocess
	    	ExecutionUnit tester = validation.getSubprocess(1);
	    	
	    	ModelApplier applier = (ModelApplier)OperatorService.createOperator(ModelApplier.class);
	    	
	    	if (experimentType == Type.SURVIVAL_BY_REGRESSION) {
	    		ruleGenerator.setParameter(ruleGenerator.PARAMETER_LOGRANK_SURVIVAL, "" + true);
	    	}
	    
	    	tester.addOperator(applier);
	    	tester.addOperator(validationEvaluator);
	    	
	    	tester.getInnerSources().getPortByName("model").connectTo(applier.getInputPorts().getPortByName("model"));
	    	tester.getInnerSources().getPortByName("test set").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
	    	applier.getOutputPorts().getPortByName("labelled data").connectTo(validationEvaluator.getInputPorts().getPortByName("labelled data"));
	    	validationEvaluator.getOutputPorts().getPortByName("performance").connectTo(tester.getInnerSinks().getPortByName("averagable 1"));
	    	
	    	tester.getInnerSources().getPortByName("through 1").connectTo(validationEvaluator.getInputPorts().getPortByName("performance"));
	    		
	    	// connect performance vector directly to process output
	    	validation.getOutputPorts().getPortByName("averagable 1").connectTo(
	    			process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
	    	
	    	
	    	// connect entire training set and corresponding model from validation to globa applier
	    	validation.getOutputPorts().getPortByName("training").connectTo(globalApplier.getInputPorts().getPortByName("unlabelled data"));
	    	validation.getOutputPorts().getPortByName("model").connectTo(globalApplier.getInputPorts().getPortByName("model"));
	    			
	    	// get outputs of model applier to 		
	    	globalApplier.getOutputPorts().getPortByName("model").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(1));
	    	
	    	globalApplier.getOutputPorts().getPortByName("labelled data").connectTo(globalEvaluator.getInputPorts().getPortByName("labelled data"));
	    	globalEvaluator.getOutputPorts().getPortByName("performance").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(2));
		    
	    	arffSource.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, arffFile.getAbsolutePath());
	 	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		try {
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
			
	    	Logger.log("Processing: " + arffFile.getName() + "\n", Level.FINE);
			
			Date begin = new Date();
	    	String dateString = dateFormat.format(begin);

			Logger.log("started!\n", Level.FINE);
			
			// generate headers
    		String paramsHeader = ", ,";
			String performanceHeader = "Dataset, time started, ";
    		String row = arffFile + "," + dateString + ",";
    		
			for (Map<String,Object> params : paramsSets) {
			
				// set parameters
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
				
				
				long t1 = System.nanoTime();
		    	IOContainer out;
				out = process.run();
				long t2 = System.nanoTime();
				
				if (modelFile.length() > 0) {
					IOObject[] objs = out.getIOObjects();
					FileWriter fw = new FileWriter(modelFile);
					BufferedWriter bw = new BufferedWriter(fw);
		    		Model model = (Model)objs[1];
		    		bw.write(model.toString());
		    		PerformanceVector performance = (PerformanceVector)objs[2];
		    	
		    		bw.write("\n");
			    	for (String name : performance.getCriteriaNames()) {
			    		double avg = performance.getCriterion(name).getAverage();
			    		bw.write(name + ": " + avg + "\n");	
			    	}
		    		
		    		bw.close();
		    	}
				
	
				double elapsedSec = (double)(t2 - t1) / 1e9;
		    	
		    	PerformanceVector performance = out.get(PerformanceVector.class, 0);	
		    	String[] columns = performance.getCriteriaNames();
		    	
		    	Logger.log(performance + "\n", Level.FINE);
		    	
		    	// generate headers
		    	paramsHeader += ruleGenerator.toString() + ",";
	    		performanceHeader += "elapsed[s], ";
	    		row += elapsedSec + ",";
	    		
	    		for (String name : columns) {
	    			paramsHeader += ", ";
	    			performanceHeader +=  name + ",";
	    		}
	
		    	for (String name : performance.getCriteriaNames()) {
		    		double avg = performance.getCriterion(name).getAverage();
		    		row +=  avg + ", ";
		    	}
			}
	
			report.add(new String[] {paramsHeader, performanceHeader}, row);	
			
		} catch (OperatorException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
