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
package adaa.analytics.rules.logic.representation.valueset;

import java.io.Serializable;
import java.util.List;

public class Universum implements IValueSet, Serializable {

	private static final long serialVersionUID = -1067103164849784013L;

	@Override
	public boolean contains(double value) {
		return true;
	}

	@Override
	public boolean intersects(IValueSet set) {
		return true;
	}

	@Override
	public IValueSet getIntersection(IValueSet set) {
		return set;
	}
	
	@Override 
	public String toString() {
		return "Any";
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Universum);
	}

	@Override
	public List<IValueSet> getDifference(IValueSet set) {
		// TODO Auto-generated method stub
		return null;
	}
}
