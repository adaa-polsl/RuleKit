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
