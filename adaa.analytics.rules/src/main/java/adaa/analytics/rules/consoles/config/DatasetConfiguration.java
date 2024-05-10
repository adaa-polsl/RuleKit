package adaa.analytics.rules.consoles.config;

import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import adaa.analytics.rules.utils.Logger;
import adaa.analytics.rules.logic.representation.rule.SurvivalRule;
import adaa.analytics.rules.data.IExampleSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DatasetConfiguration {
    private static final String WEIGHT_NAME = "weight";
    public String testingReportFilePath;
    public String trainingReportFilePath;

    public String predictionPerformanceFilePath;

    public String label;

    private Map<String, String> options = new HashMap<>();

    public String outDirectory;

    public List<TrainElement> trainElements = new ArrayList<>();

    public java.util.List<PredictElement> predictElements = new ArrayList<>();

    private void readConfiguration(Element node) {
        String lineSeparator = System.getProperty("line.separator");
        label = ElementUtils.getXmlParameterValue(node, "label");

        outDirectory = ElementUtils.getXmlParameterValue(node, "out_directory");
        addOptionIfValueExist(node, "ignore");
        addOptionIfValueExist(node, SurvivalRule.SURVIVAL_TIME_ROLE);
        addOptionIfValueExist(node, ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
        addOptionIfValueExist(node, WEIGHT_NAME);

        Logger.log("Out directory " + outDirectory + lineSeparator +
                "Label " + label + lineSeparator, Level.FINE);

        // Training

        NodeList trainingNodes = node.getElementsByTagName("training");
        if (trainingNodes.getLength() > 0) {
            Element trainingElement = (Element) trainingNodes.item(0);

            trainingReportFilePath = ElementUtils.getXmlParameterValue(trainingElement, "report_file");

            Logger.log("Report file " + trainingReportFilePath + lineSeparator, Level.FINE);

            NodeList trainNodes = node.getElementsByTagName("train");
            for (int trainId = 0; trainId < trainNodes.getLength(); trainId++) {

                Element trainElement = (Element) trainNodes.item(trainId);
                trainElements.add(new TrainElement(trainElement));

                Logger.log("In file " + trainElements.get(trainElements.size() - 1).inFile + lineSeparator +
                        "Model file " + trainElements.get(trainElements.size() - 1).modelFile + lineSeparator, Level.FINE);
            }
        }

        // Prediction

        NodeList predictionNodes = node.getElementsByTagName("prediction");
        if (predictionNodes.getLength() > 0) {
            Element predictionElement = (Element) predictionNodes.item(0);

            predictionPerformanceFilePath = ElementUtils.getXmlParameterValue(predictionElement, "performance_file");
            testingReportFilePath = ElementUtils.getXmlParameterValue(predictionElement, "report_file");

            Logger.log("Performance file " + predictionPerformanceFilePath + lineSeparator, Level.FINE);

            NodeList predictNodes = node.getElementsByTagName("predict");
            for (int predictId = 0; predictId < predictNodes.getLength(); predictId++) {

                Element predictElement = (Element) predictNodes.item(predictId);
                predictElements.add(new PredictElement(predictElement));

                Logger.log("Model file " + predictElements.get(predictElements.size() - 1).modelFile + lineSeparator +
                        "Test file " + predictElements.get(predictElements.size() - 1).testFile + lineSeparator +
                        "Predictions file " + predictElements.get(predictElements.size() - 1).predictionsFile + lineSeparator, Level.FINE);

            }
        }

        Logger.log(" [OK]\n", Level.INFO);
    }

    private void addOptionIfValueExist(Element searchedElement, String optionName) {
        String value = ElementUtils.getXmlParameterValue(searchedElement, optionName);
        if (value != null)
            options.put(optionName, value);
    }

    private static Document readXmlDocument(String configFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(configFile);
        return doc;
    }

    public static List<DatasetConfiguration> readConfigurations(String filePath) throws ParserConfigurationException, IOException, SAXException {
        Document doc = readXmlDocument(filePath);
        return readConfigurations(doc);
    }

    public static List<DatasetConfiguration> readConfigurations(Document doc) {
        String lineSeparator = System.getProperty("line.separator");
        List<DatasetConfiguration> ret = new ArrayList<>();
        Logger.log("Processing datasets" + lineSeparator, Level.FINE);
        NodeList datasetNodes = doc.getChildNodes();
        return readConfigurations((Element) datasetNodes.item(0));
    }

    public static List<DatasetConfiguration> readConfigurations(Element datasetsElement) {
        String lineSeparator = System.getProperty("line.separator");
        List<DatasetConfiguration> ret = new ArrayList<>();
        NodeList datasetNodes = datasetsElement.getElementsByTagName("dataset");
        for (int datasetId = 0; datasetId < datasetNodes.getLength(); datasetId++) {
            Logger.log("Processing dataset" + datasetId + lineSeparator, Level.FINE);
            Element node = (Element) datasetNodes.item(datasetId);
            DatasetConfiguration dc = new DatasetConfiguration();
            dc.readConfiguration(node);
            ret.add(dc);
        }
        return ret;
    }

    public boolean hasOptionParameter(String param) {
        return options.containsKey(param);
    }

    private List<String[]> generateRoles() {
        List<String[]> roles = new ArrayList<>();

        // add custom roles to mask ignored attributes
        if (options.containsKey("ignore")) {
            String[] attrs = options.get("ignore").split(",");
            int i = 0;
            for (String a : attrs) {
                roles.add(new String[]{a, "ignored_" + i});
                ++i;
            }
        }

        // survival dataset - set proper role
        if (options.containsKey(SurvivalRule.SURVIVAL_TIME_ROLE)) {
            roles.add(new String[]{options.get(SurvivalRule.SURVIVAL_TIME_ROLE), SurvivalRule.SURVIVAL_TIME_ROLE});
        }

        if (options.containsKey(WEIGHT_NAME)) {
            roles.add(new String[]{options.get(WEIGHT_NAME), WEIGHT_NAME});
        }

        return roles;
    }

    public void applyParametersToExempleSet(IExampleSet exampleSet){
        RoleConfigurator roleConfigurator = new RoleConfigurator(label);

        List<String[]> roles = generateRoles();

        if (hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            String contrastAttr = options.get(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

            // use annotation for storing contrast attribute info
            roleConfigurator.configureContrast(contrastAttr);
        }

        if (roles.size() > 0) {
            roleConfigurator.configureRoles(roles);
        }
        roleConfigurator.apply(exampleSet);
    }
}
