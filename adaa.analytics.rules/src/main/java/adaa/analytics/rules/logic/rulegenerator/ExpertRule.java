/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.rulegenerator;

import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.ConditionBase.Type;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An ExpertRuleGenerator is an operator that extends RuleGenerator by providing user
 * with the possibility to introduce users's knowledge to the rule induction process.
 *
 * @author Adam Gudys
 */
public class ExpertRule {

    private RuleGeneratorParams ruleGeneratorParams;

    private OperatorCommandProxy operatorCommandProxy;

    public ExpertRule(RuleGeneratorParams ruleGeneratorParams, OperatorCommandProxy operatorCommandProxy) {
        this.ruleGeneratorParams = ruleGeneratorParams;
        this.operatorCommandProxy = operatorCommandProxy;
    }


    public RuleSetBase learnWithExpert(ExampleSet exampleSet) {

        MultiSet<Rule> rules = new MultiSet<Rule>();
        MultiSet<Rule> preferredConditions = new MultiSet<Rule>();
        MultiSet<Rule> forbiddenConditions = new MultiSet<Rule>();

        ExampleSetMetaData setMeta = new ExampleSetMetaData(exampleSet);

        Logger.log("Loading initial rules:\n", Level.FINER);
        List<String[]> ruleList = ruleGeneratorParams.getParameterList(RuleGeneratorParams.PARAMETER_EXPERT_RULES);
        for (String[] e : ruleList) {

            Rule r = RuleParser.parseRule(e[1], setMeta);
            if (r != null) {
                // set all subconditions in rules as forced no matter how they were specified
                for (ConditionBase cnd : r.getPremise().getSubconditions()) {
                    cnd.setType(Type.FORCED);
                }
                rules.add(r);
                Logger.log(r.toString() + "\n", Level.FINER);
            }
        }

        Logger.log("Loading preferred conditions/attributes:\n", Level.FINER);
        Pattern pattern = Pattern.compile("(?<number>(\\d+)|(inf)):\\s*(?<rule>.*)");
        ruleList = ruleGeneratorParams.getParameterList(RuleGeneratorParams.PARAMETER_EXPERT_PREFERRED_CONDITIONS);
        for (String[] e : ruleList) {
            Matcher matcher = pattern.matcher(e[1]);
            matcher.find();
            String count = matcher.group("number");
            String ruleDesc = matcher.group("rule");
            Rule r = RuleParser.parseRule(ruleDesc, setMeta);
            if (r != null) {
                r.getPremise().setType(ConditionBase.Type.PREFERRED); // set entire compound condition as preferred and all subconditions as normal
                for (ConditionBase cnd : r.getPremise().getSubconditions()) {
                    cnd.setType(Type.NORMAL);
                }
                int parsedCount = (count.equals("inf")) ? Integer.MAX_VALUE : Integer.parseInt(count);
                preferredConditions.add(r, parsedCount);
                Logger.log(r.toString() + "\n", Level.FINER);
            }
        }

        Logger.log("Loading forbidden conditions/attributes:\n", Level.FINER);
        ruleList = ruleGeneratorParams.getParameterList(RuleGeneratorParams.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS);
        for (String[] e : ruleList) {
            Rule r = RuleParser.parseRule(e[1], setMeta);
            for (ConditionBase cnd : r.getPremise().getSubconditions()) {
                cnd.setType(Type.NORMAL);
            }
            forbiddenConditions.add(r);
            Logger.log(r.toString() + "\n", Level.FINER);
        }

        fixMappings(rules, exampleSet);
        fixMappings(preferredConditions, exampleSet);
        fixMappings(forbiddenConditions, exampleSet);

        Knowledge knowledge = new Knowledge(exampleSet, rules, preferredConditions, forbiddenConditions);

        knowledge.setExtendUsingPreferred(ruleGeneratorParams.getParameterAsBoolean(RuleGeneratorParams.PARAMETER_EXTEND_USING_PREFERRED));
        knowledge.setExtendUsingAutomatic(ruleGeneratorParams.getParameterAsBoolean(RuleGeneratorParams.PARAMETER_EXTEND_USING_AUTOMATIC));
        knowledge.setInduceUsingPreferred(ruleGeneratorParams.getParameterAsBoolean(RuleGeneratorParams.PARAMETER_INDUCE_USING_PREFERRED));
        knowledge.setInduceUsingAutomatic(ruleGeneratorParams.getParameterAsBoolean(RuleGeneratorParams.PARAMETER_INDUCE_USING_AUTOMATIC));
        knowledge.setConsiderOtherClasses(ruleGeneratorParams.getParameterAsBoolean(RuleGeneratorParams.PARAMETER_CONSIDER_OTHER_CLASSES));
        knowledge.setPreferredConditionsPerRule(ruleGeneratorParams.getParameterAsInt(RuleGeneratorParams.PARAMETER_PREFERRED_CONDITIONS_PER_RULE));
        knowledge.setPreferredAttributesPerRule(ruleGeneratorParams.getParameterAsInt(RuleGeneratorParams.PARAMETER_PREFERRED_ATTRIBUTES_PER_RULE));

        InductionParameters params = ruleGeneratorParams.fillParameters();

        AbstractFinder finder = null;
        AbstractSeparateAndConquer snc = null;

        if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
            // survival problem
            params.setInductionMeasure(new LogRank());
            params.setPruningMeasure(new LogRank());
            params.setVotingMeasure(new LogRank());
            finder = new SurvivalLogRankExpertFinder(params);
            snc = new SurvivalLogRankExpertSnC((SurvivalLogRankExpertFinder) finder, params, knowledge);
        } else if (exampleSet.getAttributes().getLabel().isNominal()) {
            // expert mode in classification problems
            finder = new ClassificationExpertFinder(params, knowledge);
            snc = new ClassificationExpertSnC((ClassificationExpertFinder) finder, params, knowledge);
        } else {
            // expert mode in regression problems
            finder = new RegressionExpertFinder(params);
            snc = new RegressionExpertSnC((RegressionExpertFinder) finder, params, knowledge);
        }

        snc.setOperatorCommandProxy(operatorCommandProxy);
        double beginTime = System.nanoTime();
        RuleSetBase rs = snc.run(exampleSet);
        rs.setTotalTime((System.nanoTime() - beginTime) / 1e9);

        finder.close();

        return rs;
    }


    /**
     * Auxiliary function that fixes mappings of nominal attributes in given rules so they
     * agree with an example set.
     *
     * @param rules Rules to be fixed.
     * @param set   Reference example set.
     */
    protected void fixMappings(Iterable<Rule> rules, ExampleSet set) {

        for (Rule r : rules) {
            List<ConditionBase> toCheck = new ArrayList<ConditionBase>(); // list of elementary conditions to check
            toCheck.addAll(r.getPremise().getSubconditions());
            toCheck.add(r.getConsequence());

            for (ConditionBase c : toCheck) {
                ElementaryCondition ec = (c instanceof ElementaryCondition) ? (ElementaryCondition) c : null;
                if (ec != null) {
                    Attribute a = set.getAttributes().get(ec.getAttribute());
                    if (a.isNominal()) {
                        if (ec.getValueSet() instanceof SingletonSet) {
                            SingletonSet ss = (SingletonSet) ec.getValueSet();
                            String valName = ss.getMapping().get((int) ss.getValue());
                            int newValue = a.getMapping().getIndex(valName);
                            ss.setValue(newValue);
                            ss.setMapping(a.getMapping().getValues());
                        }
                    }
                }
            }
        }
    }
}
