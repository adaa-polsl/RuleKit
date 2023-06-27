package utils.testcases;

import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;

import java.util.Arrays;
import java.util.HashMap;

public class InductionParametersFactory {

    public static InductionParameters make(HashMap<String, Object> paramMap) {
        InductionParameters parameters = new InductionParameters();
        for (String key : paramMap.keySet()) {
            switch (key) {
                case "min_rule_covered":
                    parameters.setMinimumCovered(Double.parseDouble((String) paramMap.get(key)));
                    break;
                case "max_rule_count":
                    parameters.setMaxRuleCount(Integer.parseInt((String) paramMap.get(key)));
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
                case "pruning_measure":
                    int id = Arrays.binarySearch(ClassificationMeasure.NAMES, paramMap.get(key));
                    if (id > 0) {
                        parameters.setInductionMeasure(new ClassificationMeasure(id));
                    } else {
                        throw new RuntimeException("Unknown name of classification induction measure: " + paramMap.get(key));
                    }
                    break;
                case "induction_measure":
                    id = Arrays.binarySearch(ClassificationMeasure.NAMES, paramMap.get(key));
                    if (id > 0) {
                        parameters.setPruningMeasure(new ClassificationMeasure(id));
                    } else {
                        throw new RuntimeException("Unknown name of classification pruning measure: " + paramMap.get(key));
                    }
                    break;
            }
        }
        return parameters;
    }
}
