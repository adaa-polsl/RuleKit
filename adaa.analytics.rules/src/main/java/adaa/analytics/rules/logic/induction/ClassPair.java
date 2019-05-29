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

import com.rapidminer.example.table.NominalMapping;

public class ClassPair {

	protected String sourceLabel;
	protected String targetLabel;
	protected int sourceId;
	protected int targetId;
	
	public ClassPair(String source, String target, NominalMapping mapping) {
		sourceLabel = source;
		targetLabel = target;
		sourceId = mapping.getIndex(source);
		targetId = mapping.getIndex(target);
	}
	
	public String getSourceLabel() {
		return sourceLabel;
	}
	
	public String getTargetLabel() {
		return targetLabel;
	}

	public int getSourceId() {
		return sourceId;
	}
	
	public int getTargetId() {
		return targetId;
	}
}
