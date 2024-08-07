package utils.reports;

import adaa.analytics.rules.logic.representation.rule.Rule;

public class RuleStringFactory {

    public static final String make(Rule rule) {
        String ruleString = rule.toString();
        ruleString = ruleString.replaceAll("(\\[[^\\]]*\\]$)|(\\([^\\)]*\\)$)", "");
        return ruleString;
    }
}
