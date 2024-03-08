package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.rm.example.AttributeRole;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.tools.Ontology;

import java.util.Set;
import java.util.TreeSet;


/**
 * Meta data about an attribute
 *
 *
 */
public class AttributeMetaData {


	private ExampleSetMetaData owner = null;

	private String name;

	private int type = Ontology.ATTRIBUTE_VALUE;
	private String role = null;

	private Set<String> valueSet = new TreeSet<String>();
	private String mode;


	/**
	 * This will generate the attribute meta data with the data's values shortened if the number of
	 * values exceeds the respective property and the boolean flag is set to true. If shortened only
	 * the first 100 characters of each nominal value is returned.
	 */
	public AttributeMetaData(AttributeRole  role) {
		this.name = role.getAttribute().getName();
		this.type = role.getAttribute().getValueType();
		this.role = role.getSpecialName();

		IAttribute att = role.getAttribute();
		if (att.isNominal()) {
			int maxValues =  Integer.MAX_VALUE;
			valueSet.clear();
			for (String value : att.getMapping().getValues()) {
				if (value == null) {
					continue;
				}

				valueSet.add(value);
				maxValues--;
				if (maxValues == 0) {
					break;
				}
			}
		}
	}

	public String getRole() {
		return role;
	}

	public String getName() {
		return name;
	}




	public boolean isNominal() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.isA(type, Ontology.NOMINAL);
	}

	public boolean isNumerical() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.isA(type, Ontology.NUMERICAL);
	}


	public Set<String> getValueSet() {
		return valueSet;
	}



	/**
	 * Sets the role of this attribute. The name is equivalent with the names from Attributes. To
	 * reset use null as parameter.
	 */
	public void setRole(String role) {
		this.role = role;
	}




	/**
	 * This method is only to be used by ExampleSetMetaData to register as owner of this
	 * attributeMetaData. Returnes is this object or a clone if this object already has an owner.
	 */
	/* pp */AttributeMetaData registerOwner(ExampleSetMetaData owner) {

			this.owner = owner;
			return this;
	}
}
