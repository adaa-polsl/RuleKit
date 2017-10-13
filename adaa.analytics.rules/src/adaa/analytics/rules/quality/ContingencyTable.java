package adaa.analytics.rules.quality;

public class ContingencyTable {
	public double p;
	public double n;
	public double P;
	public double N;
	
	public double total() { return P + N; }
	
	public ContingencyTable(double p, double n, double P, double N) {
		this.p = p;
		this.n = n;
		this.P = P;
		this.N = N;
	}
	
	
}
