package adaa.analytics.rules.logic.actions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import adaa.analytics.rules.logic.actions.ActionRangeDistribution.DistributionEntry;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.IValueSet;

public class MetaValue {

	public ElementaryCondition value;
	public DistributionEntry distribution;

	public MetaValue(ElementaryCondition v, DistributionEntry d) {
		value = v;
		distribution = d;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(19, 31)
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
				.append(value, mv.value)
				.append(distribution, mv.distribution)
				.isEquals();
	}
}
