package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import adaa.analytics.rules.logic.actions.recommendations.ClassificationRecommendationTask;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.Logger;

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
		Logger.log(actions.toString(), Level.FINE);
		trainSet = (ExampleSet) set.clone();
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, set);
		dist.calculateActionDistribution();
		table = new OptimizedActionMetaTable(dist, engine.getStableAttributes());
		Logger.log("Initial meta-table\r\n", Level.FINER);
	}

	
	public ActionRuleSet test(ExampleSet set) {
		List<MetaAnalysisResult> results = new ArrayList<MetaAnalysisResult>(set.size());
		ClassificationRecommendationTask task = new ClassificationRecommendationTask(false, true, new ClassificationMeasure(ClassificationMeasure.Correlation), fromClassId, toClassId);
		
		for(int i = 0; i < set.size(); i++) {
			Example example = set.getExample(i);
			
			List<MetaAnalysisResult> res = table.analyze(example, task);
			results.addAll(res);
		}
		
		ActionRuleSet rules = new ActionRuleSet(set, false, new InductionParameters(), null);
		for (int j = 0; j < results.size(); j++) {
			
			MetaAnalysisResult res = results.get(j);
			ActionRule rule = res.getActionRule();
			Covering cov = rule.covers(trainSet);
			rule.setCoveringInformation(cov);

			
			rules.addRule(rule);	
			System.out.print(j+1 + " ");
			System.out.println(printExampleNicely(res.example));
			System.out.println(rule + rule.printStats());
		/*	System.out.println("Left Target class likeliness: " + res.primeMetaExample.getCountOfRulesPointingToClass(toClassId));
			System.out.println("Left Source class likeliness: " + res.primeMetaExample.getCountOfRulesPointingToClass(fromClassId));
			System.out.println("Right Target class likeliness: " + res.contraMetaExample.getCountOfRulesPointingToClass(toClassId));
			System.out.println("Right Source class likeliness: " + res.contraMetaExample.getCountOfRulesPointingToClass(fromClassId));
		*/}
		return rules;
	}

}
