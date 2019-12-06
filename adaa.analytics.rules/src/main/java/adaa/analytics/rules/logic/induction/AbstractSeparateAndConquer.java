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
package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.RuleSetBase;
import com.rapidminer.example.ExampleSet;

/***
 * Abstract base class for all separate and conquer algorithms for induction of rule-based models
 * (classification, regression, survival).
 * 
 * @author Adam Gudys
 * 
 */
public abstract class AbstractSeparateAndConquer {

	/**
	 * Induction parameters.
	 */
	protected final InductionParameters params;
	
	/**
	 * Rule factory.
	 */
	protected RuleFactory factory;
	
	/**
	 * Sets induction parameters.
	 * @param params Induction paremeters.
	 */
	public AbstractSeparateAndConquer(final InductionParameters params) {
		this.params = params;
	}
	
	/**
	 * Trains a rule classifier.
	 * @param trainSet Training set.
	 * @return Rule-based model.
	 */
	public abstract RuleSetBase run(final ExampleSet trainSet);
}
