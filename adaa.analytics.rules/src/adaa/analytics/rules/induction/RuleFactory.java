package adaa.analytics.rules.induction;

import adaa.analytics.rules.logic.ClassificationRule;
import adaa.analytics.rules.logic.ClassificationRuleSet;
import adaa.analytics.rules.logic.CompoundCondition;
import adaa.analytics.rules.logic.ElementaryCondition;
import adaa.analytics.rules.logic.Knowledge;
import adaa.analytics.rules.logic.RegressionRule;
import adaa.analytics.rules.logic.RegressionRuleSet;
import adaa.analytics.rules.logic.Rule;
import adaa.analytics.rules.logic.RuleSetBase;
import adaa.analytics.rules.logic.SurvivalRule;
import adaa.analytics.rules.logic.SurvivalRuleSet;

import com.rapidminer.example.ExampleSet;

public class RuleFactory {
	
	public static final int CLASSIFICATION = 1; 
	public static final int REGRESSION = 2;
	public static final int SURVIVAL = 3;
	
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
		}
		
		return null;
	}
	
}
