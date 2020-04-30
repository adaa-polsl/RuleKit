package utils;

import adaa.analytics.rules.logic.representation.Rule;
import utils.reports.RuleStringFactory;

import java.util.HashMap;
import java.util.List;

public class RuleSetComparator {

    private static String sanitizeRuleString(String ruleString) {
        return ruleString.replaceAll("(\\[)|(\\])|(\\()|(\\))", "");
    }

    public static boolean assertRulesAreEqual(List<Rule> expected, List<Rule> actual) {
        HashMap<String, Integer> rulesOccurrencesCount = new HashMap<>();

        if (expected.size() != actual.size()) {
            throw new AssertionError(
                    "Rulesets have different number of rules, actual: " +
                            actual.size() + " expected:" +
                            expected.size());
        }
        for (Rule expectedRule : expected) {
            String ruleString = sanitizeRuleString(RuleStringFactory.make(expectedRule));
            rulesOccurrencesCount.put(ruleString, 0);
        }
        for (Rule actualRule : actual) {
            String actualRuleString = sanitizeRuleString(RuleStringFactory.make(actualRule));
            if (rulesOccurrencesCount.containsKey(actualRuleString)) {
                rulesOccurrencesCount.replace(actualRuleString, rulesOccurrencesCount.get(actualRuleString) + 1);
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
