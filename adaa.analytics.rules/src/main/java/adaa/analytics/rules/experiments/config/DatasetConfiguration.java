package adaa.analytics.rules.experiments.config;

import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import com.rapidminer.example.Attributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DatasetConfiguration {
    public String testingReportFilePath;
    public String trainingReportFilePath;

    public String predictionPerformanceFilePath;

    public String label;

    public Map<String, String> options = new HashMap<>();

    public String outDirectory;

    public List<TrainElement> trainElements = new ArrayList<>();

    public java.util.List<PredictElement> predictElements = new ArrayList<>();

    public void readConfiguration(Element node)
    {
        String lineSeparator = System.getProperty("line.separator");
        label = node.getElementsByTagName("label").item(0).getTextContent();
        outDirectory = node.getElementsByTagName("out_directory").item(0).getTextContent();

        NodeList ignoreNodes = node.getElementsByTagName("ignore");
        if (ignoreNodes.getLength() > 0) {
            options.put("ignore", ignoreNodes.item(0).getTextContent());
        }

        if (node.getElementsByTagName(SurvivalRule.SURVIVAL_TIME_ROLE).getLength() > 0) {
            String val = node.getElementsByTagName(SurvivalRule.SURVIVAL_TIME_ROLE).item(0).getTextContent();
            options.put(SurvivalRule.SURVIVAL_TIME_ROLE, val);
        }

        if (node.getElementsByTagName(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getLength() > 0) {
            String val = node.getElementsByTagName(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).item(0).getTextContent();
            options.put(ContrastRule.CONTRAST_ATTRIBUTE_ROLE, val);
        }

        if (node.getElementsByTagName(Attributes.WEIGHT_NAME).getLength() > 0) {
            String val = node.getElementsByTagName(Attributes.WEIGHT_NAME).item(0).getTextContent();
            options.put(Attributes.WEIGHT_NAME, val);
        }

        Logger.log("Out directory " + outDirectory + lineSeparator +
                "Label " + label + lineSeparator, Level.FINE);

        // Training

        NodeList trainingNodes = node.getElementsByTagName("training");
        if (trainingNodes.getLength() == 1) {
            Element trainingElement = (Element) trainingNodes.item(0);

            trainingReportFilePath = trainingElement.getElementsByTagName("report_file").item(0).getTextContent();

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
        if (predictionNodes.getLength() == 1) {
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

    public static List<DatasetConfiguration> readConfigurations(Document doc)
    {
        String lineSeparator = System.getProperty("line.separator");
        List<DatasetConfiguration> ret = new ArrayList<>();
        Logger.log("Processing datasets" + lineSeparator, Level.FINE);
        NodeList datasetNodes = doc.getElementsByTagName("dataset");
        for (int datasetId = 0; datasetId < datasetNodes.getLength(); datasetId++) {
            Logger.log("Processing dataset" + datasetId + lineSeparator, Level.FINE);
            Element node = (Element) datasetNodes.item(datasetId);
            DatasetConfiguration dc = new DatasetConfiguration();
            dc.readConfiguration(node);
            ret.add(dc);
        }
        return ret;
    }
}
