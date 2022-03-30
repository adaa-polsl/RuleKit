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
import adaa.analytics.rules.logic.representation.ConditionBase;

/**
 * Helper class for storing information about evaluated condition. 
 * @author Adam Gudys
 *
 */
public class ConditionEvaluation {
	public ConditionBase condition = null;
	public Covering covering = null;
	public double quality = -Double.MAX_VALUE;
	public double covered = 0;
	public boolean opposite = false;
}
