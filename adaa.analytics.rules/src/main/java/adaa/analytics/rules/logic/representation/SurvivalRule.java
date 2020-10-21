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
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Class representing a survival rule.
 * @author Adam Gudys
 *
 */
public class SurvivalRule extends Rule {

	/** Serialization id. */
	private static final long serialVersionUID = -2143653515226214179L;
	
	/** Name of the attribute role representing survival time. */
	public static final String SURVIVAL_TIME_ROLE = "survival_time";
	
	/** Kaplan-Meier estimator in a rule consequence. */
	protected KaplanMeierEstimator estimator;
	
	/** Gets {@link #estimator}. */ 
	public KaplanMeierEstimator getEstimator() { return estimator; }
	/** Sets {@link #estimator}. */
	public void setEstimator(KaplanMeierEstimator v) { estimator = v; }
	
	/**
	 * Creates a survival rule with a given premise (consequence is ignored).
	 * @param premise Rule premise.
	 * @param consequence Ignored.
	 */
	public SurvivalRule(CompoundCondition premise, ElementaryCondition consequence) {
		super(premise, consequence);
	}
	
	/**
	 * Creates a survival rule from rule with empty consequence and survival function estimate.
	 * @param r Reference rule.
	 * @param estimator Survival function estimate.
	 */
	public SurvivalRule(Rule r, KaplanMeierEstimator estimator) {
		super(r);
		this.estimator = estimator;
	}
	
	/**
	 * Applies the rule on a part of a specified example set.
	 * @param set Example set.
	 * @param ids Set of identifiers to be examined.
	 * @return Information about covering.
	 */
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
	public void covers(ExampleSet set, @NotNull ContingencyTable ct) {
		boolean unweighted = set.getAttributes().getWeight() == null;

		for (Example ex: set) {
			double weight = unweighted ? 1.0 : ex.getWeight();

			ct.weighted_P += weight;
			if (this.getPremise().evaluate(ex)){
				ct.weighted_p += weight;
			}
		}
	}

	@Override
	public void covers(ExampleSet set, @NotNull ContingencyTable ct,  @NotNull Set<Integer> positives, @NotNull Set<Integer> negatives) {
		int id = 0;
		boolean unweighted = set.getAttributes().getWeight() == null;

		for (Example ex : set) {
			double weight = unweighted ? 1.0 : ex.getWeight();

			ct.weighted_P += weight;
			if (this.getPremise().evaluate(ex)){
				positives.add(id);
				ct.weighted_p += weight;
			}
			++id;
		}
	}

	/**
	 * Applies the rule on a specified example set.
	 * @param set Example set.
	 * @return Information about covering.
	 */
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
