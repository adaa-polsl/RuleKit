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

public class ActionSnC extends AbstractSeparateAndConquer {
	
	protected ActionFinder finder;

	
	
	public ActionSnC(ActionFinder finder_, InductionParameters params) {
		super(params);
		finder = finder_;
		this.factory = new RuleFactory(RuleFactory.ACTION, false, null);
	}

	private int sourceId;
	private int targetId;
	
	public void setSourceClassId(int id) {
		sourceId = id;
	}
	
	public void setTargetClassId(int id) {
		targetId = id;
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
		Set<Integer> uncoveredNegatives = new HashSet<Integer>();
		//iterate over all examples
		for (int i = 0; i < dataset.size(); i++) {
			
			Example ex = dataset.getExample(i);
			double w = dataset.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			
			if ((int)ex.getLabel() == sourceId) {
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
							new SingletonSet((double)sourceId, mapping.getValues()),
							new SingletonSet((double)targetId, mapping.getValues())
							)
					);
			
			Logger.log(rule.toString(), Level.FINER);
			
			rule.setWeighted_P(weightedP);
			rule.setWeighted_N(weightedN);
			
			carryOn = (finder.grow(rule, dataset, uncoveredPositives) > 0);
			double uncovered_p = weightedP;
			
			
			//try merge conditions
			
			Map<Attribute, HashSet<Integer>> atrToCondition = new HashMap<Attribute, HashSet<Integer>>();
			
	//		rule.getPremise().getSubconditions().stream()
				
		/*	for (int i = 0; i < rule.getPremise().getSubconditions().size(); i++) {
				ConditionBase curr = rule.getPremise().getSubconditions().get(i);
				Attribute currAttribute = dataset.getAttributes().get(((ElementaryCondition)curr).getAttribute());
				
				if (!atrToCondition.containsKey(currAttribute)) {
					atrToCondition.put(currAttribute, new HashSet<Integer>());
				}
				
				atrToCondition.get(currAttribute).add(i);
			}
			
			for (Map.Entry<Attribute, HashSet<Integer>> pair : atrToCondition.entrySet()) {
				
				Attribute atr = pair.getKey();
				HashSet<Integer> ids = pair.getValue();
				
				List<ConditionBase> newConds = new LinkedList<ConditionBase>();
				List<ConditionBase> presentConds = ids.stream().map(x -> rule.getPremise().getSubconditions().get(x))
						.collect(Collectors.toList());
				
				boolean kontinue = true;
				
				while (kontinue) {
					
					ElementaryCondition curr = (ElementaryCondition)presentConds.get(0);
					presentConds.remove(curr);
					
					for (ConditionBase cand_ : presentConds) {
						ElementaryCondition cand = (ElementaryCondition)cand_;
						
						if (curr.getValueSet().intersects(cand.getValueSet())){
							
							
						}
						
					}
				}
			}
			*/
			
			if (carryOn) {
				if (params.isPruningEnabled()) {
					Logger.log("Before prunning:" + rule.toString() + "\n" , Level.FINE);
					finder.prune(rule, dataset);
				}
				Logger.log("Candidate rule" + ruleset.getRules().size() +  ":" + rule.toString() + "\n", Level.INFO);
				
				ActionRule aRule = (ActionRule)rule;
				
				Covering covered = aRule.covers(dataset, uncovered);
				
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
					ruleset.addRule(aRule);
				}
			}
		}
		
		return ruleset;
	}

}
