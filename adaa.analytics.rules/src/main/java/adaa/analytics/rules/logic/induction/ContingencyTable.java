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

/**
 *  Class representing contingency table. For regression rules it also stores mean, median and standard deviation of
 *  conditional attribute value of covered examples.
 *
 * @author Adam Gudys
 */
public class ContingencyTable {
	public double weighted_p = 0;
	public double weighted_n = 0; 
	public double weighted_P = 0;
	public double weighted_N = 0;
	
	public double median_y = 0;
	public double mean_y = 0;
	public double stddev_y = 0;
	
	public ContingencyTable() {	}
	
	public ContingencyTable(double p, double n, double P, double N) {
		this.weighted_p = p;
		this.weighted_n = n;
		this.weighted_P = P;
		this.weighted_N = N;
	}

	public void clear() {
		weighted_p = weighted_n = weighted_P = weighted_N = 0;
		mean_y = median_y = stddev_y = 0;
	}
	
}
