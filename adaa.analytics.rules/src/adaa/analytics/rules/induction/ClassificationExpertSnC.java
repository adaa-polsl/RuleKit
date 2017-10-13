package adaa.analytics.rules.induction;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.SerializationUtils;

import adaa.analytics.rules.gui.Serializer;
import adaa.analytics.rules.logic.ClassificationRule;
import adaa.analytics.rules.logic.ClassificationRuleSet;
import adaa.analytics.rules.logic.CompoundCondition;
import adaa.analytics.rules.logic.ElementaryCondition;
import adaa.analytics.rules.logic.Knowledge;
import adaa.analytics.rules.logic.Logger;
import adaa.analytics.rules.logic.Rule;
import adaa.analytics.rules.logic.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;

public class ClassificationExpertSnC extends ClassificationSnC {

	protected Knowledge knowledge;
	
	public ClassificationExpertSnC(ClassificationFinder finder, InductionParameters params, Knowledge knowledge) {
		super(finder, params);
		factory = new RuleFactory(RuleFactory.CLASSIFICATION, true, knowledge);
		this.knowledge = (Knowledge)SerializationUtils.clone(knowledge);
	}
	
	@Override
	public ClassificationRuleSet run(ExampleSet dataset)
	{
		Logger.log("ClassificationExpertSnC.run()\n", Level.FINE);
		
		ClassificationRuleSet ruleset = (ClassificationRuleSet)factory.create(dataset);
		Attribute label = dataset.getAttributes().getLabel();
		NominalMapping mapping = label.getMapping();
		
		double defaultClassWeight = 0;
		
		// iterate over all classes
		for (int classId = 0; classId < mapping.size(); ++classId) {
			
			Set<Integer> uncoveredPositives = new HashSet<Integer>();
			Set<Integer> uncovered = new HashSet<Integer>();
			double weighted_P = 0;
			double weighted_N = 0;
			
			// at the beginning rule set does not cover any examples
			for (int id = 0; id < dataset.size(); ++id) {
				Example e = dataset.getExample(id);
				double w = dataset.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
				
				if ((double)e.getLabel() == classId) {
					weighted_P += w;
					uncoveredPositives.add(id);
				} else {
					weighted_N += w;
				}
				uncovered.add(id);
			}
			
			// change default class if neccessary
			if (weighted_P > defaultClassWeight) {
				defaultClassWeight = weighted_P;
				ruleset.setDefaultClass(classId);
			}
			
			Knowledge classKnowledge = null;
			
			// if no expert rules and conditions specified
			if (knowledge.getRules(classId).size() == 0 
					&& knowledge.getPreferredConditions(classId).size() == 0
					&& knowledge.getPreferredAttributes(classId).size() == 0
					&& knowledge.getForbiddenAttributes(classId).size() == 0
					&& knowledge.getForbiddenAttributes(classId).size() == 0) {
				
				if (knowledge.isConsiderOtherClasses()) {
					// if flag specified allow induction for non-expert rules
					classKnowledge = (Knowledge) SerializationUtils.clone(knowledge);
					classKnowledge.setInduceUsingAutomatic(true);
				} else {
					continue;
				}
				
			} else {
				classKnowledge = knowledge;
				
			}
			
			// add expert rules to the ruleset and try to refine them
			Logger.log("Processing expert rules...\n", Level.INFO);
			for (Rule r : knowledge.getRules(classId)) {
				Rule rule = (Rule) SerializationUtils.clone(r);
				Covering cov = rule.covers(dataset, uncovered);
				double q = finder.calculateQuality(dataset, cov, params.getInductionMeasure());
				rule.setCoveringInformation(cov);
				rule.setWeight(q);
				Logger.log("Expert rule: " + rule.toString() + "\n", Level.FINE);
				

				ClassificationExpertFinder erf = (ClassificationExpertFinder)finder;
				erf.setKnowledge(classKnowledge);
				finder.grow(rule, dataset, uncoveredPositives);
				
				if (params.isPruningEnabled()) {
					Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
					finder.prune(rule, dataset);
				}
				Logger.log("Candidate rule:" + rule.toString() + "\n", Level.INFO);
				
				ruleset.addRule(rule);
				
				cov = rule.covers(dataset, uncovered);
				
				// remove covered examples
				uncoveredPositives.removeAll(cov.positives);
				uncovered.removeAll(cov.positives);
				uncovered.removeAll(cov.negatives);
			}
			
			// try to generate new rules
			Logger.log("Processing other rules...\n", Level.INFO);
			boolean carryOn = uncoveredPositives.size() > 0; 
			while (carryOn) {
				Rule rule = new ClassificationRule(
						new CompoundCondition(),
						new ElementaryCondition(label.getName(), new SingletonSet((double)classId, mapping.getValues())));
				
				rule.setWeighted_P(weighted_P);
				rule.setWeighted_N(weighted_N);
				
				ClassificationExpertFinder erf = (ClassificationExpertFinder)finder;
				erf.setKnowledge(classKnowledge);
				carryOn = (finder.grow(rule, dataset, uncoveredPositives) > 0);
			
				if (carryOn) {
					if (params.isPruningEnabled()) {
						Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
						finder.prune(rule, dataset);
					}
					Logger.log("Candidate rule:" + rule.toString() + "\n", Level.INFO);
					
					Covering covered = rule.covers(dataset, uncovered);
					
					// remove covered examples
					int previouslyUncovered = uncoveredPositives.size();
					uncoveredPositives.removeAll(covered.positives);
					uncovered.removeAll(covered.positives);
					uncovered.removeAll(covered.negatives);
					
					// stop if no positive examples remaining
					if (uncoveredPositives.size() == 0) {
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
		}
			
		return ruleset;
	}
	

}
