package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;

import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.BackwardActionRuleAdapter;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SingletonSet;

public class BackwardActionSnC extends ActionSnC {

	public BackwardActionSnC(ActionFinder finder_, ActionInductionParameters params) {
		
		super(finder_, params);
		params.reverseTransitions();
		// TODO Auto-generated constructor stub
	}

	@Override
	public RuleSetBase run(ExampleSet dataset) {
		
		Logger.log("ActionSnC.run", Level.FINE);
		
		ActionRuleSet ruleset = (ActionRuleSet) factory.create(dataset);
		Attribute label = dataset.getAttributes().getLabel();
		NominalMapping mapping = label.getMapping();
		
		List<ClassPair> pairs = ((ActionInductionParameters)params).generateClassPairs(mapping);
		
		if (pairs.isEmpty()) {
			Logger.log("No valid transitions provided for action generation", Level.ALL);
			return null;
		}
		
		//iteration 2: generate for all (demanded) transitions.
		
		for (ClassPair pair : pairs) {
			
			ExampleSet filtered = dataset;

			double weightedP = 0, weightedN = 0;
			Set<Integer> uncoveredPositives = new HashSet<Integer>(), uncovered = new HashSet<Integer>();
			Set<Integer> uncoveredNegatives = new HashSet<Integer>();
			//iterate over all examples
			for (int i = 0; i < filtered.size(); i++) {
				
				Example ex = filtered.getExample(i);
				double w = filtered.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
				
				if ((int)ex.getLabel() == pair.getSourceId()) {
					weightedP += w;
					uncoveredPositives.add(i);
				} else {
					weightedN += w;
					uncoveredNegatives.add(i);
				}
				uncovered.add(i);
			}
			finder.setUncoveredNegatives(uncoveredNegatives);
			boolean carryOn = uncoveredPositives.size() > 0;
			
			while(carryOn) {
				
				Rule rule = new ActionRule(new CompoundCondition(), 
						new Action(
								label.getName(),
								new SingletonSet((double)pair.getSourceId(), mapping.getValues()),
								new SingletonSet((double)pair.getTargetId(), mapping.getValues())
								)
						);
				
				Logger.log(rule.toString(), Level.FINER);
				
				rule.setWeighted_P(weightedP);
				rule.setWeighted_N(weightedN);
				
				carryOn = (finder.grow(rule, filtered, uncoveredPositives) > 0);
				double uncovered_p = weightedP;
				
				if (carryOn) {
				
					Logger.log("Candidate rule" + ruleset.getRules().size() +  ":" + rule.toString() + "\n", Level.INFO);
					
					ActionRule aRule = (ActionRule)rule;
					
					Covering covered = aRule.covers(filtered, uncovered);
					
					// remove covered examples
					int previouslyUncovered = uncoveredPositives.size();
					uncoveredPositives.removeAll(covered.positives);
					uncovered.removeAll(covered.positives);
					uncovered.removeAll(covered.negatives);
					
					uncovered_p = 0;
					for (int id : uncoveredPositives) {
						Example e = filtered.getExample(id);
						uncovered_p += filtered.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
					}
					
					// stop if number of positive examples remaining is less than threshold
					if (uncovered_p <= params.getMaximumUncoveredFraction() * weightedP) {
						carryOn = false;
					}
					
					// stop and ignore last rule if no new positive examples covered
					if (uncoveredPositives.size() == previouslyUncovered) {
						carryOn = false; 
					} else {
						ruleset.addRule(aRule);
					}
				}
			}
			
		}
		
		
		
		ActionRuleSet reversed = (ActionRuleSet) factory.create(dataset);
		// ruleset pointing from b->a
		// now lets reverse the arrow
		// check performance
		// return normal actions
		ruleset
			.getRules()
			.stream()
			.map(x -> (new BackwardActionRuleAdapter((ActionRule)x)).get())
			.forEach(x -> {
					Covering cov = x.covers(dataset);
					x.setCoveringInformation((ActionCovering)cov);
					reversed.addRule(x);
				});
		
		
		
		if (params.isPruningEnabled()) {
			ActionRuleSet pruned = (ActionRuleSet) factory.create(dataset);
			for (Rule r : reversed.getRules()) {
				ActionRule rule = (ActionRule)r;
				Logger.log("Before prunning:" + rule.toString() + "\n" , Level.FINE);
				finder.prune(rule, dataset);
				Logger.log("After pruning" + rule.toString() + "\n", Level.FINE);
				pruned.addRule(rule);
			}
			return pruned;
		}
		
		
		
		
		return reversed;
	}
	
public RuleSetBase run2(ExampleSet dataset) {
		
		Logger.log("ActionSnC.run", Level.FINE);
		
		ActionRuleSet ruleset = (ActionRuleSet) factory.create(dataset);
		Attribute label = dataset.getAttributes().getLabel();
		NominalMapping mapping = label.getMapping();
		
		List<ClassPair> pairs = ((ActionInductionParameters)params).generateClassPairs(mapping);
		
		if (pairs.isEmpty()) {
			Logger.log("No valid transitions provided for action generation", Level.ALL);
			return null;
		}
		
		//iteration 2: generate for all (demanded) transitions.
		
		for (ClassPair pair : pairs) {
			
			ExampleSet filtered = dataset;

			double weightedP = 0, weightedN = 0;
			Set<Integer> uncoveredPositives = new HashSet<Integer>(), uncovered = new HashSet<Integer>();
			Set<Integer> uncoveredNegatives = new HashSet<Integer>();
			//iterate over all examples
			for (int i = 0; i < filtered.size(); i++) {
				
				Example ex = filtered.getExample(i);
				double w = filtered.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
				
				if ((int)ex.getLabel() == pair.getSourceId()) {
					weightedP += w;
					uncoveredPositives.add(i);
				} else {
					weightedN += w;
					uncoveredNegatives.add(i);
				}
				uncovered.add(i);
			}
			finder.setUncoveredNegatives(uncoveredNegatives);
			boolean carryOn = uncoveredPositives.size() > 0;
			
			while(carryOn) {
				
				Rule rule = new ActionRule(new CompoundCondition(), 
						new Action(
								label.getName(),
								new SingletonSet((double)pair.getSourceId(), mapping.getValues()),
								new SingletonSet((double)pair.getTargetId(), mapping.getValues())
								)
						);
				
				Logger.log(rule.toString(), Level.FINER);
				
				rule.setWeighted_P(weightedP);
				rule.setWeighted_N(weightedN);
				
				carryOn = (finder.grow(rule, filtered, uncoveredPositives) > 0);
				double uncovered_p = weightedP;
				
				if (carryOn) {
					
					rule = (new BackwardActionRuleAdapter((ActionRule)rule)).get();
				
					ActionRule aRule = (ActionRule)rule;
					Covering cov = aRule.covers(filtered);
					aRule.setCoveringInformation(cov);
					
					if (params.isPruningEnabled()) {
						Logger.log("Before prunning:" + rule.toString() + "\n" , Level.FINE);
						finder.prune(rule, dataset);
					}
					Logger.log("Candidate rule" + ruleset.getRules().size() +  ":" + rule.toString() + "\n", Level.INFO);
	
					
					Covering covered = aRule.covers(filtered, uncovered);
					Rule rightRule = aRule.getRightRule();
					Covering rightCovered = rightRule.covers(filtered, uncovered);
					// remove covered examples
					int previouslyUncovered = uncoveredPositives.size();
					// negatives - because the rule was reverted!
					uncoveredPositives.removeAll(rightCovered.positives);
					
					uncovered.removeAll(covered.positives);
					uncovered.removeAll(covered.negatives);
					
					uncovered_p = 0;
					for (int id : uncoveredPositives) {
						Example e = filtered.getExample(id);
						uncovered_p += filtered.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
					}
					
					// stop if number of positive examples remaining is less than threshold
					if (uncovered_p <= params.getMaximumUncoveredFraction() * weightedP) {
						carryOn = false;
					}
					
					// stop and ignore last rule if no new positive examples covered
					if (uncoveredPositives.size() == previouslyUncovered) {
						carryOn = false; 
					} else {
						ruleset.addRule(aRule);
					}
				}
			}
			
		}
				
		
		
		return ruleset;
	}
}
