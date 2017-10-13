package adaa.analytics.rules.logic;

import java.util.ArrayList;
import java.util.List;

import adaa.analytics.rules.quality.IQualityMeasure;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.SimplePredictionModel;


public abstract class RuleSetBase extends SimplePredictionModel {
	
	private static final long serialVersionUID = -7112032011785315168L;

	protected ExampleSet trainingSet;
	
	protected List<Rule> rules = new ArrayList<Rule>();

	protected boolean isVoting;
	
	protected Knowledge knowledge = null;
	
	protected List<IQualityMeasure> qualityMeasures = new ArrayList<IQualityMeasure>();
	
	public List<Rule> getRules() { return rules; }
	public void addRule(Rule v) { rules.add(v); }
	
	public boolean getIsVoting() { return isVoting; }
	public void setIsVoting(boolean v) { isVoting = v; }
	
	public int calculateConditionsCount() {
		int cnt = 0;
		for (Rule r : rules) {
			cnt += r.getPremise().getSubconditions().size();
		}
		return cnt;
	}
	
	public int calculateInducedCondtionsCount() {
		int cnt = 0;
		for (Rule r : rules) {
			cnt += r.getInducedConditionsCount();
		}
		return cnt;
	}
	
	public double calculateAvgRuleCoverage() {
		double cov = 0;
		for (Rule r : rules) {
			cov += (r.weighted_p + r.weighted_n) / (r.weighted_P + r.weighted_N);
		}
		return cov / rules.size();
	}
	
	public double calculateAvgRuleQuality() {
		double q = 0.0;
		for (Rule rule : rules) {
			q += rule.getWeight();
		}
		return q / rules.size();
	}
	
	public RuleSetBase(ExampleSet exampleSet, boolean isVoting, Knowledge knowledge) {
		super(exampleSet);
		this.trainingSet = exampleSet;
		this.isVoting = isVoting;
		this.knowledge = knowledge;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (knowledge != null) {
			sb.append("Knowledge:\n");
			sb.append(knowledge.toString());
			sb.append("\n");
		}
		
		sb.append("Rules:\n");
		int i = 1;
		for (Rule r : rules) {
			sb.append("r" + i + ": " + r.toString() + " " + r.printStats() + "\n");
			++i;
		}
		
	//	sb.append("General information:\n");
	//	sb.append("voting: " + (getIsVoting() ? "true" : "false") + "\n");
		
		return sb.toString();
	}

}
