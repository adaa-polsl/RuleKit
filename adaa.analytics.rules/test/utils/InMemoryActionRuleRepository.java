package utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import adaa.analytics.rules.rm.example.IExampleSet;

import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.induction.RuleFactory;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.model.ActionRuleSet;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.SingletonSet;

public class InMemoryActionRuleRepository {

	protected IExampleSet set;
	
	public InMemoryActionRuleRepository(IExampleSet set) {
		this.set = set;
	}
	
	public ActionRuleSet getActionRulest() {
		
		
		RuleFactory factory = new RuleFactory(RuleFactory.ACTION, false, new InductionParameters(), null);
		ActionRuleSet actions = (ActionRuleSet) factory.create(set);
		
		List<String> m1 = Collections.unmodifiableList(Arrays.asList("1", "2", "3"));
		List<String> m2 = Collections.unmodifiableList(Arrays.asList("a", "b"));
		Action c1 = new Action("class", new SingletonSet(0.0, m1), new SingletonSet(1.0, m1));
		
		CompoundCondition cc1 = new CompoundCondition();
		cc1.addSubcondition(new Action("nominal", new SingletonSet(0.0, m2), new SingletonSet(1.0, m2) ));
		Action common = new Action("numerical1", new Interval(12.0, 15.0, true, true), new Interval(-1.0, 1.0, true, true));
		cc1.addSubcondition(common);
		
		ActionRule a1 = new ActionRule(cc1, c1);
		
		Action c2 = new Action("class", new SingletonSet(0.0, m1), new SingletonSet(2.0, m1));
		
		CompoundCondition cc2 = new CompoundCondition();
		cc2.addSubcondition(common);
		
		ActionRule a2 = new ActionRule(cc2, c2);
		
		
		actions.addRule(a1);
		actions.addRule(a2);
		
		return actions;
		
	}

}
