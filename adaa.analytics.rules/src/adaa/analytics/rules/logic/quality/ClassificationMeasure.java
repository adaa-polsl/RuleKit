package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.induction.Covering;

public class ClassificationMeasure implements IQualityMeasure {
	
	public static final int	Accuracy = 0;
	public static final int	C2 = 1;
	public static final int	Correlation = 2;
	public static final int	Lift = 3;
	public static final int	LogicalSufficiency = 4;
	public static final int	Precision = 5;
	public static final int	RSS = 6;
	public static final int	SBayesian = 7;
	public static final int	Sensitivity = 8;
	public static final int	Specificity = 9;
	public static final int BinaryEntropy = 10;
	public static final int GeoRSS = 11;
	
	
	protected int criterion = Correlation;

	public ClassificationMeasure(int criterion) {
		this.criterion = criterion;
	}
	
	public String getName() {
		return ClassificationMeasure.getName(criterion);
	}
	
	public static String getName(int criterion) {
		switch(criterion) {
		case Accuracy: return "Accuracy";
		case C2: return "C2";
		case Correlation: return "Correlation";
		case Lift: return "Lift";
		case LogicalSufficiency: return "LogicalSufficiency";
		case Precision: return "Precision";
		case RSS: return "RSS";
		case GeoRSS: return "GeoRSS";
		case SBayesian: return "SBayesian";
		case Sensitivity: return "Sensitivity";
		case Specificity: return "Specificity";
		case BinaryEntropy: return "BinaryEntropy";
		default:
			throw new IllegalArgumentException("ClassificationMeasure: unknown measure type");
		}
	}
	 
	public double calculate(double p, double n, double P, double N) {
		switch(criterion) {
		case Accuracy:
			return (p + N - n) / (P + N);
		case C2:
			return (((P+N)*p/(p + n)-P) /N) * ((1 + p/P) / 2);
		case Correlation:
			return (p*N - P*n) / Math.sqrt(P*N*(p+n)*(P-p+N-n));
		case Lift:
			return (p + 1)*(P + N) / (p + n + 2)*P;
		case LogicalSufficiency:
			return p*N/(n*P);
		case Precision:
			return p / (p+n);
		case RSS:
			return p/P - n/N;
		case GeoRSS:
			return Math.sqrt(p/P * (1 - n/N));
		case SBayesian:
			return p / (p+n) - (P-p)/(P-p + N-n); 
		case Sensitivity:
			return p / P;
		case Specificity:
			return (N - n) / N;		
		case BinaryEntropy:
			double probs[][] = new double[2][2];
			double H[] = new double[2];
			
			probs[0][0] =  p / (p + n);		// covered
			probs[0][1] = 1 - probs[0][0];
			probs[1][0] = (P - p) / (P + N - p - n); // uncovered
			probs[1][1] = 1 - probs[1][0];
			
			H[0] = H[1] = 0;
			
			for (int i = 0; i < 2; ++i) {
				for (int j = 0; j < 2; ++j) {
					if (probs[i][j] > 0) {
						H[i] -= probs[i][j] * Math.log(probs[i][j]) / Math.log(2.0);
					}
				}
			}
			
			double coveredFrac = (p + n) / (P + N);
			double CH = coveredFrac * H[0] + (1 - coveredFrac) * H[1];	
			return 1-CH;
			
		default:
			throw new IllegalArgumentException("ClassificationMeasure: unknown measure type");
		}
	}
	
	public double calculate(Covering cov) {
		return this.calculate(cov.weighted_p, cov.weighted_n,
				cov.weighted_P, cov.weighted_N);
	}
	
	public double calculate(ContingencyTable ct) {
		return this.calculate(ct.p, ct.n, ct.P, ct.N);
	}
	
}
