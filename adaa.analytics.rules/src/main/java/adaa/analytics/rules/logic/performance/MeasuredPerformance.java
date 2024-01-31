/**
 * Copyright (C) 2001-2019 by RapidMiner and the contributors
 * 
 * Complete list of developers available at our web site:
 * 
 * http://rapidminer.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
*/
package adaa.analytics.rules.logic.performance;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;


public abstract class MeasuredPerformance {

	/** The averages are summed up each time buildAverage is called. */
	private double meanSum;

	/** The squared averages are summed up each time buildAverage is called. */
	private double meanSquaredSum;

	/** Counts the number of times, build average was executed. */
	private int averageCount;


	public MeasuredPerformance() {
		this.meanSum = Double.NaN;
		this.meanSquaredSum = Double.NaN;
		this.averageCount = 0;
	}
	/**
	 * Returns the name of this averagable. The returned string should only contain lowercase
	 * letters and underscore (RapidMiner parameter format) since the names will be automatically
	 * used for GUI purposes.
	 */
	public abstract String getName();

	/**
	 * This method returns the macro average if it was defined and the micro average (the current
	 * value) otherwise. This method should be used instead of {@link #getMikroAverage()} for
	 * optimization purposes, i.e. by methods like <code>getFitness()</code> of performance
	 * criteria.
	 */
	public final double getAverage() {
		double average = Double.NaN;
		if (averageCount > 0) {
			average = getMakroAverage();
		}
		if (Double.isNaN(average)) {
			average = getMikroAverage();
		}
		return average;
	}

	/**
	 * Returns the (current) value of the averagable (the average itself).
	 */
	public abstract double getMikroAverage();

	/**
	 * Returns the average value of all performance criteria average by using the
	 */
	public final double getMakroAverage() {
		return meanSum / averageCount;
	}

	/** Counts a single example, e.g. by summing up errors. */
	public abstract void countExample(Example example);


	/** Initializes the criterion. The default implementation does nothing. */
	public void startCounting(ExampleSet set, boolean useExampleWeights)  {}

}
