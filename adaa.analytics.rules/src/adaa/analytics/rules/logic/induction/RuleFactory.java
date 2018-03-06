package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.ClassificationRule;
import adaa.analytics.rules.logic.representation.ClassificationRuleSet;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Knowledge;
import adaa.analytics.rules.logic.representation.RegressionRule;
import adaa.analytics.rules.logic.representation.RegressionRuleSet;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.logic.representation.SurvivalRuleSet;

import com.rapidminer.example.ExampleSet;

public class RuleFactory {
	
	public static final int CLASSIFICATION = 1; 
	public static final int REGRESSION = 2;
	public static final int SURVIVAL = 3;
	public static final int ACTION = 4;
	
	protected int type;
	protected boolean isVoting = true;
	protected Knowledge knowledge;
	
	public int getType() { return this.type; }
	
	public RuleFactory(int type, boolean isVoting) {
		this.type = type;
	}
	
	public RuleFactory(int type, boolean isVoting, Knowledge knowledge) {
		this.type = type;
		this.knowledge = knowledge;
	}
	
	public Rule create(CompoundCondition premise, ElementaryCondition consequence) {
		switch (type) {
		case CLASSIFICATION:
			return new ClassificationRule(premise, consequence);
		case REGRESSION:
			return new RegressionRule(premise, consequence);
		case SURVIVAL:
			return new SurvivalRule(premise, consequence);
		case ACTION:
			return new ActionRule();
		}
		
		return null;
	}
	
	public RuleSetBase create(ExampleSet set) {
		switch (type) {
		case CLASSIFICATION:
			return new ClassificationRuleSet(set, isVoting, knowledge);
		case REGRESSION:
			return new RegressionRuleSet(set, isVoting, knowledge);
		case SURVIVAL:
			return new SurvivalRuleSet(set, isVoting, knowledge);
		case ACTION:
			return new ActionRuleSet(set, isVoting, knowledge);
		}
		
		return null;
	}
	
}