package adaa.analytics.rules.consoles;

import adaa.analytics.rules.experiments.SynchronizedReport;
import adaa.analytics.rules.experiments.TrainTestValidationExperiment;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.ExpertRuleGenerator;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.tools.ParameterService;

import org.apache.lucene.util.ArrayUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class ExperimentalConsole {

    /**
     * XML train section data
     */
    public class TrainElement {

        public String inFile;
        public String modelFile;

        private TrainElement(Element train){

            inFile = train.getElementsByTagName("in_file").item(0).getTextContent();
            modelFile = train.getElementsByTagName("model_file").item(0).getTextContent();
        }
    }

    /**
     * XML predict section data
     */
    public class PredictElement {

        public String modelFile;
        public String testFile;
        public String predictionsFile;

        private PredictElement(Element predict){

            modelFile = predict.getElementsByTagName("model_file").item(0).getTextContent();
            testFile = predict.getElementsByTagName("test_file").item(0).getTextContent();
            predictionsFile = predict.getElementsByTagName("predictions_file").item(0).getTextContent();
        }
    }

    private class ParamSetWrapper {
        String name;
        final Map<String, Object> map = new TreeMap<>();
    }
    
    
    static final String VERSION = "1.0.0"; 
    static final String BUILD_DATE = "26.05.2019";
    
   
    public static void main(String[] args) {
        
    	ExperimentalConsole console = new ExperimentalConsole();
    	console.parse(args);
    }
    
    private void parse(String[] args) {
    	try {
    		
	    	System.out.print("RuleKit version " + VERSION + " (" + BUILD_DATE + ")\n" 
			+ "    RuleKit Development Team (c) 2018\n\n");
    	
	    	ArrayList<String> argList = new ArrayList();
	    	argList.addAll(Arrays.asList(args));
	    
	    	boolean isVerbose = findSwitch(argList, "-v");
	    	Logger.getInstance().addStream(System.out, isVerbose ? Level.FINE : Level.INFO);	
	    
            if (argList.size() == 1) {
            	RapidMiner.init();
            	execute(args[0]);

            } else {
                Logger.log("Usage:\njava -jar RuleKit.jar [-v] <experiment_xml>\n"
                		+ "    <experiment_xml> - XML file with experimentel setting description\n"
                		+ "    -v - verbose mode\n", Level.INFO);
            	
            }
    	
        } catch (IOException | ParserConfigurationException | SAXException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void execute(String configFile) throws ParserConfigurationException, SAXException, IOException, InterruptedException, ExecutionException {
      
        String lineSeparator = System.getProperty("line.separator");

        int threadCount = 1; //Runtime.getRuntime().availableProcessors();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future> futures = new ArrayList<>();

        List<ParamSetWrapper> paramSets = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(configFile);

        NodeList paramSetNodes = doc.getElementsByTagName("parameter_set");

        Logger.log("Loading XML experiment file: " + configFile + "..." , Level.INFO);
        
        for (int setId = 0; setId < paramSetNodes.getLength(); setId++) {
            ParamSetWrapper wrapper = new ParamSetWrapper();
            Element setNode = (Element) paramSetNodes.item(setId);
            wrapper.name = setNode.getAttribute("name");
            Logger.log("Reading parameter set " + setNode.getAttribute("name")
                    + lineSeparator, Level.FINE);
            NodeList paramNodes = setNode.getElementsByTagName("param");

            for (int paramId = 0; paramId < paramNodes.getLength(); ++paramId) {
                Element paramNode = (Element) paramNodes.item(paramId);
                String name = paramNode.getAttribute("name");

                String[] expertParamNames = new String[]{
                        ExpertRuleGenerator.PARAMETER_EXPERT_RULES,
                        ExpertRuleGenerator.PARAMETER_EXPERT_PREFERRED_CONDITIONS,
                        ExpertRuleGenerator.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS
                };

                // parse expert rules/conditions
                boolean paramProcessed = false;
                for (String expertParamName : expertParamNames) {
                    if (name.equals(expertParamName)) {
                        List<String[]> expertRules = new ArrayList<>();
                        NodeList ruleNodes = paramNode.getElementsByTagName("entry");

                        for (int ruleId = 0; ruleId < ruleNodes.getLength(); ++ruleId) {
                            Element ruleNode = (Element) ruleNodes.item(ruleId);
                            String ruleName = ruleNode.getAttribute("name");
                            String ruleContent = ruleNode.getTextContent();
                            expertRules.add(new String[]{ruleName, ruleContent});
                        }
                        wrapper.map.put(expertParamName, expertRules);
                        paramProcessed = true;
                    }
                }

                if (!paramProcessed) {
                    String value = paramNode.getTextContent();
                    wrapper.map.put(name, value);
                }
            }

            paramSets.add(wrapper);
        }

        // Dataset
        Logger.log("Processing datasets" + lineSeparator, Level.FINE);
        NodeList datasetNodes = doc.getElementsByTagName("dataset");
        for (int datasetId = 0; datasetId < datasetNodes.getLength(); datasetId++) {
            Logger.log("Processing dataset" + datasetId + lineSeparator, Level.FINE);
            Element node = (Element) datasetNodes.item(datasetId);

            String label = node.getElementsByTagName("label").item(0).getTextContent();
            String outDirectory = node.getElementsByTagName("out_directory").item(0).getTextContent();

            Map<String, String> options = new HashMap<>();
            if (node.getElementsByTagName(SurvivalRule.SURVIVAL_TIME_ROLE).getLength() > 0) {
                String val = node.getElementsByTagName(SurvivalRule.SURVIVAL_TIME_ROLE).item(0).getTextContent();
                options.put(SurvivalRule.SURVIVAL_TIME_ROLE, val);
            }

            if (node.getElementsByTagName(Attributes.WEIGHT_NAME).getLength() > 0) {
                String val = node.getElementsByTagName(Attributes.WEIGHT_NAME).item(0).getTextContent();
                options.put(Attributes.WEIGHT_NAME, val);
            }

            Logger.log("Out directory " + outDirectory + lineSeparator +
                    "Label " + label + lineSeparator, Level.FINE);

            // Training
            String trainingReportFilePath = null;
            List<TrainElement> trainElements = new ArrayList<>();

            NodeList trainingNodes = node.getElementsByTagName("training");
            if(trainingNodes.getLength() == 1){
                Element trainingElement = (Element)trainingNodes.item(0);

                trainingReportFilePath = trainingElement.getElementsByTagName("report_file").item(0).getTextContent();

                Logger.log("Report file " + trainingReportFilePath + lineSeparator, Level.FINE);

                NodeList trainNodes = node.getElementsByTagName("train");
                for(int trainId = 0 ; trainId <trainNodes.getLength() ; trainId++ ){

                    Element trainElement = (Element)trainNodes.item(trainId);
                    trainElements.add(new TrainElement(trainElement));

                    Logger.log("In file " + trainElements.get(trainElements.size()-1).inFile + lineSeparator +
                            "Model file " + trainElements.get(trainElements.size()-1).modelFile + lineSeparator, Level.FINE);
                }
            }

            // Prediction
            String predictionPerformanceFilePath = null;
            List<PredictElement> predictElements = new ArrayList<>();

            NodeList predictionNodes = node.getElementsByTagName("prediction");
            if(predictionNodes.getLength() == 1){
                Element predictionElement = (Element)predictionNodes.item(0);

                predictionPerformanceFilePath = predictionElement.getElementsByTagName("performance_file").item(0).getTextContent();

                Logger.log("Performance file " + predictionPerformanceFilePath + lineSeparator, Level.FINE);

                NodeList predictNodes = node.getElementsByTagName("predict");
                for(int predictId = 0 ; predictId <predictNodes.getLength() ; predictId++ ){

                    Element predictElement = (Element)predictNodes.item(predictId);
                    predictElements.add(new PredictElement(predictElement));

                    Logger.log("Model file " + predictElements.get(predictElements.size()-1).modelFile + lineSeparator +
                            "Test file " + predictElements.get(predictElements.size()-1).testFile + lineSeparator +
                            "Predictions file " + predictElements.get(predictElements.size()-1).predictionsFile + lineSeparator, Level.FINE);

                }
            }
            
            Logger.log("OK\n", Level.INFO);
            
            // create experiments for all params sets
            for (ParamSetWrapper wrapper : paramSets) {
//                StringBuilder paramString = new StringBuilder();
//
//                if (wrapper.name.length() > 0) {
//                    paramString.append(", ").append(wrapper.name);
//
//                } else {
//                    for (String key : wrapper.map.keySet()) {
//                        Object o = wrapper.map.get(key);
//                        if (o instanceof String) {
//                            paramString.append(", ").append(key).append("=").append(wrapper.map.get(key));
//
//                        }
//                    }
//                }

                String outDirPath = outDirectory + "/" + wrapper.name;

                //noinspection ResultOfMethodCallIgnored
                new File(outDirPath).mkdirs();

                TrainTestValidationExperiment ttValidationExp;
                Future f;

                Logger.log("Creating new TrainTestValidationExperiment" + lineSeparator +
                        "outDirPath = " + outDirPath + lineSeparator +
                        "trainingReportPathFile = " + trainingReportFilePath + lineSeparator +
                        "predictionReportPathFile = " + predictionPerformanceFilePath + lineSeparator, Level.FINE);

                SynchronizedReport predictionSynchronizedReport = predictionPerformanceFilePath == null || predictionPerformanceFilePath.isEmpty() ?
                        null : new SynchronizedReport(outDirPath + "/" + predictionPerformanceFilePath);
                SynchronizedReport trainingSynchronizedReport = trainingReportFilePath == null || trainingReportFilePath.isEmpty() ?
                        null : new SynchronizedReport(outDirPath + "/" + trainingReportFilePath);

                ttValidationExp = new TrainTestValidationExperiment(trainingSynchronizedReport, predictionSynchronizedReport,
                        label, options, wrapper.map, outDirPath, trainElements, predictElements);

                
                Logger.log("\nPARAMETER SET: " + wrapper.name + "\n", Level.INFO);
                f = pool.submit(ttValidationExp);
                futures.add(f);
            }
        }
      
        for (Future f : futures) {
            f.get();
        }

        Logger.log("Experiments finished", Level.INFO);
        RapidMiner.quit(RapidMiner.ExitMode.NORMAL);
    }
    
    
    private boolean findSwitch(List<String> params, String name) {
    	if (params.contains(name)) {
    		params.remove(name);
    		return true;
    	}
    	
    	return false;
    }
}
