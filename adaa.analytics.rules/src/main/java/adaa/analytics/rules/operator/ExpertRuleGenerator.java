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
package adaa.analytics.rules.operator;

import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.ConditionBase.Type;
import adaa.analytics.rules.operator.gui.ExpertWizard.ExpertWizardCreator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.parameter.*;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.ParameterCondition;

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
 *
 */
public class ExpertRuleGenerator extends RuleGenerator {
	
	/**
	 * Boolean indicating whether user's knowledge should be used.
	 */
	public static final String PARAMETER_USE_EXPERT = "use_expert";
	
	
	public static final String PARAMETER_EXPERT_CONFIG = "expert_config";
	
	/**
	 * Set of initial (expert's) rules.
	 */
	public static final String PARAMETER_EXPERT_RULES = "expert_rules";
	
	/**
	 * Multiset of preferred conditions (used also for specifying preferred attributes by using special value "Any").
	 */
	public static final String PARAMETER_EXPERT_PREFERRED_CONDITIONS = "expert_preferred_conditions";
	
	/**
	 * Set of forbidden conditions (used also for specifying forbidden attributes by using special value Any).
	 */
	public static final String PARAMETER_EXPERT_FORBIDDEN_CONDITIONS = "expert_forbidden_conditions";
	
	/**
	 * Auxiliary parameter for specifying sets/multisets of expert rules and preferred/forbidden conditions/attributes.
	 */
	public static final String PARAMETER_EXPORT_KEY = "export_key";

	/**
	 * Auxiliary parameter for specifying sets/multisets of expert rules and preferred/forbidden conditions/attributes.
	 */
	public static final String PARAMETER_EXPORT_VALUE = "export_value";
	
	/**
	 * Boolean indicating whether initial rules should be extended with a use of preferred conditions and attributes.
	 */
	public static final String PARAMETER_EXTEND_USING_PREFERRED = "extend_using_preferred";
	
	/**
	 * Boolean indicating whether initial rules should be extended with a use of automatic conditions.
	 */
	public static final String PARAMETER_EXTEND_USING_AUTOMATIC = "extend_using_automatic";
	
	/**
	 * Boolean indicating whether new rules should be induced with a use of preferred conditions and attributes.
	 */
	public static final String PARAMETER_INDUCE_USING_PREFERRED = "induce_using_preferred";
	
	/**
	 * Boolean indicating whether new rules should be induced with a use of automatic conditions.
	 */
	public static final String PARAMETER_INDUCE_USING_AUTOMATIC = "induce_using_automatic";
	
	/**
	 * Boolean indicating whether automatic induction should be performed for classes for which 
	 * no user's knowledge has been defined (classification only).
	 */
	public static final String PARAMETER_CONSIDER_OTHER_CLASSES = "consider_other_classes";
	
	/**
	 * Maximum number of preferred conditions per rule.
	 */
	public static final String PARAMETER_PREFERRED_CONDITIONS_PER_RULE = "preferred_conditions_per_rule";
	
	/**
	 * Maximum number of preferred attributes per rule.
	 */
	public static final String PARAMETER_PREFERRED_ATTRIBUTES_PER_RULE = "preferred_attributes_per_rule";
	
	/**
	 * Invokes base class constructor.
	 * @param description Operator description.
	 */
	public ExpertRuleGenerator(OperatorDescription description) {
		super(description);	
	}

	@Override
    public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		ParameterType type = new ParameterTypeBoolean(PARAMETER_USE_EXPERT, getParameterDescription(PARAMETER_USE_EXPERT), false);
		types.add(type);
		ParameterCondition enableExpertCondition = new BooleanParameterCondition(this, PARAMETER_USE_EXPERT, true, true);
		
