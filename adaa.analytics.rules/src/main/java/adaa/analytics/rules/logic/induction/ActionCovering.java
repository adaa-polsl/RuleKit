package adaa.analytics.rules.logic.induction;

public class ActionCovering extends Covering {

	public double weighted_pRight = 0.0;
	public double weighted_nRight = 0.0;
	public double weighted_P_right = 0.0;
	public double weighted_N_right = 0.0;

	public double median_y_right = 0.0;
	public double mean_y_right = 0.0;
	public double stddev_y_right = 0.0;

	public ActionCovering() {
	}

	public ActionCovering(Covering source, Covering target) {
		this.positives = source.positives;
		this.negatives = source.negatives;
		this.weighted_p = source.weighted_p;
		this.weighted_n = source.weighted_n;
		this.weighted_P = source.weighted_P;
		this.weighted_N = source.weighted_N;

		this.weighted_pRight = target.weighted_p;
		this.weighted_nRight = target.weighted_n;
		this.weighted_P_right = target.weighted_P; //source.weightedN
		this.weighted_N_right = target.weighted_N;
	}
}
