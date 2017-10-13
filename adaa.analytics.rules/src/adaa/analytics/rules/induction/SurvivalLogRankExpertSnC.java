package adaa.analytics.rules.induction;

import java.util.Set;
import java.util.logging.Level;

import adaa.analytics.rules.logic.KaplanMeierEstimator;
import adaa.analytics.rules.logic.Knowledge;
import adaa.analytics.rules.logic.Logger;
import adaa.analytics.rules.logic.Rule;
import adaa.analytics.rules.logic.RuleSetBase;
import adaa.analytics.rules.logic.SurvivalRule;
import adaa.analytics.rules.logic.SurvivalRuleSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;

public class SurvivalLogRankExpertSnC extends RegressionExpertSnC {

	public SurvivalLogRankExpertSnC(RegressionFinder finder,
			InductionParameters params, Knowledge knowledge) {
		super(finder, params, knowledge);
		this.factory = new RuleFactory(RuleFactory.SURVIVAL, true, knowledge);
	}
	
	@Override
	public RuleSetBase run(final ExampleSet dataset) {
		
		Attribute label = dataset.getAttributes().getLabel();
		SortedExampleSet ses = new SortedExampleSet(dataset, label, SortedExampleSet.INCREASING);
		
		SurvivalRuleSet survSet = (SurvivalRuleSet)super.run(dataset);
		
		for (Rule r : survSet.getRules()) {
			SurvivalRule sr = (SurvivalRule)r;
			Covering cov = r.covers(ses);
			Set<Integer> covered = cov.positives;
			KaplanMeierEstimator kme = new KaplanMeierEstimator(ses, covered);
			
			sr.setEstimator(kme);
		}
		
		Logger.log(survSet.toString(), Level.FINE);
		
		return survSet;
	}

}
