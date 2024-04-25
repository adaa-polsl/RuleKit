package adaa.analytics.rules.consoles.config;

import org.w3c.dom.Element;

public class PredictElement {

    public String modelFile;
    public String testFile;
    public String predictionsFile;

    public PredictElement(Element predict) {

        modelFile = ElementUtils.getXmlParameterValue(predict,"model_file");
        testFile = ElementUtils.getXmlParameterValue(predict,"test_file");
        predictionsFile = ElementUtils.getXmlParameterValue(predict,"predictions_file");
    }
}
