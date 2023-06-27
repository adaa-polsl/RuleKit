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

import adaa.analytics.rules.logic.induction.ContingencyTable;
import adaa.analytics.rules.logic.induction.Covering;

import adaa.analytics.rules.logic.quality.IQualityMeasure;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

import static java.lang.Double.NaN;


/**
 * Abstract class representing all kinds of rules (classification/regression/survival).
 * @author Adam Gudys
 *
 */
public abstract class Rule implements Serializable, Cloneable {
	/** Serialization id. */
	private static final long serialVersionUID = -1296329752476014421L;

	protected Map<String, Object> stats = new TreeMap<String, Object>();

	/** Rule premise.*/
	protected CompoundCondition premise;
	
	/** Rule consequence. */
	protected ElementaryCondition consequence;
	
	/** Number of positives covered by the rule (accounting weights). */
	protected double weighted_p = NaN;
	
	/** Number of negatives covered by the rule (accounting weights). */
	protected double weighted_n = NaN;
	
	/** Number of positives in the training set (accounting weights). */
	protected double weighted_P = NaN;
	
	/** Number of negatives in the training set (accounting weights). */
	protected double weighted_N = NaN;
	
	/** Rule weight. */
	protected double weight = Double.NEGATIVE_INFINITY;
	
	/** Rule significance. */
	protected double pvalue = 1.0;

	/** Rule order number. */
	protected int ruleOrderNum = -1;

	/** Number of induced conditions. */
	protected int inducedConditionsCount = 0;

	/** Set of covered positives. */
	transient protected IntegerBitSet coveredPositives;

	/** Set of covered negatives. */
	transient protected IntegerBitSet coveredNegatives;

	/** Gets {@link #premise} */
	public CompoundCondition getPremise() { return premise; }
	/** Sets {@link #premise} */
	public void setPremise(CompoundCondition v) { this.premise = v; }
	
	/** Gets {@link #consequence} */
	public ElementaryCondition getConsequence() { return consequence; }
	/** Sets {@link #consequence} */
	public void setConsequence(ElementaryCondition v) { this.consequence = v; } 
	
	/** Gets {@link #weighted_p} */
	public double getWeighted_p() { return weighted_p; }
	/** Sets {@link #weighted_p} */
	public void setWeighted_p( double v ) { weighted_p = v; }
	
	/** Gets {@link #weighted_n} */
	public double getWeighted_n() { return weighted_n; }
	/** Sets {@link #weighted_n} */
	public void setWeighted_n( double v ) { weighted_n = v; }
	
	/** Gets {@link #weighted_P} */
	public double getWeighted_P() { return weighted_P; }
	/** Sets {@link #weighted_P} */
	public void setWeighted_P( double v ) { weighted_P = v; }
	
	/** Gets {@link #weighted_N} */
	public double getWeighted_N() { return weighted_N; }
	/** Sets {@link #weighted_N} */
	public void setWeighted_N( double v ) { weighted_N = v; }
	
	/** Gets {@link #weight} */
	public double getWeight() { return weight; }
	/** Sets {@link #weight} */
	public void setWeight(double v) { weight = v; } 
	
	/** Gets {@link #inducedConditionsCount} */
	public int getInducedConditionsCount() { return inducedConditionsCount;}
	/** Sets {@link #inducedConditionsCount} */
	public void setInducedContitionsCount(int v) { inducedConditionsCount = v; }

	public int getRuleOrderNum() {
		return ruleOrderNum;
	}

	public void setRuleOrderNum(int ruleOrderNum) {
		this.ruleOrderNum = ruleOrderNum;
	}

	/** Gets {@link #pvalue} */
	public double getPValue() { return pvalue; }
	/** Sets {@link #pvalue} */
	public void setPValue(double v) { pvalue = v; }

	/** Gets {@link #coveredPositives} */
	public IntegerBitSet getCoveredPositives() { return coveredPositives; }
	/** Sets {@link #coveredPositives} */
	public void setCoveredPositives(IntegerBitSet v) { coveredPositives = v; }

