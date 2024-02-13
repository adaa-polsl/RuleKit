package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.model.ActionRuleSet;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.table.INominalMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class ActionSnC extends AbstractSeparateAndConquer {
	
	protected ActionFinder finder;	
	protected ActionRuleSet unprunedRules;
	
	public ActionSnC(ActionFinder finder_, ActionInductionParameters params) {
		super(new ActionInductionParameters(params));
		finder = finder_;
		this.factory = new RuleFactory(RuleFactory.ACTION, false, params, null);
	}
	
	public ActionRuleSet getUnprunedRules() {
		return unprunedRules;
	}

	public List<String> getStableAttributes() {
	    return ((ActionInductionParameters)finder.params).getStableAttributes();
    }
	
	@Override
	public RuleSetBase run(IExampleSet dataset) {
		long currTime = System.currentTimeMillis();
		Logger.log("ActionSnC.run entered at " + currTime, Level.FINE);


		ActionRuleSet ruleset = (ActionRuleSet) factory.create(dataset);
		unprunedRules = (ActionRuleSet)factory.create(dataset);
		IAttribute label = dataset.getAttributes().getLabel();
		INominalMapping mapping = label.getMapping();
		
		List<ClassPair> pairs = ((ActionInductionParameters)params).generateClassPairs(mapping);
		
		if (pairs.isEmpty()) {
			Logger.log("No valid transitions provided for action generation", Level.ALL);
			return null;
		}

		finder.preprocess(dataset);
		//iteration 2: generate for all (demanded) transitions.
		
		for (ClassPair pair : pairs) {

			double weightedP = 0, weightedN = 0;
			Set<Integer> positives = new IntegerBitSet(dataset.size());
			Set<Integer> negatives = new IntegerBitSet(dataset.size());
			Set<Integer> uncoveredPositives = new IntegerBitSet(dataset.size());
			Set<Integer> uncovered = new HashSet<>();
			//iterate over all examples
			for (int i = 0; i < dataset.size(); i++) {
				
				Example ex = dataset.getExample(i);
				double w = dataset.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
				
				if ((int)ex.getLabel() == pair.getSourceId()) {
					weightedP += w;
					positives.add(i);
				} else {
					weightedN += w;
					if ((int)ex.getLabel() == pair.getTargetId()) {
						negatives.add(i);
					}
				}
			}
			finder.setUncoveredNegatives(negatives);
			uncoveredPositives.addAll(positives);
			uncovered.addAll(positives);
			uncovered.addAll(negatives);

			boolean carryOn = uncoveredPositives.size() > 0;
			
			while(carryOn) {
				
				Rule rule = new ActionRule(new CompoundCondition(),
						new Action(
								label.getName(),
								new SingletonSet(pair.getSourceId(), mapping.getValues()),
								new SingletonSet(pair.getTargetId(), mapping.getValues())
								)
						);
				
				Logger.log(rule.toString(), Level.FINER);
				
				rule.setWeighted_P(weightedP);
				rule.setWeighted_N(weightedN);

				// rule covers everything at the beginning
				rule.setCoveredPositives(new IntegerBitSet(dataset.size()));
				rule.setCoveredNegatives(new IntegerBitSet(dataset.size()));
				rule.getCoveredPositives().addAll(positives);
				rule.getCoveredNegatives().addAll(negatives);
				
				carryOn = (finder.grow(rule, dataset, uncoveredPositives) > 0);
				double uncovered_p;
				
				if (carryOn) {
					Rule unpruned = new ActionRule(rule);
					if (params.isPruningEnabled()) {
						Logger.log("Before prunning:" + rule.toString() + "\n" , Level.FINE);
						finder.prune(rule, dataset, uncoveredPositives);
					}
					Logger.log("Candidate rule" + ruleset.getRules().size() +  ":" + rule.toString() + "\n", Level.INFO);
					
					ActionRule aRule = (ActionRule)rule;

					// remove covered examples
					int previouslyUncovered = uncoveredPositives.size();
					uncoveredPositives.removeAll(rule.getCoveredPositives()); //..are you sure rule.getCoveredPos returns the correct value ?
					uncovered.removeAll(rule.getCoveredPositives());
					uncovered.removeAll(rule.getCoveredNegatives());
					
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
						unpruned.setCoveringInformation(unpruned.covers(dataset));

						unprunedRules.addRule(unpruned);
					}

					//report to operator command proxy
					this.operatorCommandProxy.onNewRule(rule);
					this.operatorCommandProxy.onProgressChange(dataset.size(), uncovered.size());
				}
				if (this.operatorCommandProxy.isRequestStop()) {
					carryOn = false;
				}
			}
			
		}
		Logger.log("ActionSnC.run exited after " + (System.currentTimeMillis() - currTime), Level.FINE);
		return ruleset;
	}

}
