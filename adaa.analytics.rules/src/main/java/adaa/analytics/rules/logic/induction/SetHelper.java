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

import java.util.Set;

/**
 * Auxiliary class for handling sets.
 *
 * @author Adam Gudys
 */
public class SetHelper {
	/**
	 * Calculates size of intersection of two sets.
	 *
	 * @param A First set.
	 * @param B Second set.
	 * @return Intersection size.
	 */
	public static <T> int intersectionSize(Set<T> A, Set<T> B) {
		int size = 0;
		for (T a: A) {
			if (B.contains(a)) {
				++size;
			}
		}
		return size;
	}
	
	/**
	 * Calculates size of difference of two sets.
	 * 
	 * @param A First set.
	 * @param B Second set.
	 * @return Difference size.
	 */
	public static <T> int differenceSize(Set<T> A, Set<T> B) {
		int size = 0;
		for (T a: A) {
			if (!B.contains(a)) {
				++size;
			}
		}
		return size;
	}
}
