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
package adaa.analytics.rules.logic.representation.ruleset;

import java.util.*;

import adaa.analytics.rules.logic.induction.InductionParameters;

import adaa.analytics.rules.logic.representation.Knowledge;
import adaa.analytics.rules.logic.representation.rule.Rule;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.utils.OperatorException;


/**
 * Abstract class representing all rule-based models (classification/regression/survival).
 * @author Adam Gudys
 *
 */
public abstract class RuleSetBase extends PredictionModel {

	/**
	 * Auxiliary class storing result of rule set significance test.
	 * @author Adam Gudys
	 *
	 */
	public class Significance {
		/** Average p-value of all rules. */
		public double p = 0;
		
		/** Fraction of rules significant at assumed level. */
		public double fraction = 0;
	};

	private class AttributeRank {
		public int count;

		public double weight;

		public AttributeRank(int count, double weight) {
			this.count = count;
			this.weight = weight;
		}
	}
	
	/** Serialization identifier. */
	private static final long serialVersionUID = -7112032011785315168L;

	public static final String ANNOTATION_TEST_REPORT = "annotation_test_report";

	/** Collection of attributes */
	protected List<String> attributes = new ArrayList<>();
	
	/** Collection of rules. */
	protected List<Rule> rules = new ArrayList<Rule>();

	/** Value indicating whether rules are voting. */
	protected boolean isVoting;
	
	/** Induction paramters. */
	protected InductionParameters params = null;
	
	/** User's knowledge. */
	protected Knowledge knowledge = null;
	
	/** Time of constructing the rule set. */
	protected double totalTime;
	
	/** Time of growing. */
	protected double growingTime;
	
	/** Time of pruning. */
	protected double pruningTime;
	
	/** Gets {@link #totalTime} */
	public double getTotalTime() { return totalTime; }
	/** Sets {@link #totalTime} */
	public void setTotalTime(double v) { totalTime = v; }
	
	/** Gets {@link #growingTime} */
	public double getGrowingTime() { return growingTime; }
	/** Sets {@link #growingTime} */
	public void setGrowingTime(double v) { growingTime = v; }
	
	/** Gets {@link #pruningTime} */
	public double getPruningTime() { return pruningTime; }
	/** Sets {@link #pruningTime} */
	public void setPruningTime(double v) { pruningTime = v; }

	/** Gets {@link #isVoting} */
	public boolean getIsVoting() { return isVoting; }
	/** Sets {@link #isVoting} */
	public void setIsVoting(boolean v) { isVoting = v; }
	
	/** Gets {@link #params} */
	public InductionParameters getParams() { return params; }
	
	/** Gets {@link #rules} */
	public List<Rule> getRules() { return rules; }
	
	/**
	 * Adds rule to the collection.
	 * @param v Rule to be added.
	 */
	public void addRule(Rule v) { rules.add(v); }

	/**
	 * Applies the model to a single example and returns the predicted class value.
	 */
	public abstract double predict(Example example) throws OperatorException;

	/** Iterates over all examples and applies the model to them. */
	@Override
	public IExampleSet performPrediction(IExampleSet exampleSet, IAttribute predictedLabel) throws OperatorException {
		Iterator<Example> r = exampleSet.iterator();

		while (r.hasNext()) {
			Example example = r.next();
			example.setValue(predictedLabel, predict(example));

		}
		return exampleSet;
	}
	/**
	 * Calculates number of conditions.
	 * @return Number of conditions.
	 */
	public double calculateConditionsCount() {
		double cnt = 0;
		for (Rule r : rules) {
			cnt += r.getPremise().getAttributes().size();
			//cnt += r.getPremise().getSubconditions().size();
		}
		return cnt / rules.size();
	}
	
	/**
	 * Calculates number of induced conditions.
	 * @return Number of induced conditions.
	 */
	public double calculateInducedCondtionsCount() {
		double cnt = 0;
		for (Rule r : rules) {
			cnt += r.getInducedConditionsCount();
		}
		return cnt / rules.size();
	}
	
	/**
	 * Calculates average rule coverage.
	 * @return Average rule coverage.
	 */
	public double calculateAvgRuleCoverage() {
		double cov = 0;
		for (Rule r : rules) {
			cov += (r.weighted_p + r.weighted_n) / (r.weighted_P + r.weighted_N);
		}
		return cov / rules.size();
	}
	
