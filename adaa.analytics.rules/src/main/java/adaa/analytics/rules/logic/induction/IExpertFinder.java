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

import adaa.analytics.rules.logic.representation.Rule;
import com.rapidminer.example.ExampleSet;

import java.util.Set;

/**
 * Interface to be implemented by all classes representing user-guided rule induction procedures.
 *
 * @author Adam Gudys
 */
public interface IExpertFinder {
	public void adjust(
			Rule rule,
			ExampleSet dataset, 
			Set<Integer> uncovered);
}
