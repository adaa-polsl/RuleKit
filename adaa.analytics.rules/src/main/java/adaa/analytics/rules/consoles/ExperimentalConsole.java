package adaa.analytics.rules.consoles;

import adaa.analytics.rules.experiments.ExperimentBase;
import adaa.analytics.rules.experiments.InternalXValidationExperiment;
import adaa.analytics.rules.experiments.SplittedXValidationExperiment;
import adaa.analytics.rules.experiments.SynchronizedReport;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import com.rapidminer.RapidMiner;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class ExperimentalConsole {

    protected class ParamSetWrapper {
        String name;
        Map<String, Object> map = new TreeMap<String, Object>();
    }

    public static void main(String[] args) {
        try {
            if (args.length == 1) {

                ExperimentalConsole console = new ExperimentalConsole();
                console.execute(args[0]);

            } else {
                throw new IllegalArgumentException("Please specify two arguments");
            }

        } catch (IOException | ParserConfigurationException | SAXException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected void execute(String configFile) throws ParserConfigurationException, SAXException, IOException, InterruptedException, ExecutionException {
        RapidMiner.init();
        Logger.getInstance().addStream(System.out, Level.FINE);
        String lineSeparator = System.getProperty("line.separator");

        int threadCount = Runtime.getRuntime().availableProcessors();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future> futures = new ArrayList<Future>();

        List<ParamSetWrapper> paramSets = new ArrayList<ParamSetWrapper>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = (Document) dBuilder.parse(configFile);

        NodeList paramSetNodes = ((Document) doc).getElementsByTagName("parameter_set");

        for (int setId = 0; setId < paramSetNodes.getLength(); setId++) {
            ParamSetWrapper wrapper = new ParamSetWrapper();
            Element setNode = (Element) paramSetNodes.item(setId);
            wrapper.name = setNode.getAttribute("name");
            Logger.getInstance().log("Reading parameter set " + setNode.getAttribute("name")
                    + lineSeparator, Level.INFO);
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
                        List<String[]> expertRules = new ArrayList<String[]>();
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
        Logger.getInstance().log("Processing datasets" + lineSeparator, Level.INFO);
        NodeList datasetNodes = ((Document) doc).getElementsByTagName("dataset");
        for (int datasetId = 0; datasetId < datasetNodes.getLength(); datasetId++) {
            Logger.getInstance().log("Processing dataset" + datasetId + lineSeparator, Level.INFO);
            Element node = (Element) datasetNodes.item(datasetId);

            String name = node.getAttribute("name");
            String path = node.getElementsByTagName("path").item(0).getTextContent();
            String label = node.getElementsByTagName("label").item(0).getTextContent();
            String typeString = node.getElementsByTagName("type").item(0).getTextContent();
            String reportPath = node.getElementsByTagName("report_path").item(0).getTextContent();

            Logger.getInstance().log("Name " + name + lineSeparator +
                    "Path " + path + lineSeparator +
                    "Label " + label + lineSeparator +
                    "Type string " + typeString + lineSeparator +
                    "Report path " + reportPath + lineSeparator, Level.INFO);

            ExperimentBase.Type type;
            if (typeString.equals("BinaryClassification")) {
                type = ExperimentBase.Type.BINARY_CLASSIFICATION;
            } else if (typeString.equals("Classification")) {
                type = ExperimentBase.Type.CLASSIFICATION;
            } else if (typeString.equals("Regression")) {
                type = ExperimentBase.Type.REGRESSION;
            } else if (typeString.equals("Survival")) {
                type = ExperimentBase.Type.SURVIVAL_BY_REGRESSION;
            } else {
                throw new IllegalArgumentException();
            }

            // create experiments for all params sets
            for (ParamSetWrapper wrapper : paramSets) {
                String paramString = "";

                if (wrapper.name.length() > 0) {
                    paramString += ", " + wrapper.name;

                } else {
                    for (String key : wrapper.map.keySet()) {
                        Object o = wrapper.map.get(key);
                        if (o instanceof String) {
                            paramString += ", " + key + "=" + wrapper.map.get(key);

                        }
                    }
                }

                File file = new File(path);

                String reportFile = reportPath + "/" + name + paramString + ".csv";
                String modelFile = reportPath + "/" + name + paramString + ".res";

                ExperimentBase exp;

                if (file.isDirectory()) {
                    Logger.getInstance().log("Creating new SplittedXValidationExperiment" + lineSeparator, Level.INFO);
                    exp = new SplittedXValidationExperiment(file, new SynchronizedReport(reportFile), new SynchronizedReport(modelFile), label, type, wrapper.map);
                } else {
                    Logger.getInstance().log("Creating new InternalXValidationExperiment" + lineSeparator, Level.INFO);
                    exp = new InternalXValidationExperiment(file, new SynchronizedReport(reportFile), new SynchronizedReport(modelFile), label, 10, type, wrapper.map);
                }
                Future f = pool.submit(exp);
                futures.add(f);
            }
        }
        Logger.getInstance().log("Finished processing datasets" + lineSeparator, Level.INFO);

        for (Future f : futures) {
            f.get();
        }

        Logger.getInstance().log("Experiments finished", Level.INFO);
        RapidMiner.quit(RapidMiner.ExitMode.NORMAL);
    }
}
