package adaa.analytics.rules.consoles;

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.utils.OperatorException;
import ioutils.ArffFileLoader;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import utils.TestResourcePathFactory;
import utils.config.TestConfig;
import utils.config.TestConfigParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Class used to run test process to profile execution of prediction
 */
public class RunTestProcess {

    private static String configFilePath = "c:\\Users\\wojciech.gorka\\Downloads\\poker.xml";


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