	/**
	 * Calculates average rule precision.
	 * @return Average rule precision.
	 */
	public double calculateAvgRulePrecision() {
		double prec = 0;
		for (Rule r : rules) {
			prec += r.weighted_p / (r.weighted_p + r.weighted_n);
		}
		return prec / rules.size();
	}
	
	/**
	 * Calculates average rule quality.
	 * @return Average rule quality. 
	 */
	public double calculateAvgRuleQuality() {
		double q = 0.0;
		for (Rule rule : rules) {
			q += rule.getWeight();
		}
		return q / rules.size();
	}
	
	/**
	 * Evaluates significance of the rule set. 
	 * @param alpha Significance level.
	 * @return Average rules p-value and fraction of rules significant at assumed level.
	 */
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
	
	/**
	 * Evaluates significance of the rule set with false discovery rate correction. 
	 * @param alpha Significance level.
	 * @return Average rules p-value and fraction of rules significant at assumed level.
	 */
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
	
	/**
	 * Evaluates significance of the rule set with familiy-wise error rate correction. 
	 * @param alpha Significance level.
	 * @return Average rules p-value and fraction of rules significant at assumed level.
	 */
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
	
	/**
	 * Initializes members.
	 * @param exampleSet Training set.
	 * @param isVoting Voting flag.
	 * @param params Induction parameters.
	 * @param knowledge User's knowledge.
	 */
	public RuleSetBase(IExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
		super(exampleSet);
		this.isVoting = isVoting;
		this.params = params;
		this.knowledge = knowledge;

		for (IAttribute attr: exampleSet.getAttributes()) {
			attributes.add(attr.getName());
		}
	}

	/**
	 * Generates text representation of the rule set which contains:
	 * <p><ul>
	 * <li>induction parameters,
	 * <li>user's knowledge (if defined),
	 * <li>list of rules, 
	 * <li>information about coverage of the training set examples.
	 * </ul>
	 * @return Rule set in the text form.
	 */
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
		Map<String, AttributeRank> attributeCountsWeights = new LinkedHashMap<>(); // create attribute ranking
		Map<String, Double> attributeWeights = new LinkedHashMap<>(); // create attribute ranking
		for (String a: attributes) {
			attributeCountsWeights.put(a, new AttributeRank(0, 0.0));
		}
		int rid = 1;

		for (Rule r : rules) {
			sb.append("r" + rid + ": " + r.toString() + " " + r.printStats() + "\n");
			++rid;

			// update attribute ranking
			for (String a: r.getPremise().getAttributes()) {
				AttributeRank rank = attributeCountsWeights.get(a);
				rank.weight += r.getWeight();
				++rank.count;
			}
		}
		List<Map.Entry<String,AttributeRank>> ranking = new ArrayList<>();
		ranking.addAll(attributeCountsWeights.entrySet());
		ranking.sort((Map.Entry<String,AttributeRank> e1, Map.Entry<String,AttributeRank> e2) ->
				Integer.compare(e1.getValue().count, e2.getValue().count));

		sb.append("\nAttribute ranking (by count):\n");
		for (Map.Entry<String,AttributeRank> e : ranking) {
			sb.append(e.getKey() + ": " + e.getValue().count + "\n");
		}

		ranking.sort((Map.Entry<String,AttributeRank> e1, Map.Entry<String,AttributeRank> e2) ->
				Double.compare(e1.getValue().weight, e2.getValue().weight));

		sb.append("\nAttribute ranking (by weight):\n");
		for (Map.Entry<String,AttributeRank> e : ranking) {
			sb.append(e.getKey() + ": " + e.getValue().weight + "\n");
		}

		/*
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
		 */

		sb.replace(sb.length()-1, sb.length(), "\n");
	//	sb.append("General information:\n");
	//	sb.append("voting: " + (getIsVoting() ? "true" : "false") + "\n");
		
		return sb.toString();
	}

	public String toTable() {
		StringBuilder sb = new StringBuilder();

		if (rules.size() > 0) {
			sb.append(rules.get(0).getTableHeader());
			sb.append('\n');

			for (Rule r : rules) {
				sb.append(r.toTable());
				sb.append('\n');
			}
		}

		return sb.toString();
	}

}
