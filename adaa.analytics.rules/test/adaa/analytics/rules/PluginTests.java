/*package adaa.analytics.rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import adaa.analytics.rules.consoles.app.ExperimentalConsole;
import adaa.analytics.rules.experiments.ExperimentBase;
import adaa.analytics.rules.experiments.InternalXValidationExperiment;
import adaa.analytics.rules.experiments.Report;
import adaa.analytics.rules.logic.Logger;
import adaa.analytics.rules.logic.SurvivalRule;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.SurvivalPerformanceEvaluator;
import adaa.analytics.rules.quality.ClassificationMeasure;
import adaa.analytics.rules.stream.RuleGeneratorFromStream;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.OperatorService;

import disesor.rapidminer.development.RapidMinerEnvironment;
import disesor.rapidminer.development.SwingUtils;

public class PluginTests {
    
	private Integer batchSize = 0;
	
    @Test
    public void runPlugin() throws Exception {
    	Logger.getInstance().addStream(System.out, Level.FINER);

    	RapidMinerEnvironment env = new RapidMinerEnvironment.Builder(false)
        		.initCoreOperators(true)
                .loadExternalPlugins(false)
                .showSplash(false)
                .initLookAndFeel(true)
                .build();
       
        env.runPlugin();
       
        SwingUtils.waitForEventDispatchThread();
    }
    
    @Test
    public void runStreamableDataClassification() throws OperatorCreationException, OperatorException, UnsupportedEncodingException, FileNotFoundException, InterruptedException {
    
    	try{
    		RapidMiner.init();
    	
    	} catch (Exception ex)
    	{
    		
    		Assert.fail();
    	}
    	Logger.getInstance().addStream(System.out, Level.FINE);
    	
    	int nFolds = 2;
    	
    	String testDirectory = "C:/test_sets";
    	String logPrefix = "C:/test_result/classification_";
    	
    	Object [][] parameterSets = {
    			// minimum rule coverage, pruning enabled, induction quality measure, pruning quality(optional)
    		//	{5, 0.0, false, ClassificationMeasure.Correlation},
    			{5, 0.0, true, ClassificationMeasure.Correlation},
    		//	{5, 0.0, false, ClassificationMeasure.C2},
    		//	{5, 0.0, true, ClassificationMeasure.C2},
    		//	{5, 0.0, false, ClassificationMeasure.RSS},
    		//	{5, 0.0, true, ClassificationMeasure.RSS},
    		//	{5, 0.0, false, ClassificationMeasure.Lift},
    		//	{5, 0.0, true, ClassificationMeasure.Lift},
    		//	{5, 0.0, false, ClassificationMeasure.SBayesian},
    		//	{5, 0.0, true, ClassificationMeasure.SBayesian},
    		//	{5, 0.0, true, ClassificationMeasure.C2, ClassificationMeasure.RSS},
    	};
    	batchSize = 1000;
    	
    	crossValidate("class", nFolds, ExperimentBase.Type.CLASSIFICATION, testDirectory, logPrefix, parameterSets, 4);
    }
    
    @Test 
    public void runClassification() throws OperatorCreationException, OperatorException, UnsupportedEncodingException, FileNotFoundException, InterruptedException {
    	
    	RapidMiner.init();
    	Logger.getInstance().addStream(System.out, Level.FINE);
    	//Logger.getInstance().addStream(System.out, Level.WARNING);
    	
    	int folds = 5;
    	String testDirectory = "E:/Disesor/Workspace/tst/arff/classification";
    	String logPrefix = "D:/classification_cv_";
    	
    	Object [][] parameterSets = {
    			// minimum rule coverage, pruning enabled, induction quality measure, pruning quality(optional)
    		//	{5, 0.0, false, ClassificationMeasure.Correlation},
    		//	{5, 0.0, true, ClassificationMeasure.Correlation},
    		//	{5, 0.0, false, ClassificationMeasure.C2},
    	
    	//		{5, 0.0, true, 0, ClassificationMeasure.C2},
    	//		{5, 0.0, true, 1, ClassificationMeasure.C2},
    	//		{5, 0.0, true, 3, ClassificationMeasure.C2},
    	//		{5, 0.0, true, 5, ClassificationMeasure.C2},
    		//	{5, 0.0, false, ClassificationMeasure.RSS},
    		//	{5, 0.0, true, ClassificationMeasure.RSS},
    	//		{5, 0.0, false, 0, ClassificationMeasure.Lift},
    	//		{5, 0.0, false, 1, ClassificationMeasure.Lift},
    	//		{5, 0.0, false, 3, ClassificationMeasure.Lift},
    	//		{5, 0.0, false, 5, ClassificationMeasure.Lift},
    		//	{5, 0.0, true, ClassificationMeasure.Lift},
    		//	{5, 0.0, false, ClassificationMeasure.SBayesian},
    		//	{5, 0.0, true, ClassificationMeasure.SBayesian},
    			
    			{5, 0.0, true, 0, ClassificationMeasure.BinaryEntropy, ClassificationMeasure.Correlation},
 
    	};

    	int cores = Runtime.getRuntime().availableProcessors();
    	crossValidate("class", folds, ExperimentBase.Type.CLASSIFICATION, testDirectory, logPrefix, parameterSets, 4);
    }
    
    
    @Test 
    public void runRegression() throws OperatorCreationException, OperatorException, UnsupportedEncodingException, FileNotFoundException, InterruptedException {
    	
    	RapidMiner.init();
    	Logger.getInstance().addStream(System.out, Level.FINER);
    	
    	int folds = 5;
    	String testDirectory = "E:/Disesor/Workspace/tst/arff/regression";
    	String logPrefix = "D:/regression_cv_";
    	
    	Object [][] parameterSets = {
    			// minimum rule coverage, pruning enabled, induction quality measure, pruning quality(optional)
    	//		{5, 0.1, false, ClassificationMeasure.Correlation},
    			{5, 0.1, true, ClassificationMeasure.Correlation},
    	};

    	int cores = Runtime.getRuntime().availableProcessors();
    	crossValidate("class", folds, ExperimentBase.Type.REGRESSION, testDirectory, logPrefix, parameterSets, 1);
    }
    
    @Test
    public void runSurvival() throws Exception {
    	RapidMiner.init();
    	Logger.getInstance().addStream(System.out, Level.FINE);
    	
    	int folds = 5;
    	String testDirectory = "E:/Disesor/Workspace/tst/miary-kombinacje/przezycie/cases/";
    	String logPrefix = "D:/survival_cv_";
    	
    	Object [][] parameterSets = {
    			// minimum rule coverage, pruning enabled
    			{5, 0.1, true, ClassificationMeasure.SBayesian},
    	};

    	int cores = Runtime.getRuntime().availableProcessors();
    	crossValidate("survival_status", folds, ExperimentBase.Type.SURVIVAL_BY_REGRESSION, testDirectory, logPrefix, parameterSets, 1);
    }
    
    protected void crossValidate(String labelAttribute, int folds, ExperimentBase.Type experimentType, 
    		String testDirectory, String logPrefix, Object[][] paramsArray, int threadCount) 
    		throws OperatorCreationException, OperatorException, UnsupportedEncodingException, FileNotFoundException, InterruptedException { 
    	
    	List<Map<String, Object>> paramSets = new ArrayList<Map<String,Object>>();
    	
    	DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
    	
		Date begin = new Date();
    	String dateString = dateFormat.format(begin);
    	String logFile = logPrefix + "_t" + threadCount + "_" + dateString + ".csv";	
    	
    	Report report = new Report(logFile);
    	
    	try {
	    	for (Object[] params : paramsArray) {
	    		Map<String, Object> set = new HashMap<String, Object>();
	    		set.put(RuleGenerator.PARAMETER_MIN_RULE_COVERED, params[0].toString());
	    		set.put(RuleGenerator.PARAMETER_MAX_UNCOVERED_FRACTION, params[1].toString());
	    		set.put(RuleGenerator.PARAMETER_PRUNING_ENABLED, params[2].toString());
	    		set.put(RuleGenerator.PARAMETER_MAX_GROWING, params[3].toString());
	    		set.put(RuleGenerator.PARAMETER_INDUCTION_MEASURE, ClassificationMeasure.getName((int)params[4]));
	    		int pruningMeasure = (params.length == 6) ? (int)params[5] : (int)params[4];	
	    		
	    		set.put(RuleGenerator.PARAMETER_PRUNING_MEASURE, ClassificationMeasure.getName(pruningMeasure));	
	    		if (batchSize != 0)
	    			set.put(RuleGeneratorFromStream.PARAMETER_BATCH_SIZE, batchSize.toString());
	    		paramSets.add(set);
	    	}
	    	
	    	System.out.println("Performing " + folds + "-fold cross validation...");
	    	File dir = new File(testDirectory);
	    	File[] directoryListing = dir.listFiles();
	    	
	    	if (directoryListing == null) {
	    		throw new IOException();
	    	}
	    	
			ExecutorService pool = Executors.newFixedThreadPool(threadCount);
			List<Future> futures = new ArrayList<Future>();
			for (File child : directoryListing) {
				if (!child.isFile()) {
					continue;
				}
				
				InternalXValidationExperiment exp = new InternalXValidationExperiment(
						child, 
						report,
						labelAttribute, 
						folds, 
						experimentType, 
						paramSets,
						null);
			//	Future f = pool.submit(exp);
			//	futures.add(f);
				exp.run();
		    }	
			
			for (Future f : futures) {
				f.get();
			}
		
    	} catch (ExecutionException | IOException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void runSurvivalSplitted() throws Exception {
      	String testDir = "E:/Disesor/Workspace/prv/dat/survival/10x10cv";
    	String reportDirPrefix =  "E:/Disesor/Workspace/survival_rules/res/10x10cv";
    	
    	float[] minCovs = {1, 2, 3};
    	
    	ExperimentalConsole console = new ExperimentalConsole();
    	console.testSurvivalSplitted(testDir, reportDirPrefix, minCovs);
   
    }

    @Test
    public void runSurvivalTraining() throws Exception {
    	RapidMiner.init();
    	Logger.getInstance().addStream(System.out, Level.FINER);
    	
     	DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
        int threads = 1;
    	Date begin = new Date();
    	String dateString = dateFormat.format(begin);
    	String reportFile = "D:/survival_report_t" + threads + "_" + dateString + ".csv";	
    	Report report = new Report(reportFile);
    	String logFile = "D:/survival_log_t" + threads + "_" + dateString + ".log";	
    	Logger.getInstance().addStream(new PrintStream(new FileOutputStream(logFile, true)), Level.FINE);

    	String testDirectory = "E:/Disesor/Workspace/tst/miary-kombinacje/przezycie/cases/";
    	
    	ArffExampleSource arffSource = (ArffExampleSource)OperatorService.createOperator(ArffExampleSource.class);
    	ChangeAttributeRole roleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
    	RuleGenerator ruleGenerator = new RuleGenerator(new OperatorDescription("", "", null, null, "", null));
    	ModelApplier applier = (ModelApplier)OperatorService.createOperator(ModelApplier.class);
    	SurvivalPerformanceEvaluator evaluator = new SurvivalPerformanceEvaluator(new OperatorDescription("", "", null, null, "", null));
    	
    	// configure main process
    	com.rapidminer.Process process = new com.rapidminer.Process();
    	process.getRootOperator().getSubprocess(0).addOperator(arffSource);
    	process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
    	process.getRootOperator().getSubprocess(0).addOperator(ruleGenerator);
    	process.getRootOperator().getSubprocess(0).addOperator(applier);
    	process.getRootOperator().getSubprocess(0).addOperator(evaluator);
    	
    	arffSource.getOutputPorts().getPortByName("output").connectTo(roleSetter.getInputPorts().getPortByName("example set input"));	
    	roleSetter.getOutputPorts().getPortByName("example set output").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
    	
    	ruleGenerator.getOutputPorts().getPortByName("model").connectTo(applier.getInputPorts().getPortByName("model"));
    	ruleGenerator.getOutputPorts().getPortByName("exampleSet").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
    	 
    	applier.getOutputPorts().getPortByName("labelled data").connectTo(
    			evaluator.getInputPorts().getPortByName("labelled data"));
   	
    	// pass estimated performance to 
    	ruleGenerator.getOutputPorts().getPortByName("estimated performance").connectTo(
    			evaluator.getInputPorts().getPortByName("performance"));
    	
    	evaluator.getOutputPorts().getPortByName("performance").connectTo(
    			process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
    
    	// configure role setter
    	roleSetter.setParameter(roleSetter.PARAMETER_NAME, "survival_status");
    	roleSetter.setParameter(roleSetter.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
    	List<String[]> roles = new ArrayList<String[]>();
    	roles.add(new String[]{"survival_time", SurvivalRule.SURVIVAL_TIME_ROLE});
    	roleSetter.setListParameter(roleSetter.PARAMETER_CHANGE_ATTRIBUTES, roles);
    	
    	ruleGenerator.setParameter(ruleGenerator.PARAMETER_LOGRANK_SURVIVAL, "true");
    	ruleGenerator.setParameter(ruleGenerator.PARAMETER_MIN_RULE_COVERED, "" + 4);
    	
    	File dir = new File(testDirectory);
    	File[] directoryListing = dir.listFiles();
    	
    	if (directoryListing == null) {
    		throw new IOException();
    	}
    	
		for (File child : directoryListing) {
			if (!child.isFile()) {
				continue;
			}
			Logger.log("Processing: " + child.getName() + "\n", Level.FINE);
			
			arffSource.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, child.getAbsolutePath());
	    	IOContainer out = process.run();
	    	
	    	PerformanceVector performance = out.get(PerformanceVector.class, 0);	
	    	String[] columns = performance.getCriteriaNames();
	    	
	    	Logger.log(performance + "\n", Level.FINE);
	    	

	    	// generate headers
    		String performanceHeader = "Dataset, ";
    		String row = child.getName() + ",";
    		
    		for (String name : columns) {
    			performanceHeader += "avg (" +  name + "), std(" + name + "),";
    		}

	    	for (String name : performance.getCriteriaNames()) {
	    		double avg = performance.getCriterion(name).getAverage();
	    		double std = Math.sqrt(performance.getCriterion(name).getVariance());
	    		row +=  avg + ", " + std + ", ";
	    	}
	
			report.add(new String[] {"", performanceHeader}, row);	
		}
    }
}
*/
