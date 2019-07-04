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

import com.rapidminer.example.Attribute;

import java.util.Comparator;

/**
 * Auxilliary class that compares attributes w.r.t. their ordering in the dataset.
 * 
 * @author Adam Gudys
 *
 */
public class AttributeComparator implements Comparator<Attribute>{
	/**
	 * Compares two attributes w.r.t. their ordering in the dataset.
	 * 
	 * @param a First attribute.
	 * @param b Second attribute.
	 * @return Comparison result.
	 */
	public int compare(Attribute a, Attribute b) {
        return Integer.compare(a.getTableIndex(), b.getTableIndex()); 
     }
}
