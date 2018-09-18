package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RegressionRuleSet;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;

/**
 * Separate'n'conquer algorithm for generating regression rule sets.
 * @author Adam
 *
 */
public class RegressionSnC extends AbstractSeparateAndConquer {

	protected final RegressionFinder finder;
	
	public RegressionSnC(final RegressionFinder finder, final InductionParameters params) {
		super(params);
		this.finder = finder;
		factory = new RuleFactory(RuleFactory.REGRESSION, true);
	}

	@Override
	public RuleSetBase run(final ExampleSet dataset) {
		
		Logger.log("RegressionSnC.run()\n", Level.FINE);
		double beginTime;
		beginTime = System.nanoTime();
		
		RuleSetBase ruleset = factory.create(dataset);
		Attribute label = dataset.getAttributes().getLabel();
		SortedExampleSet ses = new SortedExampleSet(dataset, label, SortedExampleSet.INCREASING);
		ses.recalculateAttributeStatistics(ses.getAttributes().getLabel());
			
		if (factory.getType() == RuleFactory.REGRESSION) {
			double median = ses.getExample(ses.size() / 2).getLabel();
			RegressionRuleSet tmp = (RegressionRuleSet)ruleset;
			tmp.setDefaultValue(median);
		}
		
		Set<Integer> uncovered = new HashSet<Integer>();
		double weighted_PN = 0;
		// at the beginning rule set does not cover any examples
		for (int id = 0; id < ses.size(); ++id) {
			uncovered.add(id);
			Example ex = ses.getExample(id);
			double w = ses.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			weighted_PN += w;
		}
		
		boolean carryOn = true; 
		double uncovered_pn = weighted_PN;
		
		while (carryOn) {
			Logger.log("Uncovered positive weight: " + uncovered_pn +  "/" + weighted_PN + "\n", Level.FINE);
			
			Rule rule = factory.create(
				new CompoundCondition(),
				new ElementaryCondition(label.getName(), new SingletonSet(Double.NaN, null)));
			
			double t = System.nanoTime();
			carryOn = (finder.grow(rule, ses, uncovered) > 0);
			ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
			
			if (carryOn) {
				if (params.isPruningEnabled()) {
					Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
					t = System.nanoTime();
					finder.prune(rule, ses);
					ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
				}
				Logger.log("Candidate rule: " + rule.toString() + "\n", Level.INFO);
				
				Covering covered = rule.covers(ses, uncovered);
				
				// remove covered examples
				int previouslyUncovered = uncovered.size();
				uncovered.removeAll(covered.positives);
				uncovered.removeAll(covered.negatives);
				
				uncovered_pn = 0;
				for (int id : uncovered) {
					Example e = dataset.getExample(id);
					uncovered_pn += dataset.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
				}
				
				// stop if number of examples remaining is less than threshold
				if (uncovered_pn <= params.getMaximumUncoveredFraction() * weighted_PN) {
					carryOn = false; 
				}
				
				// stop and ignore last rule if no new examples covered
				if (uncovered.size() == previouslyUncovered) {
					carryOn = false; 
				} else {
					ruleset.addRule(rule);
				}
			}
		}
		
		ruleset.setTotalTime((System.nanoTime() - beginTime) / 1e9);
		return ruleset;
	}
}
