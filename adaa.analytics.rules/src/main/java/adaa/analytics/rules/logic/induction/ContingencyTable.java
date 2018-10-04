package adaa.analytics.rules.logic.induction;

public class ContingencyTable {
	public double weighted_p = 0;
	public double weighted_n = 0; 
	public double weighted_P = 0;
	public double weighted_N = 0;
	
	public double median_y = 0;
	public double mean_y = 0;
	public double stddev_y = 0;
	
	public ContingencyTable() {	}
	
	public ContingencyTable(double p, double n, double P, double N) {
		this.weighted_p = p;
		this.weighted_n = n;
		this.weighted_P = P;
		this.weighted_N = N;
	}
	
	
}
