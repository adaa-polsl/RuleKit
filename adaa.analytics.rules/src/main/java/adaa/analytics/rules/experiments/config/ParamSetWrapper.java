package adaa.analytics.rules.experiments.config;

import adaa.analytics.rules.logic.representation.DoubleFormatter;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.RuleGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.logging.Level;

public class ParamSetWrapper {

    private static final String RULES_SIGNIFICANT_FIGURES = "rules_significant_figures";
    /**
     * Name of the parameter set
     */
    private String name;

    /**
     * Mapping between parameter names and their values.
     */
    private final Map<String, Object> map = new TreeMap<>();

    public static List<ParamSetWrapper> readParamSets(Document doc) {

        List<ParamSetWrapper> paramSets = new ArrayList<>();
        NodeList paramSetNodes = doc.getElementsByTagName("parameter_set");

        for (int setId = 0; setId < paramSetNodes.getLength(); setId++) {
            ParamSetWrapper wrapper = new ParamSetWrapper();
            Element setNode = (Element) paramSetNodes.item(setId);
            wrapper.readParamSet(setNode);
            paramSets.add(wrapper);
        }
        return paramSets;
    }

    public void readParamSet(Element setNode)
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
                name = RuleGenerator.PARAMETER_MINCOV_NEW;
            }

            String[] expertParamNames = new String[]{ExpertRuleGenerator.PARAMETER_EXPERT_RULES, ExpertRuleGenerator.PARAMETER_EXPERT_PREFERRED_CONDITIONS, ExpertRuleGenerator.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS};

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

    public Object getParam(String key)
    {
        return this.map.get(key);
    }

    public Set<String> listKeys()
    {
        return this.map.keySet();
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
}
