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
import adaa.analytics.rules.logic.quality.ChiSquareVarianceTest;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.tools.container.Pair;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

	/***
	 * Calculates {@link #weight} and {@link #pvalue}.
	 *
	 * @param trainSet Training set.
	 * @param ct Contingency table.
	 *  @param votingMeasure Measure used as weight.
	 */
	@Override
	public void updateWeightAndPValue(ExampleSet trainSet, ContingencyTable ct, IQualityMeasure votingMeasure) {
		ChiSquareVarianceTest test = new ChiSquareVarianceTest();
		double expectedDev = Math.sqrt(trainSet.getStatistics(trainSet.getAttributes().getLabel(), Statistics.VARIANCE));
		Pair<Double,Double> statsAndPVal = test.calculateLower(expectedDev, ct.stddev_y, (int)(ct.weighted_p + ct.weighted_n));

		this.weight = votingMeasure.calculate(trainSet, ct);
		this.pvalue = statsAndPVal.getSecond();
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
	 * Applies the regression rule on a specified example set.
	 * @param set Example set.
	 * @return Information about covering.
	 */
	@Override
	public Covering covers(ExampleSet set) {
		//assert false: "Obsolete method in RegressionRule: Covering covers(ExampleSet set)";
		//return null;

		Covering cov = new Covering();
		this.covers(set, cov, cov.positives, cov.negatives);
		return cov;
	}
	
	@Override
	public void covers(ExampleSet set, ContingencyTable ct, Set<Integer> positives, Set<Integer> negatives) {
		SortedExampleSet ses = (set instanceof SortedExampleSet) ? (SortedExampleSet)set : null;
		if (ses == null) {
			throw new InvalidParameterException("RegressionRules support only sorted example sets");
		}
		double sum_y = 0.0, sum_y2 = 0.0;
		//initially, everything as negatives
		List<Integer> orderedNegatives = new ArrayList<Integer>(set.size());

		for (int id = 0; id < set.size(); ++id) {
			Example ex = set.getExample(id);
			double weight = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();

			ct.weighted_N += weight;

			if (this.getPremise().evaluate(ex)) { // if covered
				ct.weighted_n += weight;
				orderedNegatives.add(id);
				negatives.add(id);

				double y = ex.getLabel();
				sum_y += y;
				sum_y2 += y*y;
			}
		}

		if (negatives.size() == 0) {
			return;
		}

		ct.mean_y = sum_y / ct.weighted_n;
		ct.stddev_y = Math.sqrt(sum_y2 / ct.weighted_n - ct.mean_y * ct.mean_y); // VX = E(X^2) - (EX)^2

		boolean weighted = (set.getAttributes().getWeight() != null);
		int medianId = 0;

		if (weighted) {
			double cur = 0;
			Iterator<Integer> it = negatives.iterator();
			while (it.hasNext()) {
				int id = it.next();
				Example ex = set.getExample(id);

				// if example covered
				cur += set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
				if (cur > ct.weighted_n / 2) {
					break;
				}
				medianId = id;
			}
		} else {
			medianId = orderedNegatives.get(orderedNegatives.size() / 2);
		}

		ct.median_y = set.getExample(medianId).getLabel();

		// update positives
		for (int id = 0; id < set.size(); ++id) {
			Example ex = set.getExample(id);

			// if inside epsilon
			if (Math.abs(ex.getLabel() - ct.median_y) <= ct.stddev_y) {
				double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
				ct.weighted_N -= w;
				ct.weighted_P += w;
				// if covered
				if (this.getPremise().evaluate(ex)) {
					negatives.remove(id);
					ct.weighted_n -= w;
					positives.add(id);
					ct.weighted_p += w;
				}
			}
		}
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
