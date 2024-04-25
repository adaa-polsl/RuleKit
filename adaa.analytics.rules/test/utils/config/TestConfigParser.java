package utils.config;

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.consoles.config.ParamSetConfiguration;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.logic.rulegenerator.RuleGeneratorParams;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class TestConfigParser {

    private static final String TEST_KEY = "test";
    private static final String NAME_KEY = "name";


    private Document document;

    private void createDocument(String filePath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        document = dBuilder.parse(filePath);
    }

    private String getNodeAttributeValue(Node node, String attributeName) {
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        return attribute != null ? attribute.getNodeValue() : null;
    }

    private String getNodeName(Node node) {
        return getNodeAttributeValue(node, NAME_KEY);
    }


    private TestConfig parseTest(Element testElement) {
        TestConfig testConfig = new TestConfig();
        testConfig.name = getNodeName(testElement);
        testConfig.datasets = DatasetConfiguration.readConfigurations(testElement);
        testConfig.paramSetConfigurations = ParamSetConfiguration.readParamSetConfigurations(testElement);
        return testConfig;
    }

    public HashMap<String, TestConfig> parse(String configFilePath) throws ParseException {
        try {
            HashMap<String, TestConfig> testsConfigs = new HashMap<>();
            createDocument(configFilePath);
            NodeList tests = document.getElementsByTagName(TEST_KEY);
            TestConfig testConfig;
            for (int i = 0; i < tests.getLength(); i++) {
                testConfig = parseTest((Element) tests.item(i));
                testsConfigs.put(testConfig.name, testConfig);
            }
            return testsConfigs;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            throw new ParseException("Failed to parse test config file", -1);
        }
    }
}
