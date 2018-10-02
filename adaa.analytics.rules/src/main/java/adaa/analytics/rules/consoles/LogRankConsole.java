package adaa.analytics.rules.consoles;

import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.logic.representation.SurvivalRuleSet;
import adaa.analytics.rules.operator.RuleGenerator;
import adaa.analytics.rules.utils.RapidMiner5;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.*;
import com.rapidminer.operator.ports.PortException;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer5.operator.io.ArffExampleSource;
import com.rapidminer5.operator.io.ModelLoader;
import com.rapidminer5.operator.io.ModelWriter;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class LogRankConsole {
	public static void main (String[] args) {
		Logger.getInstance().addStream(System.out, Level.INFO);	
		Logger.log( StringUtils.repeat("*", 80) + "\n"
				+ "\tLR-Rules 1.0 (August 2016)\n"
				+ "\tLearning rule sets from survival data\n" 
				+ "\tLukasz Wrobel (lukasz.wrobel@ibemag.pl), Adam Gudys, Marek Sikora\n"
				+  StringUtils.repeat("*", 80) + "\n", Level.INFO);
		
		System.setProperty("rapidminer.home", ".");
		LogService.getGlobal().setVerbosityLevel(LogService.ERROR);
		RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);
		RapidMiner.init();

		LogRankConsole console = new LogRankConsole();
		
		try {
		
			if (args.length > 1) {
				String mode = args[0];
				if (mode.equals("-train") && args.length == 4) {
					// train a model
					console.trainModel(args[1], args[2], args[3]);			
				} else if (mode.equals("-apply") && args.length == 4) {
					// apply a model
					console.applyModel(args[1], args[2], args[3]);
				} else {
					console.printUsage();
				}
			} else {
				console.printUsage();
			} 
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	protected void printUsage() {
		Logger.log(	
			"USAGE:\n" +
			"Training the model:\n" +
			"java -jar lr-rules.jar -train trainSet binaryModel ruleSet\n" +
			"Applying the model:\n" +
			"java -jar lr-rules.jar -apply testSet binaryModel predictions\n", Level.INFO);
	}
	
	protected void trainModel(String trainFile, String binaryModel, String textModel) throws OperatorCreationException, OperatorException, PortException, UnsupportedEncodingException, FileNotFoundException, IOException {
		ArffExampleSource arffSource = RapidMiner5.createOperator(ArffExampleSource.class);
    	ChangeAttributeRole roleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
    	RuleGenerator ruleGenerator = RapidMiner5.createOperator(RuleGenerator.class);
    	ModelWriter modelWriter = RapidMiner5.createOperator(ModelWriter.class);
    	
    	// configure main process
    	com.rapidminer.Process process = new com.rapidminer.Process();
    	process.getRootOperator().getSubprocess(0).addOperator(arffSource);
    	process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
    	process.getRootOperator().getSubprocess(0).addOperator(ruleGenerator);
    	process.getRootOperator().getSubprocess(0).addOperator(modelWriter);
    	
    	arffSource.getOutputPorts().getPortByName("output").connectTo(roleSetter.getInputPorts().getPortByName("example set input"));	
    	roleSetter.getOutputPorts().getPortByName("example set output").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
    	ruleGenerator.getOutputPorts().getPortByName("model").connectTo(modelWriter.getInputPorts().getPortByName("input"));
    	modelWriter.getOutputPorts().getPortByName("through").connectTo(process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
    	
    	// convert to absolute filenames
    	trainFile = toAbsolutePath(trainFile);
    	textModel = toAbsolutePath(textModel);
    	binaryModel = toAbsolutePath(binaryModel);
    	
    	Logger.log("Train set file: " + trainFile + "\n", Level.INFO);
    	Logger.log("Binary model file: " + binaryModel + "\n", Level.INFO);	
    	Logger.log("Rule set file: " + textModel + "\n", Level.INFO);	
    	
    	arffSource.setParameter(arffSource.PARAMETER_DATA_FILE, trainFile);
    	
    	roleSetter.setParameter(roleSetter.PARAMETER_NAME, "survival_status");
    	roleSetter.setParameter(roleSetter.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
    	List<String[]> roles = new ArrayList<String[]>();
    	roles.add(new String[]{"survival_time", SurvivalRule.SURVIVAL_TIME_ROLE});
    	roleSetter.setListParameter(roleSetter.PARAMETER_CHANGE_ATTRIBUTES, roles);
    	
    	ruleGenerator.setParameter(ruleGenerator.PARAMETER_LOGRANK_SURVIVAL, "true");
    	modelWriter.setParameter(modelWriter.PARAMETER_MODEL_FILE, binaryModel);
    	
    	Logger.log("Building a model...\n", Level.INFO);	
    	IOContainer out = process.run();
    	Model model = out.get(Model.class, 0);
    	
    	File file = new File(textModel);
    	BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
    	buf.write(model.toString());
    	
    	Logger.log("Finished!\n", Level.INFO);
    	
	}
	
	protected void applyModel(String testFile, String binaryModel, String predictionsFile) 
			throws OperatorCreationException, OperatorException, IOException {
		ArffExampleSource arffSource = RapidMiner5.createOperator(ArffExampleSource.class);
    	ChangeAttributeRole roleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
    	ModelLoader loader = RapidMiner5.createOperator(ModelLoader.class);
    	ModelApplier applier = (ModelApplier)OperatorService.createOperator(ModelApplier.class);
    	
    	// configure main process
    	com.rapidminer.Process process = new com.rapidminer.Process();
    	process.getRootOperator().getSubprocess(0).addOperator(arffSource);
    	process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
    	process.getRootOperator().getSubprocess(0).addOperator(loader);
    	process.getRootOperator().getSubprocess(0).addOperator(applier);
    	
    	arffSource.getOutputPorts().getPortByName("output").connectTo(roleSetter.getInputPorts().getPortByName("example set input"));	
    	roleSetter.getOutputPorts().getPortByName("example set output").connectTo(applier.getInputPorts().getPortByName("unlabelled data"));
    	loader.getOutputPorts().getPortByName("output").connectTo(applier.getInputPorts().getPortByName("model"));
    	
    	applier.getOutputPorts().getPortByName("labelled data").connectTo(
    			process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
    
    	// convert to absolute filenames
    	testFile = toAbsolutePath(testFile);
    	binaryModel = toAbsolutePath(binaryModel);
    	predictionsFile = toAbsolutePath(predictionsFile);
    	
    	Logger.log("Test set file: " + testFile + "\n", Level.INFO);
    	Logger.log("Binary model file: " + binaryModel + "\n", Level.INFO);	
    	Logger.log("Predictions file: " + predictionsFile + "\n", Level.INFO);	
    	
    	arffSource.setParameter(arffSource.PARAMETER_DATA_FILE, testFile);
    	
    	roleSetter.setParameter(roleSetter.PARAMETER_NAME, "survival_status");
    	roleSetter.setParameter(roleSetter.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
    	List<String[]> roles = new ArrayList<String[]>();
    	roles.add(new String[]{"survival_time", SurvivalRule.SURVIVAL_TIME_ROLE});
    	roleSetter.setListParameter(roleSetter.PARAMETER_CHANGE_ATTRIBUTES, roles);
    	
    	loader.setParameter(loader.PARAMETER_MODEL_FILE, binaryModel);
    
    	
    	Logger.log("Applying a model...\n", Level.INFO);	
    	IOContainer out = process.run();
    	ExampleSet set = out.get(ExampleSet.class, 0);
    
    	File file = new File(predictionsFile);
    	BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
    	
    	String s = set.getAnnotations().getAnnotation(SurvivalRuleSet.ANNOTATION_TRAINING_ESTIMATOR);
    	KaplanMeierEstimator kme = new KaplanMeierEstimator();
    	kme.load(s);
    	List<Double> times = kme.getTimes();
    	
    	buf.write("times");
    	for (double t : times) {
    		buf.write( "," + t );
    	}
    	buf.write("\n"); 
    	
    	for (int i = 0; i < set.size(); ++i) {
    		Example e = set.getExample(i);
    		s = e.getValueAsString(e.getAttributes().getSpecial(SurvivalRuleSet.ATTRIBUTE_ESTIMATOR));
    		kme = new KaplanMeierEstimator();
        	kme.load(s);
    		
    		buf.write("instance_" + (i+1) );
    		for (double t : times) {
        		buf.write("," + kme.getProbabilityAt(t));
        	} 
    		buf.write("\n"); 
    	}
    	
    	
    	Logger.log("Done!\n", Level.INFO);
	}
	
	public static String toAbsolutePath(String maybeRelative) {
	    Path path = Paths.get(maybeRelative);
	    Path effectivePath = path;
	    if (!path.isAbsolute()) {
	        Path base = Paths.get("");
	        effectivePath = base.resolve(path).toAbsolutePath();
	    }
	    return effectivePath.normalize().toString();
	}
}
