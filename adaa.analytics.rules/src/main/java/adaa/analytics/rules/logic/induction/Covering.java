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

import java.util.HashSet;
import java.util.Set;

public class Covering extends ContingencyTable{
	
	public Set<Integer> positives = new HashSet<Integer>();
	public Set<Integer> negatives = new HashSet<Integer>();
	
	public Covering() {}
	
	public Covering(double p, double n, double P, double N) {
		super(p, n, P, N);
	}

	public int getSize() { return positives.size() + negatives.size(); }

}
