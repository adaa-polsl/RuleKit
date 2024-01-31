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

/**
 * Computes the square of the empirical corellation coefficient 'r' between label and prediction.
 * Eith P=prediction, L=label, V=Variance, Cov=Covariance we calculate r by: <br>
 * Cov(L,P) / sqrt(V(L)*V(P)). Uses the calculation of the superclass.
 * 
 * @author Ingo Mierswa ingomierswa Exp $
 */
public class SquaredCorrelationCriterion extends CorrelationCriterion {


	@Override
	public double getMikroAverage() {
		double r = super.getMikroAverage();
		return r * r;
	}

	@Override
	public String getName() {
		return "squared_correlation";
	}
}
