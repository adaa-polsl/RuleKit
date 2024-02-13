/**
 * Copyright (C) 2001-2019 by RapidMiner and the contributors
 * 
 * Complete list of developers available at our web site:
 * 
 * http://rapidminer.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
*/
package adaa.analytics.rules.logic.representation;


import adaa.analytics.rules.rm.example.AttributeRole;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.*;


/**
 * This class stores detailed meta data information about ExampleSets.
 *
 * @author Simon Fischer, Sebastian Land
 */
public class ExampleSetMetaData {

	private Map<String, AttributeMetaData> attributeMetaData = new LinkedHashMap<String, AttributeMetaData>();


	public ExampleSetMetaData(IExampleSet exampleSet) {

		int maxNumber = Integer.MAX_VALUE;


			try {
				exampleSet.recalculateAllAttributeStatistics();
			} catch (UnsupportedOperationException e) {
				// May not be supported by HeaderExampleSet
			}

		Iterator<AttributeRole> i = exampleSet.getAttributes().allAttributeRoles();
		while (i.hasNext()) {
			AttributeRole role = i.next();
			addAttribute(new AttributeMetaData(role));
			maxNumber--;
			if (maxNumber == 0) {
				break;
			}
		}
	}

	public AttributeMetaData getAttributeByName(String name) {
		return attributeMetaData.get(name);
	}

	public AttributeMetaData getAttributeByRole(String role) {
		for (AttributeMetaData amd : attributeMetaData.values()) {
			String currentRole = amd.getRole();
			if (currentRole != null && currentRole.equals(role)) {
				return amd;
			}
		}
		return null;
	}

	public void addAttribute(AttributeMetaData attribute) {
		if (attributeMetaData == null) {
			attributeMetaData = new LinkedHashMap<String, AttributeMetaData>();
		}
		// registering this exampleSetmetaData as owner of the attribute.
		attribute = attribute.registerOwner(this);
		attributeMetaData.put(attribute.getName(), attribute);
	}


	public AttributeMetaData getSpecial(String role) {
		if (attributeMetaData != null) {
			for (AttributeMetaData amd : attributeMetaData.values()) {
				if (role.equals(amd.getRole())) {
					return amd;
				}
			}
		}
		return null;
	}

	public AttributeMetaData getLabelMetaData() {
		return getSpecial(IAttributes.LABEL_NAME);
	}

}
