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
package adaa.analytics.rules.logic.representation.rule;

import adaa.analytics.rules.data.IDataColumnAdapter;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.metadata.EStatisticType;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.logic.induction.ContingencyTable;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ChiSquareVarianceTest;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.representation.condition.CompoundCondition;
import adaa.analytics.rules.logic.representation.condition.ElementaryCondition;
import adaa.analytics.rules.logic.representation.exampleset.SortedExampleSetEx;
import adaa.analytics.rules.logic.representation.valueset.SingletonSet;
import adaa.analytics.rules.utils.DoubleFormatter;
import adaa.analytics.rules.utils.Pair;

import java.security.InvalidParameterException;
import java.util.*;

import static java.lang.Float.NaN;

/**
 * Class representing regression rule.
 * @author Adam Gudys
 *
 */
public class RegressionRule extends Rule {

	/** Serialization id. */
	private static final long serialVersionUID = -6597003506869205514L;

	private static boolean useMean = false;

	public static void setUseMean(boolean v) { useMean = v; }

	public static boolean getUseMean() { return useMean; }

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
	public void updateWeightAndPValue(IExampleSet trainSet, ContingencyTable ct, IQualityMeasure votingMeasure) {
		ChiSquareVarianceTest test = new ChiSquareVarianceTest();
		double expectedDev = Math.sqrt(trainSet.getAttributes().getLabel().getStatistic(EStatisticType.VARIANCE));

		int sampleSize = (int)(ct.weighted_p + ct.weighted_n);

		Pair<Double, Double> statsAndPVal;
		if (sampleSize > 1) {
			statsAndPVal = test.calculateLower(expectedDev, ct.stddev_y, sampleSize);
		} else {
			// one element sample will always have zero variance.
			// value of test statistics is not used
			statsAndPVal = new Pair<Double,Double>(0.0, 0.0);
		}

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
		this.setConsequenceValue(useMean ? cov.mean_y : cov.median_y);
		this.stddev = cov.stddev_y;
	}

	/**
	 * Applies the rule on a specified example set.
	 * @param set Example set.
	 * @param filterIds Ignored.
	 * @return Information about covering.
	 */
	@Override
	public Covering covers(IExampleSet set, Set<Integer> filterIds) {
		return covers(set);
	}


	/**
	 * Applies the regression rule on a specified example set.
	 * @param set Example set.
	 * @return Information about covering.
	 */
	@Override
	public Covering covers(IExampleSet set) {
		//assert false: "Obsolete method in RegressionRule: Covering covers(ExampleSet set)";
		//return null;

		Covering cov = new Covering();
		this.covers(set, cov, cov.positives, cov.negatives);
		return cov;
	}

	@Override
	public void covers(IExampleSet set, ContingencyTable ct, Set<Integer> positives, Set<Integer> negatives) {
		IDataColumnAdapter weightDataColumnDoubleAdapter = set.getDataColumnDoubleAdapter(set.getAttributes().getWeight(), Double.NaN);

		SortedExampleSetEx ses = (set instanceof SortedExampleSetEx) ? (SortedExampleSetEx)set : null;
		if (ses == null) {
			throw new InvalidParameterException("RegressionRules support only ListedExampleSet example sets");
		}
		double sum_y = 0.0, sum_y2 = 0.0;
		//initially, everything as negatives
		List<Integer> orderedNegatives = new ArrayList<Integer>(set.size());


		for (int id = 0; id < set.size(); ++id) {
			Example ex = set.getExample(id);

			if (this.getPremise().evaluate(ex)) { // if covered
				double w = ses.weights[id];
				double y = ses.labelsWeighted[id];

				ct.weighted_n += w;
				sum_y += y;
				sum_y2 += y*y;
				orderedNegatives.add(id);
				negatives.add(id);
			}
		}

		if (negatives.size() == 0) {
			return;
		}

		ct.mean_y = sum_y / ct.weighted_n;
		ct.mean_y2 = sum_y2 / ct.weighted_n;
		ct.stddev_y = Math.sqrt(sum_y2 / ct.weighted_n - ct.mean_y * ct.mean_y); // VX = E(X^2) - (EX)^2

		boolean weighted = (set.getAttributes().getWeight() != null);
		int medianId = 0;

		if (weighted) {
			double cur = 0;
			Iterator<Integer> it = negatives.iterator();
			while (it.hasNext()) {
				int id = it.next();

				// if example covered
				cur += set.getAttributes().getWeight() == null ? 1.0 : weightDataColumnDoubleAdapter.getDoubleValue(id);
				if (cur > ct.weighted_n / 2) {
					break;
				}
				medianId = id;
			}
		} else {
			medianId = orderedNegatives.get(orderedNegatives.size() / 2);
		}

		ct.median_y = set.getExample(medianId).getLabelValue();

		// update positives inside epsilon
		double label = useMean ? ct.mean_y : ct.median_y;

		// binary search to get elements inside epsilon
		// assumption: double value preceeding/following one being search appears at most once
		int lo = Arrays.binarySearch(ses.labels, Math.nextDown(label - ct.stddev_y));
		if (lo < 0) {
			lo = -(lo + 1); // if element not found, extract id of the next larger: ret = (-(insertion point) - 1)
		} else { lo += 1;} // if element found move to next one (first inside a range)

		int hi = Arrays.binarySearch(ses.labels, Math.nextUp(label + ct.stddev_y));
		if (hi < 0) { hi = -(hi + 1); // if element not found, extract id of the next larger: ret = (-(insertion point) - 1)
		} // if element found - do nothing (first after the range)

		ct.weighted_P = ses.totalWeightsBefore[hi] - ses.totalWeightsBefore[lo];
		ct.weighted_N = ses.totalWeightsBefore[set.size()] - ct.weighted_P;

		for (int id = lo; id < hi; ++id) {
			Example ex = set.getExample(id);
			if (this.getPremise().evaluate(ex)) {
				negatives.remove(id);
				ct.weighted_n -= ses.weights[id];
				positives.add(id);
				ct.weighted_p += ses.weights[id];
			}
		}
	}
	/**
	 * Generates a text representation of the rule.
	 * @return Text representation.
	 */
	@Override
	public String toString() {
		String consequenceString;
		if (consequence.getValueSet() instanceof SingletonSet &&
				((SingletonSet) consequence.getValueSet()).getValue() == NaN && ((SingletonSet) consequence.getValueSet()).getMapping() == null) {
			consequenceString = "";
		} else {
			double lo = getConsequenceValue() - stddev;
			double hi = getConsequenceValue() + stddev;
			consequenceString = consequence.toString() + " [" + DoubleFormatter.format(lo) + "," + DoubleFormatter.format(hi) + "]";
		}
		String s = "IF " + premise.toString() + " THEN " + consequenceString;
		return s;
	}
}
