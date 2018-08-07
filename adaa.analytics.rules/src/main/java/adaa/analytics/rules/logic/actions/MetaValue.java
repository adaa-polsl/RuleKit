package adaa.analytics.rules.logic.actions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import adaa.analytics.rules.logic.actions.ActionRangeDistribution.DistributionEntry;
import adaa.analytics.rules.logic.representation.IValueSet;

public class MetaValue {

	public String attribute;
	public IValueSet value;
	public DistributionEntry distribution;

	public MetaValue(IValueSet v, DistributionEntry d, String attr) {
		value = v;
		distribution = d;
		attribute = attr;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(19, 21)
				.append(attribute)
				.append(value)
				.append(distribution)
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		MetaValue mv = (MetaValue) obj;
		return new EqualsBuilder()
				.append(attribute, mv.attribute)
				.append(value, mv.value)
				.append(distribution, mv.distribution)
				.isEquals();
	}
}
