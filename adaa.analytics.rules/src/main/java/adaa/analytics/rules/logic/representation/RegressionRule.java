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

public class RegressionRule extends Rule {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6597003506869205514L;
	
	private double stddev = 0.0;
	
	public double getConsequenceValue() { return ((SingletonSet)getConsequence().getValueSet()).getValue(); }
	public void setConsequenceValue(double v) {
		((SingletonSet)getConsequence().getValueSet()).setValue(v);
	}
	
	public RegressionRule(CompoundCondition premise, ElementaryCondition consequence) {
		super(premise, consequence);
	}
	
	@Override
	public Covering covers(ExampleSet set, Set<Integer> ids) {
		return covers(set);
	}
	
	@Override
	public void setCoveringInformation(Covering cov) {
		super.setCoveringInformation(cov);
		this.setConsequenceValue(cov.median_y);
		this.stddev = cov.stddev_y;
	}

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
					cov.weighted_n -= w;
					cov.weighted_p += w;
				}
			}
		}

		return cov;
	}	
	
	@Override
	public String toString() {
		double lo = getConsequenceValue() - stddev;
		double hi = getConsequenceValue() + stddev;
		String s = "IF " + premise.toString() + " THEN " + consequence.toString() + " [" + lo + "," + hi + "]";	
		return s;
	}
}