		// add expert parameters only when enabled
		type = new ParameterTypeConfiguration(ExpertWizardCreator.class, this);
		type.setExpert(false);
		type.setKey(PARAMETER_EXPERT_CONFIG);
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		
		type = new ParameterTypeList(PARAMETER_EXPERT_RULES, getParameterDescription(PARAMETER_EXPERT_RULES),
				   new ParameterTypeString(PARAMETER_EXPORT_KEY, getParameterDescription(PARAMETER_EXPORT_KEY)),
				   new ParameterTypeString(PARAMETER_EXPORT_VALUE, getParameterDescription(PARAMETER_EXPORT_VALUE)));
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		 
		type = new ParameterTypeList(PARAMETER_EXPERT_PREFERRED_CONDITIONS, getParameterDescription(PARAMETER_EXPERT_PREFERRED_CONDITIONS),
				   new ParameterTypeString(PARAMETER_EXPORT_KEY, getParameterDescription(PARAMETER_EXPORT_KEY)),
				   new ParameterTypeString(PARAMETER_EXPORT_VALUE, getParameterDescription(PARAMETER_EXPORT_VALUE)));
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		 
		type = new ParameterTypeList(PARAMETER_EXPERT_FORBIDDEN_CONDITIONS, getParameterDescription(PARAMETER_EXPERT_FORBIDDEN_CONDITIONS),
				   new ParameterTypeString(PARAMETER_EXPORT_KEY, getParameterDescription(PARAMETER_EXPORT_KEY)),
				   new ParameterTypeString(PARAMETER_EXPORT_VALUE, getParameterDescription(PARAMETER_EXPORT_VALUE)));
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		
		type = new ParameterTypeBoolean(PARAMETER_EXTEND_USING_PREFERRED, getParameterDescription(PARAMETER_EXTEND_USING_PREFERRED), false);
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		
		type = new ParameterTypeBoolean(PARAMETER_EXTEND_USING_AUTOMATIC, getParameterDescription(PARAMETER_EXTEND_USING_AUTOMATIC), false);
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		
		type = new ParameterTypeBoolean(PARAMETER_INDUCE_USING_PREFERRED, getParameterDescription(PARAMETER_INDUCE_USING_PREFERRED), false);
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		
		type = new ParameterTypeBoolean(PARAMETER_INDUCE_USING_AUTOMATIC, getParameterDescription(PARAMETER_INDUCE_USING_AUTOMATIC), false);
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		
		type = new ParameterTypeBoolean(PARAMETER_CONSIDER_OTHER_CLASSES, getParameterDescription(PARAMETER_CONSIDER_OTHER_CLASSES), false);
		type.registerDependencyCondition(enableExpertCondition);
		type.registerDependencyCondition(classificationMetaCondition);
		types.add(type);
		
