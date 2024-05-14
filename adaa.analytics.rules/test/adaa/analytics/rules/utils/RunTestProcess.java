package adaa.analytics.rules.utils;

import adaa.analytics.rules.consoles.ModelFileInOut;
import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import ioutils.ArffFileLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Class used to run test process to profile execution of prediction
 */
public class RunTestProcess {

    private static String configFilePath = "poker.xml";


    public static void main(String[] args) throws IOException, ClassNotFoundException, OperatorException, ParserConfigurationException, SAXException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document document = dBuilder.parse(configFilePath);

        DatasetConfiguration ds = DatasetConfiguration.readConfigurations(document).get(0);

        IExampleSet testEs = new ArffFileLoader().load(ds.predictElements.get(0).testFile, ds.label);
        ds.applyParametersToExempleSet(testEs);

        RuleSetBase model = ModelFileInOut.read(ds.predictElements.get(0).modelFile);
        System.in.read();
        long start = System.currentTimeMillis();

        IExampleSet appliedEs = model.apply(testEs);

        System.out.println((System.currentTimeMillis() - start) + "ms");
    }
}