package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.SerializationUtils;

import adaa.analytics.rules.logic.induction.AbstractFinder.QualityAndPValue;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Knowledge;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.RegressionRuleSet;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;

public class RegressionExpertSnC extends RegressionSnC {

	protected Knowledge knowledge;
	
	public RegressionExpertSnC(RegressionFinder finder,
			InductionParameters params,
			Knowledge knowledge) {
		super(finder, params);
		factory = new RuleFactory(RuleFactory.REGRESSION, true, knowledge);
		this.knowledge = (Knowledge)SerializationUtils.clone(knowledge);
		RegressionExpertFinder erf = (RegressionExpertFinder)finder;
		erf.setKnowledge(this.knowledge);
	}
	
	@Override
	public RuleSetBase run(final ExampleSet dataset) {
		
		Logger.log("RegressionExpertSnC.run()\n", Level.FINE);
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
		Logger.log("Processing expert rules...\n", Level.INFO);
		
		// add expert rules to the ruleset and try to refine them
		for (Rule r : knowledge.getRules()) {
			Logger.log("Uncovered positive weight: " + uncovered_pn +  "/" + weighted_PN + "\n", Level.FINE);
			Rule rule = (Rule) SerializationUtils.clone(r);
			
			RegressionExpertFinder erf = (RegressionExpertFinder)finder;
			
			erf.adjust(rule, dataset, uncovered);
			
			Covering cov = rule.covers(ses);
			
			QualityAndPValue qp = finder.calculateQualityAndPValue(dataset, cov, params.getVotingMeasure());
			rule.setWeight(qp.quality);
			rule.setPValue(qp.pvalue);
		
			rule.setCoveringInformation(cov);
			Logger.log("Expert rule: " + rule.toString() + "\n", Level.FINE);
			double t = System.nanoTime();
			finder.grow(rule, ses, uncovered);
			ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
						
			if (params.isPruningEnabled()) {
				Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
				t = System.nanoTime();
				finder.prune(rule, ses);
				ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
			}
			Logger.log("Candidate rule: " + rule.toString() + "\n", Level.INFO);
			
			
			ruleset.addRule(rule);
			cov = rule.covers(ses);
			rule.setCoveringInformation(cov);
			
			// remove examples covered by the rule and update statistics
			uncovered.removeAll(cov.positives);
			uncovered.removeAll(cov.negatives);
			uncovered_pn = 0;
			for (int id : uncovered) {
				Example e = ses.getExample(id);
				uncovered_pn += ses.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
			}
		}
		
		// try to generate new rules
		Logger.log("Processing other rules...\n", Level.INFO);
		carryOn = uncovered.size() > 0; 
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
				
				Covering covered = rule.covers(ses);
				
				// remove covered examples
				int previouslyUncovered = uncovered.size();
				uncovered.removeAll(covered.positives);
				uncovered.removeAll(covered.negatives);
				
				uncovered_pn = 0;
				for (int id : uncovered) {
					Example e = ses.getExample(id);
					uncovered_pn += ses.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
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
