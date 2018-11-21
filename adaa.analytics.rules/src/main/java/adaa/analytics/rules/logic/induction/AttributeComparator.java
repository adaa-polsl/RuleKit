package adaa.analytics.rules.logic.induction;

import com.rapidminer.example.Attribute;

import java.util.Comparator;

/**
 * Auxilliary class that compares attributes w.r.t. their ordering in the dataset.
 * 
 * @author Adam Gudyœ
 *
 */
public class AttributeComparator implements Comparator<Attribute>{
	/**
	 * Compares two attributes w.r.t. their ordering in the dataset.
	 * 
	 * @param a First attribute.
	 * @param b Second attribute.
	 * @return Comparison result.
	 */
	public int compare(Attribute a, Attribute b) {
        return Integer.compare(a.getTableIndex(), b.getTableIndex()); 
     }
}
