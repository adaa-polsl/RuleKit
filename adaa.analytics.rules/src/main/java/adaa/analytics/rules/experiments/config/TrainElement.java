package adaa.analytics.rules.experiments.config;

import org.w3c.dom.Element;

public class TrainElement {

    public String inFile;
    public String modelFile;
    public String modelCsvFile;

    public TrainElement(Element train) {

        inFile = train.getElementsByTagName("in_file").item(0).getTextContent();
        modelFile = train.getElementsByTagName("model_file").item(0).getTextContent();
        modelCsvFile = ElementUtils.getXmlParameterValue(train, "model_csv");
    }
}
