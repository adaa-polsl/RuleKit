package adaa.analytics.rules.logic.induction;

import java.util.Comparator;

import com.rapidminer.example.Attribute;

public class AttributeComparator implements Comparator<Attribute>{
	 public int compare(Attribute a, Attribute b) {
        return Integer.compare(a.getTableIndex(), b.getTableIndex()); 
     }
}
