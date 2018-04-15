package adaa.analytics.rules.logic.induction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionCreationException;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.NominalMapping;

import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SingletonSet;
import java.util.Optional;
import java.util.stream.*;
public class ActionSnC extends AbstractSeparateAndConquer {
	
	protected ActionFinder finder;	
	
	public ActionSnC(ActionFinder finder_, ActionInductionParameters params) {
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
		
		List<ClassPair> pairs = ((ActionInductionParameters)params).generateClassPairs(mapping);
		
		if (pairs.isEmpty()) {
			Logger.log("No valid transitions provided for action generation", Level.ALL);
			return null;
		}
		
		//iteration 2: generate for all possible (demanded) transitions.
		
		for (ClassPair pair : pairs) {
			
			ConditionedExampleSet filtered = null;
			try {
			
				filtered = new ConditionedExampleSet(dataset, 
					ConditionedExampleSet.createCondition(
							ConditionedExampleSet.KNOWN_CONDITION_NAMES[ConditionedExampleSet.CONDITION_ATTRIBUTE_VALUE_FILTER],
							dataset, 
							String.format("%1$s = %2$s || %1$s = %3$s", label.getName(), 
									pair.getSourceLabel(),
									pair.getTargetLabel())
									)
							
					);
				
			} catch (ConditionCreationException ex) {
				
				Logger.log(String.format("Couldn't create subdataset for source class id %1$s and target class id %2$s ", pair.getSourceLabel(), pair.getTargetLabel()), 
						Level.ALL);
				return null;
			}
			
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
					if (params.isPruningEnabled()) {
						Logger.log("Before prunning:" + rule.toString() + "\n" , Level.FINE);
						finder.prune(rule, dataset);
					}
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
		
		return ruleset;
	}

}
