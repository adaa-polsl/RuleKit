package adaa.analytics.rules.induction;

import java.util.HashSet;
import java.util.Set;

public class Covering {
	public Set<Integer> positives = new HashSet<Integer>();
	public Set<Integer> negatives = new HashSet<Integer>();
	
	public double weighted_p = 0;
	public double weighted_n = 0; 
	public double weighted_P = 0;
	public double weighted_N = 0;
	
	public double median_y = 0;
	public double mean_y = 0;
	public double stddev_y = 0;
	
	public int getSize() { return positives.size() + negatives.size(); }

}
