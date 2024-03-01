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

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.tools.Tools;

import java.util.List;


public abstract class AbstractPerformanceCounter {

	public AbstractPerformanceCounter() {
	}
	/**
	 * Returns the name of this averagable. The returned string should only contain lowercase
	 * letters and underscore (RapidMiner parameter format) since the names will be automatically
	 * used for GUI purposes.
	 */
	public abstract String getName();

	/**
	 * Returns the (current) value of the averagable (the average itself).
	 */
	public abstract double getAverage();


	/** Counts a single example, e.g. by summing up errors. */
	public abstract void countExample(Example example);


	/** Initializes the criterion. The default implementation does nothing. */
	public void startCounting(IExampleSet set)  {}

	public static String toString(List<AbstractPerformanceCounter> list) {
		StringBuffer result = new StringBuffer(Tools.getLineSeparator() + "Performance [");
		for (AbstractPerformanceCounter mp :list) {
			result.append(Tools.getLineSeparator() + "-----");
			result.append(mp.getName()+": "+mp.getAverage());
		}
		result.append(Tools.getLineSeparator() + "]");
		return result.toString();
	}
}
