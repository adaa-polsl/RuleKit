package adaa.analytics.rules.logic.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class MetaExample {
	Map<String, MetaValue> data;
	
	public MetaExample() {
		data = new HashMap<String,MetaValue>();
	}
	
	public MetaExample(Map<String, MetaValue> inData) {
		data = inData;
	}
	
	public void add(String attribute, MetaValue value) {
		data.put(attribute, value);
	}
	
	public MetaValue get(String attribute) {
		if (data.containsKey(attribute)) {
			return data.get(attribute);
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(
				data.values()
				.stream()
				.map(x->x.value.toString())
				.collect(Collectors.joining(";"))
				);
		
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder(13,19);
		b.append(data);
		return b.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		 if (obj == null) { return false; }
		 if (obj == this) { return true; }
		 if (obj.getClass() != getClass()) {
		     return false;
		 }
		 MetaExample me = (MetaExample)obj;
		 return new EqualsBuilder()
				 .append(data, me.data)
				 .isEquals();
		 
	}
}
