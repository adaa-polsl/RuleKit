package adaa.analytics.rules.consoles.config;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TrainElement {

    public String inFile;
    public String modelFile;
    public String modelCsvFile;

    public TrainElement(Element train) {

        inFile = ElementUtils.getXmlParameterValue(train, "in_file");
        modelFile = ElementUtils.getXmlParameterValue(train, "model_file");
        modelCsvFile = ElementUtils.getXmlParameterValue(train, "model_csv");
    }
}
