package adaa.analytics.rules.logic.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adaa.analytics.rules.logic.quality.IQualityMeasure;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.SimplePredictionModel;


public abstract class RuleSetBase extends SimplePredictionModel {
	
	public class Significance {
		public double p = 0;
		public double fraction = 0;
	};
	
	
	private static final long serialVersionUID = -7112032011785315168L;

	protected ExampleSet trainingSet;
	
	protected List<Rule> rules = new ArrayList<Rule>();

	protected boolean isVoting;
	
	protected Knowledge knowledge = null;
	
	protected List<IQualityMeasure> qualityMeasures = new ArrayList<IQualityMeasure>();
	
	protected double totalTime;
	
	protected double growingTime;
	
	protected double pruningTime;
	
	public double getTotalTime() { return totalTime; }
	public void setTotalTime(double v) { totalTime = v; }
	
	public double getGrowingTime() { return growingTime; }
	public void setGrowingTime(double v) { growingTime = v; }
	
	public double getPruningTime() { return pruningTime; }
	public void setPruningTime(double v) { pruningTime = v; }
	
	public List<Rule> getRules() { return rules; }
	public void addRule(Rule v) { rules.add(v); }
	
	public boolean getIsVoting() { return isVoting; }
	public void setIsVoting(boolean v) { isVoting = v; }
	
	public double calculateConditionsCount() {
		double cnt = 0;
		for (Rule r : rules) {
			cnt += r.getPremise().getSubconditions().size();
		}
		return cnt / rules.size();
	}
	
	public double calculateInducedCondtionsCount() {
		double cnt = 0;
		for (Rule r : rules) {
			cnt += r.getInducedConditionsCount();
		}
		return cnt / rules.size();
	}
	
	public double calculateAvgRuleCoverage() {
		double cov = 0;
		for (Rule r : rules) {
			cov += (r.weighted_p + r.weighted_n) / (r.weighted_P + r.weighted_N);
		}
		return cov / rules.size();
	}
	
	public double calculateAvgRulePrecision() {
		double prec = 0;
		for (Rule r : rules) {
			prec += r.weighted_p / (r.weighted_p + r.weighted_n);
		}
		return prec / rules.size();
	}
	
	public double calculateAvgRuleQuality() {
		double q = 0.0;
		for (Rule rule : rules) {
			q += rule.getWeight();
		}
		return q / rules.size();
	}
	
	public Significance calculateSignificance(double alpha) {
		Significance out = new Significance();
		
		for (Rule rule : rules) {
			double p = rule.getPValue();
			out.p += p; 
			
			if (p < alpha) {
				out.fraction += 1.0;
			}
		}
		
		out.p /= rules.size();
		out.fraction /= rules.size();
		return out;
	}
	
	public Significance calculateSignificanceFDR(double alpha) {
		Significance out = new Significance();
		
		int N = rules.size();
		double[] pvals = new double[N];
		int k = 0;
		for (Rule rule : rules) {
			pvals[k] = rule.getPValue();
			++k;
		}
		Arrays.sort(pvals);
		
		k = 1;
		boolean ok = true;
		for (double p : pvals) { // from smallest to largest p-value
			double adj_p = p * N / k;
			out.p += adj_p;
			if (adj_p < alpha && ok) {
				out.fraction += 1.0;
			} else {
				ok = false;
			}
			++k;
		}
		
		out.p /= rules.size();
		out.fraction /= rules.size();
		return out;
	}
	
	public Significance calculateSignificanceFWER(double alpha) {
		Significance out = new Significance();
		
		int N = rules.size();
		double[] pvals = new double[N];
		int k = 0;
		for (Rule rule : rules) {
			pvals[k] = rule.getPValue();
			++k;
		}
		Arrays.sort(pvals);
		
		k = 1;
		boolean ok = true;
		for (double p : pvals) { // from smallest to largest p-value
			double adj_p = p * (N + 1 - k); 
			out.p += adj_p;
			if (adj_p < alpha && ok) {
				out.fraction += 1.0;
			} else {
				ok = false;
			}
			++k;
		}
		
		out.p /= rules.size();
		out.fraction /= rules.size();
		return out;
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
		int rid = 1;
		for (Rule r : rules) {
			sb.append("r" + rid + ": " + r.toString() + " " + r.printStats() + "\n");
			++rid;
		}
		
		sb.append("\nBest rules covering examples from training set (1-based):\n");
		for (int eid = 0; eid < trainingSet.size(); ++eid){
			Example ex = trainingSet.getExample(eid);
			int bestRuleId = -1;
			Double bestWeight = Double.NEGATIVE_INFINITY;
			
			rid = 1;
			for (Rule r: rules) {
				if (r.getPremise().evaluate(ex) && r.getWeight() > bestWeight) {
					bestRuleId = rid;
					bestWeight = r.getWeight();
				}
				++rid;
			}
			sb.append((bestRuleId > 0 ? bestRuleId : "-") + ",");
		}
		sb.append("\n");
		
	//	sb.append("General information:\n");
	//	sb.append("voting: " + (getIsVoting() ? "true" : "false") + "\n");
		
		return sb.toString();
	}

}
