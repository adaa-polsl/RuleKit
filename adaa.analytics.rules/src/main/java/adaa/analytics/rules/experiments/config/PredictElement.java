package adaa.analytics.rules.experiments.config;

import org.w3c.dom.Element;

public class PredictElement {

    public String modelFile;
    public String testFile;
    public String predictionsFile;

    public PredictElement(Element predict) {

        modelFile = predict.getElementsByTagName("model_file").item(0).getTextContent();
        testFile = predict.getElementsByTagName("test_file").item(0).getTextContent();
        predictionsFile = predict.getElementsByTagName("predictions_file").item(0).getTextContent();
    }
}
