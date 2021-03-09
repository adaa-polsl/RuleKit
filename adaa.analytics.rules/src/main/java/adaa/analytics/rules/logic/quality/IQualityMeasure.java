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
package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.induction.ContingencyTable;
import com.rapidminer.example.ExampleSet;

/**
 * Interface to be implemented by all classes representing quality measures.
 */
public interface IQualityMeasure {
	public String getName();

	/**
	 * Calculates quality on a data set.
	 *
	 * @param dataset Training set.
	 * @param ct Contingency table.
	 * @return Calculated measure.
	 */
	public double calculate(ExampleSet dataset, ContingencyTable ct);

	/**
	 * Calculates quality from contingency table elements.
	 *
	 * @return Calculated measure.
	 */
	public double calculate(double p, double n, double P, double N);
}