	/** Gets {@link #coveredNegatives} */
	public IntegerBitSet getCoveredNegatives() { return coveredNegatives; }
	/** Sets {@link #coveredNegatives} */
	public void setCoveredNegatives(IntegerBitSet v) { coveredNegatives = v; }

	public Object getStat(String key) {  return stats.get(key); }
	public void putStat(String key, Object val) { stats.put(key, val); }

	/**
	 * Creates empty rule.
	 */
	public Rule() {}
	
	/**
	 * Creates a rule with a given premise and a consequence.
	 * @param premise Rule premise.
	 * @param consequence Rule consequence.
	 */
	public Rule(CompoundCondition premise, ElementaryCondition consequence) {
		this();
		this.premise = premise;
		this.consequence = consequence;
	}
	
	/**
	 * Creates new rule on the basis of the reference one (incomplete shallow copy).
	 * @param ref Reference rule
	 */
	public Rule(Rule ref) {
		this.weight = ref.weight;
		this.weighted_p = ref.weighted_p;
		this.weighted_n = ref.weighted_n;
		this.weighted_P = ref.weighted_P;
		this.weighted_N = ref.weighted_N;
		this.premise = ref.premise;
		this.consequence = ref.consequence;
		this.ruleOrderNum = ref.ruleOrderNum;
	}
	
	/**
	 * Sets p,n,P,N on the basis of covering information.
	 * @param cov Covering information.
	 */
	public void setCoveringInformation(Covering cov) {
		this.weighted_p = cov.weighted_p;
		this.weighted_n = cov.weighted_n;
		this.weighted_P = cov.weighted_P;
		this.weighted_N = cov.weighted_N;
	}
	
	/**
	 * Gets covering object containing p,n,P,N.
	 * @return Covering information.
	 */
	public Covering getCoveringInformation() {
		Covering cov = new Covering();
		cov.weighted_n = weighted_n;
		cov.weighted_p = weighted_p;
		cov.weighted_N = weighted_N;
		cov.weighted_P = weighted_P;
		return cov;
	}
	
	/**
	 * Applies the rule on a part of a specified example set.
	 * @param set Example set.
	 * @param filterIds Set of identifiers to be examined.
	 * @return Information about covering.
	 */
	@Deprecated
	public abstract Covering covers(ExampleSet set, Set<Integer> filterIds);
	
	/**
	 * Applies the rule on a specified example set.
	 * @param set Example set.
	 * @return Information about covering.
	 */
	@Deprecated
	public abstract Covering covers(ExampleSet set);

	/***
	 * Calculates {@link #weight} and {@link #pvalue}.
	 *
	 * @param trainSet Training set.
	 * @param ct Contingency table.
	*  @param votingMeasure Measure used as weight.
	 */
	public abstract void updateWeightAndPValue(ExampleSet trainSet, ContingencyTable ct, IQualityMeasure votingMeasure);
	
	/**
	 * Applies the rule on a specified example set.
	 * @param set Example set.
	 * @param ct Output contingency table.
	 */
	public void covers(ExampleSet set, ContingencyTable ct) {
		Set<Integer> dummy = new Set<Integer>() {
			public int size() { return 0; }
			public boolean isEmpty() { return false; }
			public boolean contains(Object o) { return false; }
			public Iterator<Integer> iterator() { return null; }
			public Object[] toArray() { return new Object[0]; }
			public <T> T[] toArray(@NotNull T[] ts) { return null; }
			public boolean add(Integer i) { return false; }
			public boolean remove(Object o) { return false; }
			public boolean containsAll(@NotNull Collection<?> collection) { return false; }
			public boolean addAll(@NotNull Collection<? extends Integer> collection) { return false; }
			public boolean retainAll(@NotNull Collection<?> collection) { return false; }
			public boolean removeAll(@NotNull Collection<?> collection) { return false; }
			public void clear() { }
		};

		this.covers(set, ct, dummy, dummy);
	}

	/**
	 * Gets indices of covered examples without positive/negative distinction.
	 * @param set Example set to be examined.
	 * @return Set of indices of covered examples.
	 */
	public Set<Integer> coversUnlabelled(ExampleSet set) {
		Set<Integer> out = new HashSet<>();
		int id = 0;
		for (Example ex : set) {
			if (premise.evaluate(ex)) {
				out.add(id);
			}
			++ id;

		}
		return out;
	}

