package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.Set;

public class Covering extends ContingencyTable{
	
	public Set<Integer> positives = new HashSet<Integer>();
	public Set<Integer> negatives = new HashSet<Integer>();
	
	public Covering() {}
	
	public Covering(double p, double n, double P, double N) {
		super(p, n, P, N);
	}

	public int getSize() { return positives.size() + negatives.size(); }

}
