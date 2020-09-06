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

import adaa.analytics.rules.logic.induction.Covering;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Set;

/**
 * Class representing regression rule.
 * @author Adam Gudys
 *
 */
public class RegressionRule extends Rule {
	
	/** Serialization id. */
	private static final long serialVersionUID = -6597003506869205514L;
	
	/** Standard deviation of examples covered by the rule w.r.t. value in a consequence */
	private double stddev = 0.0;
	
	/** Gets value in the consequence. */
	public double getConsequenceValue() { return ((SingletonSet)getConsequence().getValueSet()).getValue(); }
	/** Gets value in the consequence. */ 
	public void setConsequenceValue(double v) {
		((SingletonSet)getConsequence().getValueSet()).setValue(v);
	}
	
	/**
	 * Creates a regression rule with a given premise and a consequence.
	 * @param premise Rule premise.
	 * @param consequence Rule consequence.
	 */
	public RegressionRule(CompoundCondition premise, ElementaryCondition consequence) {
		super(premise, consequence);
	}
	
	/**
	 * Applies the rule on a specified example set.
	 * @param set Example set.
	 * @param filterIds Ignored.
	 * @return Information about covering.
	 */
	@Override
	public Covering covers(ExampleSet set, Set<Integer> filterIds) {
		return covers(set);
	}
	
	/**
	 * Sets p,n,P,N as well as consequence value and standard deviation on the basis of covering information.
	 * @param cov Covering information.
	 */
	@Override
	public void setCoveringInformation(Covering cov) {
		super.setCoveringInformation(cov);
		this.setConsequenceValue(cov.median_y);
		this.stddev = cov.stddev_y;
	}

	/**
	 * Applies the regression rule on a specified example set.
	 * @param set Example set.
	 * @return Information about covering.
	 */
	@Override
	public Covering covers(ExampleSet set) {
		SortedExampleSet ses = (set instanceof SortedExampleSet) ? (SortedExampleSet)set : null;
		if (ses == null) {
			throw new InvalidParameterException("RegressionRules support only sorted example sets");
		}
		
		Covering cov = new Covering();
		
		double sum_y = 0;
		double sum_y2 = 0;
		
		// put everything to negatives by default
		for (int id = 0; id < set.size(); ++id) {
			Example ex = set.getExample(id);
			double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			cov.weighted_N += w;
			if (this.getPremise().evaluate(ex)) { // if covered
				cov.weighted_n += w; 
				cov.negatives.add(id);
				
				double y = ex.getLabel();
				sum_y += y;
				sum_y2 += y*y; 
			}
		}
		
		cov.mean_y = sum_y / cov.weighted_n;
		cov.stddev_y = Math.sqrt(sum_y2 / cov.weighted_n - cov.mean_y * cov.mean_y); // VX = E(X^2) - (EX)^2
		
		// get median from covered elements
		double cur = 0;
		int medianId = 0;
		Iterator<Integer> it = cov.negatives.iterator();
		while (it.hasNext()) {
			int id = it.next();
			Example ex = set.getExample(id);
			
			// if example covered
			cur += set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			if (cur > cov.weighted_n / 2) {
				break; 
			}
			medianId = id;
		}
		
		cov.median_y = set.getExample(medianId).getLabel();
	
		// update positives
		for (int id = 0; id < set.size(); ++id) {
			Example ex = set.getExample(id);
			
			// if inside epsilon
			if (Math.abs(ex.getLabel() - cov.median_y) <= cov.stddev_y) {
				double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
				cov.weighted_N -= w;
				cov.weighted_P += w;
				// if covered
				if (this.getPremise().evaluate(ex)) {
					cov.negatives.remove(id);
					cov.weighted_n -= w;
					cov.positives.add(id);
					cov.weighted_p += w;
				}
			}
		}

		return cov;
	}	
	
	/**
	 * Generates a text representation of the rule.
	 * @return Text representation.
	 */
	@Override
	public String toString() {
		double lo = getConsequenceValue() - stddev;
		double hi = getConsequenceValue() + stddev;
		String s = "IF " + premise.toString() + " THEN " + consequence.toString() + " [" + DoubleFormatter.format(lo) + "," + DoubleFormatter.format(hi) + "]";
		return s;
	}
}
