package utils.reports;

import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;

import java.util.List;

public class TestReport {
    private String fileName;
    private List<Rule> rules;

    public TestReport(String fileName) {
        this.fileName = fileName;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(RuleSetBase ruleSet) {
        rules = ruleSet.getRules();
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getFileName() {
        return fileName;
    }
}
