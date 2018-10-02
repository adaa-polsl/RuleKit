package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;

import java.util.Set;
import java.util.logging.Level;

public class SurvivalLogRankSnC extends RegressionSnC {

	public SurvivalLogRankSnC(RegressionFinder finder, InductionParameters params) {
		super(finder, params);
		this.factory = new RuleFactory(RuleFactory.SURVIVAL, true);
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
