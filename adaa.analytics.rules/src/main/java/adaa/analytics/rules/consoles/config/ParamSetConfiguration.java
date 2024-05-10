package adaa.analytics.rules.consoles.config;

import adaa.analytics.rules.utils.DoubleFormatter;
import adaa.analytics.rules.utils.Logger;
import adaa.analytics.rules.logic.rulegenerator.RuleGeneratorParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Level;

public class ParamSetConfiguration {

    private static final String RULES_SIGNIFICANT_FIGURES = "rules_significant_figures";
    /**
     * Name of the parameter set
     */
    private String name;

    /**
     * Mapping between parameter names and their values.
     */
    private final Map<String, Object> map = new TreeMap<>();

    public static List<ParamSetConfiguration> readParamSetConfigurations(String filePath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(filePath);
        return readParamSetConfigurations(doc);
    }

    public static List<ParamSetConfiguration> readParamSetConfigurations(Document doc) {

        List<ParamSetConfiguration> paramSets = new ArrayList<>();
        Element paramSetsNode = (Element)doc.getElementsByTagName("parameter_sets").item(0);
        return readParamSetConfigurations(paramSetsNode);
    }

    public static List<ParamSetConfiguration> readParamSetConfigurations(Element parametresSetsElement) {

        List<ParamSetConfiguration> paramSets = new ArrayList<>();
        NodeList paramSetNodes = parametresSetsElement.getElementsByTagName("parameter_set");
        for (int setId = 0; setId < paramSetNodes.getLength(); setId++) {
            ParamSetConfiguration wrapper = new ParamSetConfiguration();
            Element setNode = (Element) paramSetNodes.item(setId);
            wrapper.readParamSetConfiguration(setNode);
            paramSets.add(wrapper);
        }
        return paramSets;
    }

    void readParamSetConfiguration(Element setNode)
    {
        String lineSeparator = System.getProperty("line.separator");
        this.name = setNode.getAttribute("name");
        Logger.log("Reading parameter set " + setNode.getAttribute("name") + lineSeparator, Level.FINE);
        NodeList paramNodes = setNode.getElementsByTagName("param");

        for (int paramId = 0; paramId < paramNodes.getLength(); ++paramId) {
            Element paramNode = (Element) paramNodes.item(paramId);
            String name = paramNode.getAttribute("name");

            // backward compatibility
            if (name.equals("min_rule_covered") || name.equals("mincov_new")) {
                name = RuleGeneratorParams.PARAMETER_MINCOV_NEW;
            }

            String[] expertParamNames = new String[]{RuleGeneratorParams.PARAMETER_EXPERT_RULES, RuleGeneratorParams.PARAMETER_EXPERT_PREFERRED_CONDITIONS, RuleGeneratorParams.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS};

            if (name.equals(RULES_SIGNIFICANT_FIGURES)) {
                String value = paramNode.getTextContent();
                this.map.put(name, Integer.parseInt(value));
                continue;
            }

            // parse expert rules/conditions
            boolean paramProcessed = false;
            for (String expertParamName : expertParamNames) {
                if (name.equals(expertParamName)) {
                    List<String[]> expertRules = new ArrayList<>();
                    NodeList ruleNodes = paramNode.getElementsByTagName("entry");

                    for (int ruleId = 0; ruleId < ruleNodes.getLength(); ++ruleId) {
                        Element ruleNode = (Element) ruleNodes.item(ruleId);
                        String ruleName = ruleNode.getAttribute("name");
                        String ruleContent = ruleNode.getTextContent();
                        expertRules.add(new String[]{ruleName, ruleContent});
                    }
                    this.map.put(expertParamName, expertRules);
                    paramProcessed = true;
                }
            }

            if (!paramProcessed) {
                String value = paramNode.getTextContent();
                this.map.put(name, value);
            }
        }
        prepareParams();
    }

    public String getName() {

        return name;
    }


    private void prepareParams()
    {
        // set rule generator parameters
        Logger.log("\nPARAMETER SET: " + name + "\n", Level.INFO);

        if (this.map.containsKey(RULES_SIGNIFICANT_FIGURES)) {
            int numberOfRulesSignificantFigures = (int) map.get(RULES_SIGNIFICANT_FIGURES);
            this.map.remove(RULES_SIGNIFICANT_FIGURES);
            DoubleFormatter.configure(numberOfRulesSignificantFigures);
        } else {
            DoubleFormatter.defaultConfigure();
        }
    }

    public RuleGeneratorParams generateRuleGeneratorParams()
    {
        RuleGeneratorParams ret = new RuleGeneratorParams();
        for (String key : map.keySet()) {
            Object o = map.get(key);
            boolean paramOk = ret.contains(key);

            if (paramOk)
                if (o instanceof String) {
                    ret.setParameter(key, (String) o);
                } else if (o instanceof List) {
                    ret.setListParameter(key, (List<String[]>) o);
                } else {
                    throw new InvalidParameterException("Invalid paramter type: " + key);
                }
            else {
                throw new InvalidParameterException("Undefined parameter: " + key);
            }
        }


        return ret;
    }
}
