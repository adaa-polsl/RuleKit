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

import com.rapidminer.example.ExampleSet;

import java.io.Serializable;
import java.util.Set;

/**
 * Abstract class representing all kinds of rules (classification/regression/survival).
 * @author Adam Gudys
 *
 */
public abstract class Rule implements Serializable {
	/** Serialization id. */
	private static final long serialVersionUID = -1296329752476014421L;
	
	/** Rule premise.*/
	protected CompoundCondition premise;
	
	/** Rule consequence. */
	protected ElementaryCondition consequence;
	
	/** Number of positives covered by the rule (accounting weights). */
	protected double weighted_p = Double.NaN;
	
	/** Number of negatives covered by the rule (accounting weights). */
	protected double weighted_n = Double.NaN;
	
	/** Number of positives in the training set (accounting weights). */
	protected double weighted_P = Double.NaN;
	
	/** Number of negatives in the training set (accounting weights). */
	protected double weighted_N = Double.NaN;
	
	/** Rule weight. */
	protected double weight = 1.0;
	
	/** Rule significance. */
	protected double pvalue = 1.0;
	
	/** Number of induced conditions. */
	protected int inducedConditionsCount = 0;
	
	
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
	
	/** Gets {@link #pvalue} */
	public double getPValue() { return pvalue; }
	/** Sets {@link #pvalue} */
	public void setPValue(double v) { pvalue = v; }
	
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
	public abstract Covering covers(ExampleSet set, Set<Integer> filterIds);
	
	/**
	 * Applies the rule on a specified example set.
	 * @param set Example set.
	 * @return Information about covering.
	 */
	public abstract Covering covers(ExampleSet set);
	
	/**
	 * Applies the rule on a specified example set.
	 * @param set Example set.
	 * @param ct Output contingency table.
	 * @param positives Output collection of covered positive ids.
	 * @param negatives Output collection of covered negative ids.
	 */
	public void covers(ExampleSet set, ContingencyTable ct, Set<Integer> positives, Set<Integer> negatives) {
	//	throw new Exception("Not implemented: Rule.covers(ExampleSet set, ContingencyTable ct, Set<Integer> positives, Set<Integer> negatives)");
	}
	
	/**
	 * Generates a text representation of the rule.
	 * @return Text representation.
	 */
	public String toString() {
		String s = "IF " + premise.toString() + " THEN " + consequence.toString();	
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
}
