package utils.config;

import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
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
    private static final String IN_FILE_KEY = "in_file";
    private static final String TRAINING_KEY = "training";
    private static final String TRAIN_KEY = "train";
    private static final String LABEL_KEY = "label";
    private static final String DATASET_KEY = "dataset";
    private static final String DATASETS_KEY = "datasets";
    private static final String PARAM_KEY = "param";
    private static final String PARAMETERS_SET_KEY = "parameter_sets";
    private static final String PARAMETERS_KEY = "parameter_set";
    private static final String ENTRY_KEY = "entry";

    private static final List<String> EXPERTS_RULES_PARAMETERS_NAMES = Arrays.asList(
            ExpertRuleGenerator.PARAMETER_EXPERT_RULES,
            ExpertRuleGenerator.PARAMETER_EXPERT_PREFERRED_CONDITIONS,
            ExpertRuleGenerator.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS);

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

    private String parseSurvivalTime(Element testElement) {
        NodeList elements =  testElement.getElementsByTagName(SurvivalRule.SURVIVAL_TIME_ROLE);

        if (elements.getLength() > 0) {
            return elements.item(0).getTextContent();
        }
        else return null;
    }

    private List<String[]> parseExpertRulesParameter(NodeList children) {
        List<String[]> expertRules = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Element entryElement = (Element) children.item(i);
            String ruleName = getNodeName(entryElement);
            String ruleContent = entryElement.getTextContent();
            expertRules.add(new String[]{ruleName, ruleContent});
        }
        return expertRules;
    }

    private void checkAmbigousDatasetsNames(List<TestDataSetConfig> datasetsConfigs) {
        List<String> dataSetsNames = datasetsConfigs.stream()
                .map((TestDataSetConfig element) -> element.name)
                .collect(Collectors.toList());
        Set<String> set = new HashSet<>(dataSetsNames);
        if(set.size() < dataSetsNames.size()){
            throw new IllegalArgumentException("Datasets are ambigous. Provide a unique 'name' attribute for each dataset" +
                    " or make sure train files have different filenames.");
        }
    }

    private TestDataSetConfig parseDataSet(Element datasetElement) {
        TestDataSetConfig dataSetConfig = new TestDataSetConfig();
        dataSetConfig.labelAttribute = datasetElement.getElementsByTagName(LABEL_KEY).item(0).getTextContent();
        Element trainElement = (Element) datasetElement.getElementsByTagName(TRAINING_KEY).item(0);
        trainElement = (Element) trainElement.getElementsByTagName(TRAIN_KEY).item(0);
        dataSetConfig.trainFileName = trainElement.getElementsByTagName(IN_FILE_KEY).item(0).getTextContent();
        dataSetConfig.name = getNodeName(datasetElement);
        if (dataSetConfig.name == null) {
            Path path = Paths.get(dataSetConfig.trainFileName);
            dataSetConfig.name = path.getFileName().toString();
            dataSetConfig.name = dataSetConfig.name.substring(0, dataSetConfig.name.lastIndexOf('.'));
        }
        return dataSetConfig;
    }

    private List<TestDataSetConfig> parseDataSets(Element testElement) {
        List<TestDataSetConfig> datasets = new ArrayList<>();
        NodeList parameterSetNodes = testElement.getElementsByTagName(DATASETS_KEY);
        parameterSetNodes = ((Element) parameterSetNodes.item(0)).getElementsByTagName(DATASET_KEY);
        Element datasetElement;
        for (int i = 0; i < parameterSetNodes.getLength(); i++) {
            datasetElement = (Element) parameterSetNodes.item(i);
            datasets.add(parseDataSet(datasetElement));
        }
        checkAmbigousDatasetsNames(datasets);
        return datasets;
    }

    private HashMap<String, Object> parseTestParameters(Element parametersSetElement) {
        HashMap<String, Object> parameters = new HashMap<>();
        NodeList parametersNodes = parametersSetElement.getElementsByTagName(PARAM_KEY);
        List<String[]> roles = new ArrayList<>();
        for (int i = 0; i < parametersNodes.getLength(); i++) {
            Node paramNode = parametersNodes.item(i);
            String paramName = getNodeName(paramNode);
            Object value;
            if (EXPERTS_RULES_PARAMETERS_NAMES.contains(paramName)) {
                value = parseExpertRulesParameter(((Element) paramNode).getElementsByTagName(ENTRY_KEY));
            } else {
                value = paramNode.getTextContent();
            }
            parameters.put(paramName, value);
        }
        return parameters;
    }

    private HashMap<String, HashMap<String, Object>> parseTestParametersSets(Element testElement) {
        HashMap<String, HashMap<String, Object>> parametersSets = new HashMap<>();
        NodeList parameterSetNodes = testElement.getElementsByTagName(PARAMETERS_SET_KEY);
        parameterSetNodes = ((Element) parameterSetNodes.item(0)).getElementsByTagName(PARAMETERS_KEY);
        Element parameterSetElement;
        for (int i = 0; i < parameterSetNodes.getLength(); i++) {
            parameterSetElement = (Element) parameterSetNodes.item(i);
            parametersSets.put(getNodeName(parameterSetElement), parseTestParameters(parameterSetElement));
        }
        return parametersSets;
    }

    private TestConfig parseTest(Element testElement) {
        HashMap<String, HashMap<String, Object>> parametersSets = parseTestParametersSets(testElement);
        List<TestDataSetConfig> dataSetsConfig = parseDataSets(testElement);
        String testName = getNodeName(testElement);
        String survivalTime = parseSurvivalTime(testElement);

        TestConfig testConfig = new TestConfig();
        testConfig.name = testName;
        testConfig.datasets = dataSetsConfig;
        testConfig.parametersConfigs = parametersSets;
        testConfig.survivalTime = survivalTime;
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
