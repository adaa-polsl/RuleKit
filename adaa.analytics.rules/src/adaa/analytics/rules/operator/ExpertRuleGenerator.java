package adaa.analytics.rules.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adaa.analytics.rules.logic.induction.AbstractFinder;
import adaa.analytics.rules.logic.induction.AbstractSeparateAndConquer;
import adaa.analytics.rules.logic.induction.ClassificationExpertFinder;
import adaa.analytics.rules.logic.induction.ClassificationExpertSnC;
import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.induction.RegressionExpertFinder;
import adaa.analytics.rules.logic.induction.RegressionExpertSnC;
import adaa.analytics.rules.logic.induction.SurvivalLogRankExpertFinder;
import adaa.analytics.rules.logic.induction.SurvivalLogRankExpertSnC;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Knowledge;
import adaa.analytics.rules.logic.representation.MultiSet;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleParser;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SingletonSet;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.gui.ExpertWizard.ExpertWizardCreator;

import com.rapidminer.datatable.FilteredDataTable.ConditionCombination;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeConfiguration;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.ParameterCondition;

public class ExpertRuleGenerator extends RuleGenerator {
	// Wizard parameter
	public static final String PARAMETER_USE_EXPERT = "use_expert";
	
	public static final String PARAMETER_EXPERT_CONFIG = "expert_config";
	
	public static final String PARAMETER_EXPERT_RULES = "expert_rules";
	
	public static final String PARAMETER_EXPERT_PREFERRED_CONDITIONS = "expert_preferred_conditions";
	
	public static final String PARAMETER_EXPERT_FORBIDDEN_CONDITIONS = "expert_forbidden_conditions";
	
	public static final String PARAMETER_EXPORT_KEY = "export_key";

	public static final String PARAMETER_EXPORT_VALUE = "export_value";
	
	public static final String PARAMETER_EXTEND_USING_PREFERRED = "extend_using_preferred";
	
	public static final String PARAMETER_EXTEND_USING_AUTOMATIC = "extend_using_automatic";
	
	public static final String PARAMETER_INDUCE_USING_PREFERRED = "induce_using_preferred";
	
	public static final String PARAMETER_INDUCE_USING_AUTOMATIC = "induce_using_automatic";
	
	public static final String PARAMETER_CONSIDER_OTHER_CLASSES = "consider_other_classes";
	
	public static final String PARAMETER_PREFERRED_CONDITIONS_PER_RULE = "preferred_conditions_per_rule";
	
	public static final String PARAMETER_PREFERRED_ATTRIBUTES_PER_RULE = "preferred_attributes_per_rule";
	
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
			
			List<String[]> ruleList = getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_RULES);
			for (String[] e: ruleList) {
				Rule r = RuleParser.parseRule(e[1], setMeta);
				rules.add(r);
			}
		
			Pattern pattern = Pattern.compile("(?<number>\\d+):\\s*(?<rule>.*)");
			ruleList = getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_PREFERRED_CONDITIONS);
			for (String[] e: ruleList) {
				Matcher matcher = pattern.matcher(e[1]);
		    	matcher.find();
		    	String count = matcher.group("number");
		    	String ruleDesc = matcher.group("rule");
				Rule r = RuleParser.parseRule(ruleDesc, setMeta);
				if (r != null) {
					r.getPremise().setType(ConditionBase.Type.PREFERRED); // set it manually
					preferredConditions.add(r, Integer.parseInt(count));
				}
			}
			
			ruleList = getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS);
			for (String[] e: ruleList) {
				Rule r = RuleParser.parseRule(e[1], setMeta);
				forbiddenConditions.add(r);
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
			params.setInductionMeasure(createMeasure(PARAMETER_INDUCTION_MEASURE));
			params.setPruningMeasure(createMeasure(PARAMETER_INDUCTION_MEASURE)); 
			params.setMinimumCovered(getParameterAsDouble(PARAMETER_MIN_RULE_COVERED));
			params.setEnablePruning(getParameterAsBoolean(PARAMETER_PRUNING_ENABLED));
			params.setIgnoreMissing(getParameterAsBoolean(PARAMETER_IGNORE_MISSING));
			params.setMaxGrowingConditions(getParameterAsDouble(PARAMETER_MAX_GROWING));
			
			
			AbstractFinder finder = null;
			AbstractSeparateAndConquer snc = null;
			
			if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
				// survival problem
				params.setInductionMeasure(new LogRank());
				params.setPruningMeasure(new LogRank());
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
			
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return model;
	}
	
	protected void fixMappings(Iterable<Rule> rules, ExampleSet set) {
		for (Rule r : rules) {
			for (ConditionBase c: r.getPremise().getSubconditions()) {
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
