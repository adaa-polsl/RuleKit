package adaa.analytics.rules.logic.induction;

import com.rapidminer.example.Attribute;

import java.util.Comparator;

public class AttributeComparator implements Comparator<Attribute>{
	 public int compare(Attribute a, Attribute b) {
        return Integer.compare(a.getTableIndex(), b.getTableIndex()); 
     }
}