		type = new ParameterTypeInt(PARAMETER_PREFERRED_CONDITIONS_PER_RULE, getParameterDescription(PARAMETER_PREFERRED_CONDITIONS_PER_RULE),
				1, Integer.MAX_VALUE, Integer.MAX_VALUE);
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);
		
		type = new ParameterTypeInt(PARAMETER_PREFERRED_ATTRIBUTES_PER_RULE, getParameterDescription(PARAMETER_PREFERRED_ATTRIBUTES_PER_RULE),
				1, Integer.MAX_VALUE, Integer.MAX_VALUE);
		type.registerDependencyCondition(enableExpertCondition);
		types.add(type);

		return types;
	}
	
	@Override
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		Model model = null;
		
		// do not use expert knowledge in when option is not set
		if (!getParameterAsBoolean(PARAMETER_USE_EXPERT)) {
			return super.learn(exampleSet);
		}
		
		try {		
			MultiSet<Rule> rules = new MultiSet<Rule>();
			MultiSet<Rule> preferredConditions = new MultiSet<Rule>();
			MultiSet<Rule> forbiddenConditions = new MultiSet<Rule>();
			
			ExampleSetMetaData setMeta = new ExampleSetMetaData(exampleSet);
			
			Logger.log("Loading initial rules:\n", Level.FINER);
			List<String[]> ruleList = getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_RULES);
			for (String[] e: ruleList) {

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
			ruleList = getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_PREFERRED_CONDITIONS);
			for (String[] e: ruleList) {
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
			ruleList = getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS);
			for (String[] e: ruleList) {
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
			
			knowledge.setExtendUsingPreferred(getParameterAsBoolean(PARAMETER_EXTEND_USING_PREFERRED));
			knowledge.setExtendUsingAutomatic(getParameterAsBoolean(PARAMETER_EXTEND_USING_AUTOMATIC));
			knowledge.setInduceUsingPreferred(getParameterAsBoolean(PARAMETER_INDUCE_USING_PREFERRED));
			knowledge.setInduceUsingAutomatic(getParameterAsBoolean(PARAMETER_INDUCE_USING_AUTOMATIC));
			knowledge.setConsiderOtherClasses(getParameterAsBoolean(PARAMETER_CONSIDER_OTHER_CLASSES));
			knowledge.setPreferredConditionsPerRule(getParameterAsInt(PARAMETER_PREFERRED_CONDITIONS_PER_RULE));
			knowledge.setPreferredAttributesPerRule(getParameterAsInt(PARAMETER_PREFERRED_ATTRIBUTES_PER_RULE));
			
			InductionParameters params = new InductionParameters();
			params.setInductionMeasure(createMeasure(MeasureDestination.INDUCTION, new ClassificationMeasure(ClassificationMeasure.Correlation)));
			params.setPruningMeasure(createMeasure(MeasureDestination.PRUNING, params.getInductionMeasure())); 
			params.setVotingMeasure(createMeasure(MeasureDestination.VOTING, params.getInductionMeasure()));
				
			params.setMinimumCovered(getParameterAsDouble(PARAMETER_MIN_RULE_COVERED));
			params.setEnablePruning(getParameterAsBoolean(PARAMETER_ENABLE_PRUNING));
			params.setIgnoreMissing(getParameterAsBoolean(PARAMETER_IGNORE_MISSING));
			params.setMaxGrowingConditions(getParameterAsDouble(PARAMETER_MAX_GROWING));
			
			
			AbstractFinder finder = null;
			AbstractSeparateAndConquer snc = null;
			
			if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
				// survival problem
				params.setInductionMeasure(new LogRank());
				params.setPruningMeasure(new LogRank());
				params.setVotingMeasure(new LogRank());
				finder = new SurvivalLogRankExpertFinder(params);
				snc = new SurvivalLogRankExpertSnC((SurvivalLogRankExpertFinder)finder, params, knowledge);
			} else if (exampleSet.getAttributes().getLabel().isNominal()) {
				// expert mode in classification problems
				finder = new ClassificationExpertFinder(params, knowledge);
				snc = new ClassificationExpertSnC((ClassificationExpertFinder)finder, params, knowledge);
			} else {
				// expert mode in regression problems
				finder = new RegressionExpertFinder(params);
				snc = new RegressionExpertSnC((RegressionExpertFinder)finder, params, knowledge);
			}

			model = snc.run(exampleSet);
			RuleSetBase rs = (RuleSetBase)model;
			performances = recalculatePerformance(rs);
			finder.close();
			
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return model;
	}
	
	/**
	 * Auxiliary function that fixes mappings of nominal attributes in given rules so they
	 * agree with an example set. 
	 * 
	 * @param rules Rules to be fixed.
	 * @param set Reference example set.
	 */
	protected void fixMappings(Iterable<Rule> rules, ExampleSet set) {

		for (Rule r : rules) {
			List<ConditionBase> toCheck = new ArrayList<ConditionBase>(); // list of elementary conditions to check
			toCheck.addAll(r.getPremise().getSubconditions());
			toCheck.add(r.getConsequence());

			for (ConditionBase c: toCheck) {
				ElementaryCondition ec = (c instanceof ElementaryCondition) ? (ElementaryCondition)c : null;
				if (ec != null) {
					Attribute a = set.getAttributes().get(ec.getAttribute());
					if (a.isNominal()) {
						if (ec.getValueSet() instanceof SingletonSet) {
							SingletonSet ss = (SingletonSet) ec.getValueSet();
							String valName = ss.getMapping().get((int)ss.getValue());
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
