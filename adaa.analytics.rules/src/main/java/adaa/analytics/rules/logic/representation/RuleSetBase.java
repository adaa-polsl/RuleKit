/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adaa.analytics.rules.logic.induction.InductionParameters;
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
	
	protected InductionParameters params = null;
	
	protected Knowledge knowledge = null;
		
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
	
	public InductionParameters getParams() { return params; }
	
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
	
	
	public RuleSetBase(ExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
		super(exampleSet);
		this.trainingSet = exampleSet;
		this.isVoting = isVoting;
		this.params = params;
		this.knowledge = knowledge;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (params != null) {
			sb.append("Params:\n");
			sb.append(params.toString());
			sb.append("\n");
		}
		
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
		
		sb.append("\nCoverage of training examples by rules (1-based):\n");
		for (int eid = 0; eid < trainingSet.size(); ++eid){
			Example ex = trainingSet.getExample(eid);
			int bestRuleId = -1;
			double bestWeight = Double.NEGATIVE_INFINITY;

			List<Integer> matchingRules = new ArrayList<Integer>();

			rid = 1;
			for (Rule r: rules) {
			    if (r.getPremise().evaluate(ex)) {
                    matchingRules.add(rid);
			        if (r.getWeight() > bestWeight) {
                        bestRuleId = rid;
                        bestWeight = r.getWeight();
                    }
                }

				++rid;
			}

			if (bestRuleId == -1) {
			    sb.append("-,");
            } else {
                for (int ruleId : matchingRules) {
                    sb.append(ruleId);
                    if (ruleId == bestRuleId) {
                        sb.append("*");
                    }

                    sb.append(",");
                }
            }

            sb.replace(sb.length() - 1, sb.length(), ";");
			//sb.append((bestRuleId > 0 ? bestRuleId : "-") + ",");
		}

		sb.replace(sb.length()-1, sb.length(), "\n");
	//	sb.append("General information:\n");
	//	sb.append("voting: " + (getIsVoting() ? "true" : "false") + "\n");
		
		return sb.toString();
	}

}
