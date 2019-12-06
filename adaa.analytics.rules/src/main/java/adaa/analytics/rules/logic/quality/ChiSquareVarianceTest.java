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

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import com.rapidminer.tools.container.Pair;

/**
 * Class representing Chi-square test for variance.
 *
 * @author Adam Gudys
 */
public class ChiSquareVarianceTest extends StatisticalTest {
	
	public Pair<Double,Double> calculateLower(double expectedDev, double sampleDev, int samplesize) {
		double factor = sampleDev / expectedDev;
		double T = ((double)(samplesize - 1)) * (factor * factor); 
	
		ChiSquaredDistribution chi = new ChiSquaredDistribution(samplesize - 1);
		
		return new Pair<Double, Double>(T, chi.cumulativeProbability(T)) ;
	}
}
