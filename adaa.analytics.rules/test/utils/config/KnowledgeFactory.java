package utils.config;

import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.rulegenerator.RuleGeneratorParams;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import utils.testcases.KnowledgeConfigurator;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KnowledgeFactory {

    private IExampleSet exampleSet;
    private ExampleSetMetaData exampleSetMetaData;

    public KnowledgeFactory(IExampleSet exampleSet) {
        this.exampleSet = exampleSet;
        this.exampleSetMetaData = new ExampleSetMetaData(exampleSet);
    }

    /**
     * Auxiliary function that fixes mappings of nominal attributes in given rules so they
     * agree with an example set.
     *
     * @param rules Rules to be fixed.
     * @param set Reference example set.
     */
    protected void fixMappings(Iterable<Rule> rules, IExampleSet set) {
        for (Rule rule : rules) {
            for (ConditionBase conditionBase: rule.getPremise().getSubconditions()) {
                ElementaryCondition elementaryCondition = (conditionBase instanceof ElementaryCondition) ? (ElementaryCondition)conditionBase : null;
                if (elementaryCondition != null) {
                    IAttribute attribute = set.getAttributes().get(elementaryCondition.getAttribute());
                    if (attribute.isNominal()) {
                        if (elementaryCondition.getValueSet() instanceof SingletonSet) {
                            SingletonSet singletonSet = (SingletonSet) elementaryCondition.getValueSet();
                            String valName = singletonSet.getMapping().get((int)singletonSet.getValue());
                            int newValue = attribute.getMapping().getIndex(valName);
                            singletonSet.setValue(newValue);
                            singletonSet.setMapping(attribute.getMapping().getValues());
                        }
                    }
                }
            }
        }
    }

    private MultiSet<Rule> makeExpertRules(List<String[]> elements) {
        MultiSet<Rule> rules = new MultiSet<>();

        for (String[] element : elements) {
            Rule rule = RuleParser.parseRule(element[1], exampleSetMetaData);
            if (rule != null) {
                // set all subconditions in rules as forced no matter how they were specified
                for (ConditionBase conditionBase : rule.getPremise().getSubconditions()) {
                    conditionBase.setType(ConditionBase.Type.FORCED);
                }
                rules.add(rule);
            }
        }
        return rules;
    }

    private MultiSet<Rule> makePreferredConditions(List<String[]> elements) {
        MultiSet<Rule> preferredConditions = new MultiSet<>();
        Pattern pattern = Pattern.compile("(?<number>(\\d+)|(inf)):\\s*(?<rule>.*)");

        for (String[] element : elements) {
            Matcher matcher = pattern.matcher(element[1]);
            matcher.find();
            String count = matcher.group("number");
            String ruleDesc = matcher.group("rule");
            Rule rule = RuleParser.parseRule(ruleDesc, exampleSetMetaData);
            if (rule != null) {
                rule.getPremise().setType(ConditionBase.Type.PREFERRED); // set entire compound condition as preferred and all subconditions as normal
                for (ConditionBase cnd : rule.getPremise().getSubconditions()) {
                    cnd.setType(ConditionBase.Type.NORMAL);
                }
                int parsedCount = (count.equals("inf")) ? Integer.MAX_VALUE : Integer.parseInt(count);
                preferredConditions.add(rule, parsedCount);
            }
        }
        return preferredConditions;
    }

    private MultiSet<Rule> makeForbiddenConditions(List<String[]> elements) {
        MultiSet<Rule> forbiddenConditions = new MultiSet<>();

        for (String[] element : elements) {
            Rule rule = RuleParser.parseRule(element[1], exampleSetMetaData);
            for (ConditionBase conditionBase : rule.getPremise().getSubconditions()) {
                conditionBase.setType(ConditionBase.Type.NORMAL);
            }
            forbiddenConditions.add(rule);
        }
        return forbiddenConditions;
    }

    public Knowledge make(HashMap<String, Object> parameters) {
        MultiSet<Rule> rules = new MultiSet<>();
        MultiSet<Rule> preferredConditions = new MultiSet<>();
        MultiSet<Rule> forbiddenConditions = new MultiSet<>();

        for (String key : parameters.keySet()) {
            switch (key) {
                case RuleGeneratorParams.PARAMETER_EXPERT_RULES:
                    rules = makeExpertRules((List<String[]>) parameters.get(key));
                    break;
                case RuleGeneratorParams.PARAMETER_EXPERT_PREFERRED_CONDITIONS:
                    preferredConditions = makePreferredConditions((List<String[]>) parameters.get(key));
                    break;
                case RuleGeneratorParams.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS:
                    forbiddenConditions = makeForbiddenConditions((List<String[]>) parameters.get(key));
                    break;
            }
        }

        fixMappings(rules, exampleSet);
        fixMappings(preferredConditions, exampleSet);
        fixMappings(forbiddenConditions, exampleSet);

        Knowledge knowledge = new Knowledge(exampleSet, rules, preferredConditions, forbiddenConditions);

        KnowledgeConfigurator.configure(knowledge, parameters);
        return knowledge;
    }
}
