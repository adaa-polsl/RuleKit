package adaa.analytics.rules.logic.actions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.Rule;

public class MetaValue /*implements IValueSet*/ {
	
	public static MetaValue EMPTY = new MetaValue(new ElementaryCondition("empty", new Interval()), new DistributionEntry());

	private ElementaryCondition value;
	private DistributionEntry distribution;

	public MetaValue(ElementaryCondition v, DistributionEntry d) {
		value = v;
		distribution = d;
	}

	public MetaValue(MetaValue rhs){
		this.value = new ElementaryCondition(rhs.value.getAttribute(), rhs.value.getValueSet());
		this.distribution = new DistributionEntry(rhs.distribution);

	}


    public Optional<List<Rule>> getSupportingRulesPointingToClass(double classId){
		return distribution.getRulesOfClass(classId);
	}

	public Optional<List<Rule>> getSupportingRulesNotOfClass(double classId){
		Stream<Rule> rules = getSupportingRules();
		return Optional.of(rules.filter(x -> !x.getConsequence().getValueSet().contains(classId)).collect(Collectors.toList()));
	}
	
	public Stream<Rule> getSupportingRules(){
		return distribution.getAllRules();
	}

	public ElementaryCondition getValue() {
		return value;
	}
	
	public String getAttribute() {
		return value.getAttribute();
	}
	
	public String toValueString() {
		return value.toString();
	}
	
	@Override
	public String toString() {
		return value.toString() + "[" + distribution.distribution.toString() + "]";
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

	public boolean contains(Example example) {
		Attribute commonAttribute = example.getAttributes().get(getAttribute());
		if (commonAttribute == null) {
			return false;
		}
		
		double attributeValue = example.getValue(commonAttribute);
		
		return value.getValueSet().contains(attributeValue);
	}	
}
