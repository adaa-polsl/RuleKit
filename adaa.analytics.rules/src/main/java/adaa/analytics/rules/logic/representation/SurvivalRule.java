package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.Covering;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import java.util.Set;

public class SurvivalRule extends Rule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2143653515226214179L;
	
	public static final String SURVIVAL_TIME_ROLE = "survival_time";
	
	protected KaplanMeierEstimator estimator;
	
	public KaplanMeierEstimator getEstimator() { return estimator; }
	public void setEstimator(KaplanMeierEstimator v) { estimator = v; }
	
	public SurvivalRule(CompoundCondition premise, ElementaryCondition consequence) {
		super(premise, consequence);
	}
	
	public SurvivalRule(Rule r, KaplanMeierEstimator estimator) {
		super(r);
		this.estimator = estimator;
	}
	
	@Override
	public Covering covers(ExampleSet set, Set<Integer> ids) {
		Covering covered = new Covering();
		
		for (int id : ids) {
			Example ex = set.getExample(id);
			double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			covered.weighted_P += w;
			if (this.getPremise().evaluate(ex)) {
				covered.positives.add(id);
				covered.weighted_p += w;
			} 
		}
		return covered;
	}
	
	@Override
	public Covering covers(ExampleSet set) {
		Covering covered = new Covering();
		
		for (int id = 0; id < set.size(); ++id) {
			Example ex = set.getExample(id);
			double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			covered.weighted_P += w;
			if (this.getPremise().evaluate(ex)) {
				covered.positives.add(id);
				covered.weighted_p += w;
			}
		}
		return covered;
	}
	
}
