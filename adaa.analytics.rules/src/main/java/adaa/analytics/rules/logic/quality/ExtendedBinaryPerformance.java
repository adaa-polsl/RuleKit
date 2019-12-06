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

import com.rapidminer.operator.performance.BinaryClassificationPerformance;

/**
 * Class gathering additional performance measures for binary classification models (e.g., geometric mean of sensitivity
 * and specificity).
 *
 * @author Adam Gudys
 */
public class ExtendedBinaryPerformance extends BinaryClassificationPerformance{

	private static final long serialVersionUID = 791171007065379124L;

	private static final int N = 0;

	private static final int P = 1;

	@Override
	public double getMikroAverage() {
		double x = 0.0d, y = 0.0d;
		
		double[][] counter = getCounter();	
		
		x = counter[P][P];
		y = counter[P][P] + counter[P][N];
		
		if (y == 0) { 
			return Double.NaN; 
		}		
		
		double se = x / y;
				
		x = counter[N][N];
		y = counter[N][N] + counter[N][P];
		
		if (y == 0) { 
			return Double.NaN; 
		}	
		
		double sp = x / y;
		
		return Math.sqrt(se * sp);
			
	}
	
	@Override
	public String getName() {
		return "geometric_mean";
	}

	@Override
	public String getDescription() {
		return "Geometric mean of sensitivity ans specificity";
	} 
	
}