	/**
	 * Applies the rule on a specified example set.
	 * @param set Example set.
	 * @param ct Output contingency table.
	 * @param positives Output collection of covered positive ids.
	 * @param negatives Output collection of covered negative ids.
	 */
	public void covers(ExampleSet set, ContingencyTable ct, Set<Integer> positives, Set<Integer> negatives) {
		assert false: "Not implemented: Rule.covers(ExampleSet set, ContingencyTable ct, Set<Integer> positives, Set<Integer> negatives)";
	}
	
	/**
	 * Generates a text representation of the rule.
	 * @return Text representation.
	 */
	public String toString() {
		String consequenceString;
		if (consequence.valueSet instanceof SingletonSet &&
				Double.isNaN(((SingletonSet) consequence.valueSet).value) && ((SingletonSet) consequence.valueSet).mapping == null) {
			consequenceString = "";
		} else {
			consequenceString = consequence.toString();
		}
		String s = "IF " + premise.toString() + " THEN " + consequenceString;
		return s;
	}
	
	/**
	 * Generates statistics in a text form.
	 * @return Rule statistics.
	 */
	public String printStats() {
		String s ="(p=" + weighted_p + ", n=" + weighted_n + ", P=" + weighted_P + ", N=" + weighted_N + ", weight=" + getWeight() + ", pval=" + pvalue + ")";
		return s;
	}

	public String getTableHeader() {
		String header = "Rule,p,n,P,N,weight,p-value,covered_positives,covered_negatives,attributes";

		for (String key : stats.keySet()) {
			header += ", " + key;
		}

		return header;
	}

	/**
	 * Converts a rule to semicolon-separated tabular form with selected statistics.
	 * @return Tabular rule representation.
	 */
	public String toTable() {
		StringBuilder sb = new StringBuilder();
		char delim = ',';

		sb.append('\"');
		sb.append(toString()); sb.append('\"'); sb.append(delim);
		sb.append(weighted_p); sb.append(delim);
		sb.append(weighted_n); sb.append(delim);
		sb.append(weighted_P); sb.append(delim);
		sb.append(weighted_N); sb.append(delim);
		sb.append(weight); sb.append(delim);
		sb.append(pvalue); sb.append(delim);

		if (coveredPositives.size() > 0) {
			sb.append('\"');
			for (int ex : coveredPositives) {
				sb.append(ex);
				sb.append(',');
			}
			sb.setCharAt(sb.length() - 1, '\"'); // replace comma with quote
		}
		sb.append(delim);

		if (coveredNegatives.size() > 0) {
			sb.append('\"');
			for (int ex : coveredNegatives) {
				sb.append(ex);
				sb.append(',');
			}
			sb.setCharAt(sb.length() - 1, '\"'); // replace comma with quote
		}
		sb.append(delim);

		sb.append('\"');
		for (String a: getPremise().getAttributes()) {
			sb.append(a);
			sb.append(',');
		}
		sb.setCharAt(sb.length() - 1, '\"'); // replace comma with quote

		// add extra statistics
		for (String key : stats.keySet()) {
			sb.append(',');
			sb.append(stats.get(key));
		}

		return sb.toString();
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else {
			Rule ref = (obj instanceof Rule) ? (Rule) obj : null;
			if (ref != null) {
				return premise.equals(ref.premise) && consequence.equals(ref.consequence);
			} else {
				return false;
			}
		}
	}

	public void copyFrom(final Rule ref) {
		weighted_p = ref.weighted_p;
		weighted_n = ref.weighted_n;
		weighted_P = ref.weighted_P;
		weighted_N = ref.weighted_N;
		weight = ref.weight;
		pvalue = ref.pvalue;
		premise = ref.premise;
		consequence = ref.consequence;
		coveredPositives = ref.coveredPositives;
		coveredNegatives = ref.coveredNegatives;
		inducedConditionsCount = ref.inducedConditionsCount;
		ruleOrderNum = ref.ruleOrderNum;
	}
}
