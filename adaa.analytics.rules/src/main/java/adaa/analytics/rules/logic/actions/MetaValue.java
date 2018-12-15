package adaa.analytics.rules.logic.actions;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.IValueSet;
import adaa.analytics.rules.logic.representation.Interval;

public class MetaValue implements IValueSet {
	
	public static MetaValue EMPTY = new MetaValue(new ElementaryCondition("empty", new Interval()), new DistributionEntry());

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

	@Override
	public boolean contains(double value) {
		return this.value.getValueSet().contains(value);
	}

	@Override
	public boolean intersects(IValueSet set) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IValueSet getIntersection(IValueSet set) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IValueSet> getDifference(IValueSet set) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
