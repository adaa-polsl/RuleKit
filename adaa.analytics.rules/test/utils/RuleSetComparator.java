package utils;

import adaa.analytics.rules.logic.representation.Rule;

import java.util.HashMap;
import java.util.List;

public class RuleSetComparator {

    public static boolean assertRulesAreEqual(List<Rule> expected, List<Rule> actual) {
        HashMap<String, Integer> rulesOccurrencesCount = new HashMap<>();

        if (expected.size() != actual.size()) {
            throw new AssertionError(
                    "Rulesets have different number of rules, actual: " +
                            actual.size() + " expected:" +
                            expected.size());
        }
        for (Rule expectedRule : expected) {
            rulesOccurrencesCount.put(expectedRule.toString(), 0);
        }
        for (Rule actualRule : actual) {
            if (rulesOccurrencesCount.containsKey(actualRule.toString())) {
                rulesOccurrencesCount.replace(actualRule.toString(), rulesOccurrencesCount.get(actualRule.toString()) + 1);
            }
        }
        for (int wasRulePresent : rulesOccurrencesCount.values()) {
            if (wasRulePresent == 0) {
                throw new AssertionError("Ruleset are not equal, some rules are missing");
            } if (wasRulePresent > 1) {
                throw new AssertionError("Somes rules were duplicated");
            }
        }
        return true;
    }
}
