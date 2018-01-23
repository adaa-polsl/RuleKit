package adaa.analytics.rules.logic.representation;

import java.io.Serializable;
import java.util.Set;

import adaa.analytics.rules.logic.induction.Covering;

import com.rapidminer.example.ExampleSet;

public abstract class Rule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1296329752476014421L;
	/**
	 * 
	 */
	protected CompoundCondition premise;
	protected ElementaryCondition consequence;
	
	protected double weighted_p = Double.NaN;
	protected double weighted_n = Double.NaN;
	protected double weighted_P = Double.NaN;
	protected double weighted_N = Double.NaN;
	
	protected double weight = 1.0;
	
	protected int inducedConditionsCount = 0;
	
	public CompoundCondition getPremise() { return premise; }
	public void setPremise(CompoundCondition v) { this.premise = v; }
	
	public ElementaryCondition getConsequence() { return consequence; }
	public void setConsequence(ElementaryCondition v) { this.consequence = v; } 
	
	public double getWeighted_p() { return weighted_p; }
	public void setWeighted_p( double v ) { weighted_p = v; }
	
	public double getWeighted_n() { return weighted_n; }
	public void setWeighted_n( double v ) { weighted_n = v; }
	
	public double getWeighted_P() { return weighted_P; }
	public void setWeighted_P( double v ) { weighted_P = v; }
	
	public double getWeighted_N() { return weighted_N; }
	public void setWeighted_N( double v ) { weighted_N = v; }
	
	public double getWeight() { return weight; }
	public void setWeight(double v) { weight = v; } 
	
	public int getInducedConditionsCount() { return inducedConditionsCount;}
	public void setInducedContitionsCount(int v) { inducedConditionsCount = v; }
	
	public Rule() {}
	
	public Rule(CompoundCondition premise, ElementaryCondition consequence) {
		this();
		this.premise = premise;
		this.consequence = consequence;
	}
	
	public Rule(Rule ref) {
		this.weight = ref.weight;
		this.weighted_p = ref.weighted_p;
		this.weighted_n = ref.weighted_n;
		this.weighted_P = ref.weighted_P;
		this.weighted_N = ref.weighted_N;
		
		this.premise = ref.premise;
		this.consequence = ref.consequence;
	}
	
	public void setCoveringInformation(Covering cov) {
		this.weighted_p = cov.weighted_p;
		this.weighted_n = cov.weighted_n;
		this.weighted_P = cov.weighted_P;
		this.weighted_N = cov.weighted_N;
	}
	
	public Covering getCoveringInformation() {
		Covering cov = new Covering();
		cov.weighted_n = weighted_n;
		cov.weighted_p = weighted_p;
		cov.weighted_N = weighted_N;
		cov.weighted_P = weighted_P;
		return cov;
	}
	
	
	public abstract Covering covers(ExampleSet set, Set<Integer> ids);
	
	public abstract Covering covers(ExampleSet set);
	
	public String toString() {
		String s = "IF " + premise.toString() + " THEN " + consequence.toString();	
		return s;
	}
	
	public String printStats() {
		String s ="(p=" + weighted_p + ", n=" + weighted_n + ", P=" + weighted_P + ", N=" + weighted_N + ", weight=" + getWeight() + ")";
		return s;
	}
}
