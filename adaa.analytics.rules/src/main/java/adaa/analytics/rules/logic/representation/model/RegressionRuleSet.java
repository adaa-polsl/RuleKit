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
package adaa.analytics.rules.logic.representation.model;

import adaa.analytics.rules.logic.induction.InductionParameters;

import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.logic.representation.condition.ConditionBase;
import adaa.analytics.rules.logic.representation.condition.ElementaryCondition;
import adaa.analytics.rules.logic.representation.rule.Rule;
import adaa.analytics.rules.logic.representation.valueset.SingletonSet;
import adaa.analytics.rules.utils.OperatorException;

/**
 * Class representing a set of regression rules.
 * @author Adam Gudys
 *
 */
public class RegressionRuleSet extends RuleSetBase {

	/** Serialization identifier. */
	private static final long serialVersionUID = -676053943659766492L;
	
	/** Default prediction value. */
	protected double defaultValue = -1;
	
	/** Gets {@link #defaultValue}. */
	public double getDefaultValue() { return defaultValue; }
	/** Sets {@link #defaultValue}. */
	public void setDefaultValue(double defaultValue) { this.defaultValue = defaultValue; }
	
	/**
	 * Invokes base class constructor.
	 * @param exampleSet Training set.
	 * @param isVoting Voting flag.
	 * @param params Induction parameters.
	 * @param knowledge User's knowledge.
	 */
	public RegressionRuleSet(IExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
		super(exampleSet, isVoting, params, knowledge);
	}

	/**
	 * Calculate prediction for a given example.
	 * @param example Example to be examined.
	 * @return Output prediction.
	 */
	@Override
	public double predict(Example example)  {
		double result = 0.0;
		double weightSum = 0.0;
		
		for (Rule rule : rules) {
			if (rule.getPremise().evaluate(example)) {
				ConditionBase c = rule.getConsequence();
				double partial = Double.NaN;
				if (c instanceof ElementaryCondition) {
					ElementaryCondition consequence = ((ElementaryCondition)c);
					SingletonSet d = (SingletonSet)consequence.getValueSet();
					partial = d.getValue();
				}
				
				result += partial * rule.getWeight();
				weightSum += rule.getWeight();
			}
		}
		
		return (weightSum > 0) ? (result / weightSum) : defaultValue;
	}

}
