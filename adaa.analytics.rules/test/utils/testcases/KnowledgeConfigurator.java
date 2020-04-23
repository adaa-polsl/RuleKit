package utils.testcases;

import adaa.analytics.rules.logic.representation.Knowledge;

import java.util.HashMap;

public class KnowledgeConfigurator {

    public static void configure(Knowledge knowledge, HashMap<String, Object> params) {
        for (String key : params.keySet()) {
            switch (key) {
                case "extend_using_preferred":
                    knowledge.setExtendUsingPreferred(params.get(key).equals("true"));
                    break;
                case "extend_using_automatic":
                    knowledge.setExtendUsingAutomatic(params.get(key).equals("true"));
                    break;
                case "induce_using_preferred":
                    knowledge.setInduceUsingPreferred(params.get(key).equals("true"));
                    break;
                case "induce_using_automatic":
                    knowledge.setInduceUsingAutomatic(params.get(key).equals("true"));
                    break;
                case "consider_other_classes":
                    knowledge.setConsiderOtherClasses(params.get(key).equals("true"));
                    break;
                case "preferred_conditions_per_rule":
                    knowledge.setPreferredConditionsPerRule(Integer.parseInt((String) params.get(key)));
                    break;
                case "preferred_attributes_per_rule":
                    knowledge.setPreferredAttributesPerRule(Integer.parseInt((String) params.get(key)));
                    break;
            }
        }
    }
}
