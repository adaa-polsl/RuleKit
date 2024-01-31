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
package adaa.analytics.rules.logic.performance.simple;

import adaa.analytics.rules.logic.performance.simple.SimpleCriterion;
import com.rapidminer.tools.Tools;


/**
 * The average relative error in a lenient way of calculation: <i>Sum(|label-predicted|/max(|label|,
 * |predicted|))/#examples</i>. The relative error of label 0 and prediction 0 is defined as 0.
 * 
 * @author Ingo Mierswa
 */
public class LenientRelativeError extends SimpleCriterion {


	public LenientRelativeError() {}


	@Override
	public double countExample(double label, double predictedLabel) {
		double diff = Math.abs(label - predictedLabel);
		double absLabel = Math.abs(label);
		double absPrediction = Math.abs(predictedLabel);
		if (Tools.isZero(diff)) {
			return 0.0d;
		} else {
			return diff / Math.max(absLabel, absPrediction);
		}
	}

	@Override
	public String getName() {
		return "relative_error_lenient";
	}

}
