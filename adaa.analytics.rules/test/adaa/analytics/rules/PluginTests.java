package adaa.analytics.rules;

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

import adaa.analytics.rules.consoles.ExperimentalConsole;
import adaa.analytics.rules.experiments.ExperimentBase;
import adaa.analytics.rules.experiments.InternalXValidationExperiment;
import adaa.analytics.rules.experiments.SynchronizedReport;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.operator.RulePerformanceEvaluator;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
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


public class PluginTests {
    
	private Integer batchSize = 0;
	
    @Test
    public void runPlugin() throws Exception {
    	Logger.getInstance().addStream(System.out, Level.FINER);

    /*	RapidMinerEnvironment env = new RapidMinerEnvironment.Builder(false)
        		.initCoreOperators(true)
                .loadExternalPlugins(false)
                .showSplash(false)
                .initLookAndFeel(true)
                .build();
       
        env.runPlugin();
      */ 
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
    
    protected void crossValidate(String labelAttribute, int folds, ExperimentBase.Type experimentType, 
    		String testDirectory, String logPrefix, Object[][] paramsArray, int threadCount) 
    		throws OperatorCreationException, OperatorException, UnsupportedEncodingException, FileNotFoundException, InterruptedException { 
    	
    	List<Map<String, Object>> paramSets = new ArrayList<Map<String,Object>>();
    	
    	DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
    	
		Date begin = new Date();
    	String dateString = dateFormat.format(begin);
    	String logFile = logPrefix + "_t" + threadCount + "_" + dateString + ".csv";	
    	
    	SynchronizedReport report = new SynchronizedReport(logFile);
    	
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
						null,
						labelAttribute, 
						folds, 
						experimentType, 
						paramSets);
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
    
}

