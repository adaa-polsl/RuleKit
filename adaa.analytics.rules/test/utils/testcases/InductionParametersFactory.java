package utils.testcases;

import adaa.analytics.rules.logic.induction.InductionParameters;

import java.util.HashMap;

public class InductionParametersFactory {

    public static InductionParameters make(HashMap<String, Object> paramMap) {
        InductionParameters parameters = new InductionParameters();
        for (String key : paramMap.keySet()) {
            switch (key) {
                case "min_rule_covered":
                    parameters.setMinimumCovered(Double.parseDouble((String) paramMap.get(key)));
                    break;
                case "max_rule_covered":
                    parameters.setMaximumUncoveredFraction(Double.parseDouble((String) paramMap.get(key)));
                    break;
                case "max_growing":
                    parameters.setMaxGrowingConditions(Double.parseDouble((String) paramMap.get(key)));
                    break;
                case "ignore_missing":
                    parameters.setIgnoreMissing(paramMap.get(key).equals("true"));
                    break;
                case "enable_pruning":
                    parameters.setEnablePruning(paramMap.get(key).equals("true"));
                    break;
            }
        }
        return parameters;
    }
}
