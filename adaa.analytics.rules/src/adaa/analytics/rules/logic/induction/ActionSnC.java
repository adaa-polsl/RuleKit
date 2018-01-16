package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;

import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.ClassificationRuleSet;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SingletonSet;

public class ActionSnC extends AbstractSeparateAndConquer {
	
	protected ActionFinder finder;

	
	
	public ActionSnC(ActionFinder finder_, InductionParameters params) {
		super(params);
		finder = finder_;
		this.factory = new RuleFactory(RuleFactory.ACTION, false, null);
	}

	@Override
	public RuleSetBase run(ExampleSet dataset) {
		
		Logger.log("ActionSnC.run", Level.FINE);
		
		ActionRuleSet ruleset = (ActionRuleSet) factory.create(dataset);
		Attribute label = dataset.getAttributes().getLabel();
		NominalMapping mapping = label.getMapping();
		
		//iteration 1: we have only two classes. Assume that first class is the positive one
		if (mapping.size() > 2) {
			Logger.log("Only two classes supported for action rules", Level.ALL);
			return null;
		}
		
		double weightedP = 0, weightedN = 0;
		Set<Integer> uncoveredPositives = new HashSet<Integer>(), uncovered = new HashSet<Integer>();
		
		//iterate over all examples
		for (int i = 0; i < dataset.size(); i++) {
			
			Example ex = dataset.getExample(i);
			double w = dataset.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			
			if ((int)ex.getLabel() == 0) {
				weightedP += w;
				uncoveredPositives.add(i);
			} else {
				weightedN += w;
			}
			uncovered.add(i);
		}
		
		boolean carryOn = uncoveredPositives.size() > 0;
		
		while(carryOn) {
			
			Rule rule = new ActionRule(new CompoundCondition(), 
					new Action(
							label.getName(),
							new SingletonSet((double)0, mapping.getValues()),
							new SingletonSet((double)1, mapping.getValues())
							)
					);
			
			Logger.log(rule.toString(), Level.FINER);
			
			rule.setWeighted_P(weightedP);
			rule.setWeighted_N(weightedN);
			
			carryOn = (finder.grow(rule, dataset, uncoveredPositives) > 0);
			double uncovered_p = weightedP;
			
			if (carryOn) {
				if (params.isPruningEnabled()) {
					Logger.log("Before prunning:" + rule.toString() + "\n" , Level.FINE);
					finder.prune(rule, dataset);
				}
				Logger.log("Candidate rule" + ruleset.getRules().size() +  ":" + rule.toString() + "\n", Level.INFO);
				Covering covered = rule.covers(dataset, uncovered);
				
				// remove covered examples
				int previouslyUncovered = uncoveredPositives.size();
				uncoveredPositives.removeAll(covered.positives);
				uncovered.removeAll(covered.positives);
				uncovered.removeAll(covered.negatives);
				
				uncovered_p = 0;
				for (int id : uncoveredPositives) {
					Example e = dataset.getExample(id);
					uncovered_p += dataset.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
				}
				
				// stop if number of positive examples remaining is less than threshold
				if (uncovered_p <= params.getMaximumUncoveredFraction() * weightedP) {
					carryOn = false;
				}
				
				// stop and ignore last rule if no new positive examples covered
				if (uncoveredPositives.size() == previouslyUncovered) {
					carryOn = false; 
				} else {
					ruleset.addRule(rule);
				}
			}
		}
		
		return ruleset;
	}

}
