package adaa.analytics.rules.logic.representation;


import adaa.analytics.rules.rm.example.AttributeRole;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.*;


/**
 * This class stores detailed meta data information about ExampleSets.
 *
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
