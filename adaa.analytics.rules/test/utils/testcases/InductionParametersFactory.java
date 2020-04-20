package utils.testcases;

import adaa.analytics.rules.logic.induction.InductionParameters;

import java.util.HashMap;

public class InductionParametersFactory {

    public static InductionParameters make(HashMap<String, Object> paramMap) {
        InductionParameters parameters = new InductionParameters();
        for (String key : paramMap.keySet()) {
            String value = (String) paramMap.get(key);
            switch (key) {
                case "min_rule_covered":
                    parameters.setMinimumCovered(Double.parseDouble(value));
                    break;
                case "max_rule_covered":
                    parameters.setMaximumUncoveredFraction(Double.parseDouble(value));
                    break;
                case "max_growing":
                    parameters.setMaxGrowingConditions(Double.parseDouble(value));
                    break;
                case "ignore_missing":
                    parameters.setIgnoreMissing(value.equals("true"));
                    break;
                case "pruning_enabled":
                    parameters.setEnablePruning(value.equals("true"));
                    break;
            }
        }
        return parameters;
    }
}
