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

package adaa.analytics.rules.logic.representation;

import java.util.ArrayList;
import java.util.List;

public class AnyValueSet implements IValueSet {

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
		return "ANY";
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public List<IValueSet> getDifference(IValueSet set) {
		List<IValueSet> ret = new ArrayList<IValueSet>();
		return ret; //or throw ?
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof AnyValueSet) {
			return true;
		}
		return false;
	}

}
