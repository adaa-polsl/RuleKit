package adaa.analytics.rules.induction;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import adaa.analytics.rules.logic.ClassificationRuleSet;
import adaa.analytics.rules.logic.CompoundCondition;
import adaa.analytics.rules.logic.ElementaryCondition;
import adaa.analytics.rules.logic.Logger;
import adaa.analytics.rules.logic.Rule;
import adaa.analytics.rules.logic.RuleSetBase;
import adaa.analytics.rules.logic.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;

/**
 * Separate'n'conquer algorithm for generating classification rule sets.
 * @author Adam
 *
 */
public class ClassificationSnC extends AbstractSeparateAndConquer {
	
	protected ClassificationFinder finder;

	public ClassificationSnC(ClassificationFinder finder, InductionParameters params) {
		super(params);
		this.finder = finder;
		this.factory = new RuleFactory(RuleFactory.CLASSIFICATION, true, null);
	}
	
	/**
	 * Generates classification rule set on the basis of training set.
	 * @param dataset Training data set.
	 * @return Rule set.
	 */
	public RuleSetBase run(ExampleSet dataset) {
		Logger.log("ClassificationSnC.run()\n", Level.FINE);
	
		ClassificationRuleSet ruleset = (ClassificationRuleSet) factory.create(dataset);
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

			boolean carryOn = uncoveredPositives.size() > 0; 
			double uncovered_p = weighted_P;
			
			while (carryOn) {
			
				Logger.log("Class " + (classId + 1) + "/" + mapping.size() + " uncovered positive weight:" + 
						uncovered_p +  "/" + weighted_P + "\n", Level.FINE);
				Rule rule = factory.create(
						new CompoundCondition(),
						new ElementaryCondition(label.getName(), new SingletonSet((double)classId, mapping.getValues())));
				
				rule.setWeighted_P(weighted_P);
				rule.setWeighted_N(weighted_N);
				
				carryOn = (finder.grow(rule, dataset, uncoveredPositives) > 0);
			
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
					if (uncovered_p <= params.getMaximumUncoveredFraction() * weighted_P) {
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
