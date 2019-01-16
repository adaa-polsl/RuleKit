package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.actions.ActionMetaTable.AnalysisResult;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;

public class MetaRecommendation extends Recommendation {

	protected ActionMetaTable table;
	protected ActionSnC engine;
	int fromClassId;
	int toClassId;
	protected ExampleSet trainSet;
	
	public MetaRecommendation(ActionSnC snc, int fromClass, int toClass) {
		engine = snc;
		fromClassId = fromClass;
		toClassId = toClass;
	}

	
	public void train(ExampleSet set) {
		ActionRuleSet actions = (ActionRuleSet) engine.run(set);
		trainSet = (ExampleSet) set.clone();
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, set);
		dist.calculateActionDistribution();
		table = new ActionMetaTable(dist);
	}

	
	public ActionRuleSet test(ExampleSet set) {
		List<AnalysisResult> results = new ArrayList<AnalysisResult>(set.size());
		
		for(int i = 0; i < set.size(); i++) {
			Example example = set.getExample(i);
			
			AnalysisResult res = table.analyze(example, fromClassId, toClassId, trainSet);
			results.add(res);
		}
		
		ActionRuleSet rules = new ActionRuleSet(set, false, null);
		for (int j = 0; j < results.size(); j++) {
			
			AnalysisResult res = results.get(j);
			ActionRule rule = res.getActionRule();
			Covering cov = rule.covers(trainSet);
			rule.setCoveringInformation(cov);
			
			rules.addRule(rule);	
			System.out.print(j+1 + " ");
			System.out.println(printExampleNicely(res.example));
			System.out.println(rule + rule.printStats());
			System.out.println("Left Target class likeliness: " + res.primeMetaExample.getCountOfRulesPointingToClass(toClassId));
			System.out.println("Left Source class likeliness: " + res.primeMetaExample.getCountOfRulesPointingToClass(fromClassId));
			System.out.println("Right Target class likeliness: " + res.contraMetaExample.getCountOfRulesPointingToClass(toClassId));
			System.out.println("Right Source class likeliness: " + res.contraMetaExample.getCountOfRulesPointingToClass(fromClassId));
		}
		return rules;
	}

}