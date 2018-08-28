package adaa.analytics.rules.logic.quality;

import com.rapidminer.operator.performance.BinaryClassificationPerformance;

public class ExtendedBinaryPerformance extends BinaryClassificationPerformance{

	private static final long serialVersionUID = 791171007065379124L;

	private static final int N = 0;

	private static final int P = 1;

	@Override
	public double getMikroAverage() {
		double x = 0.0d, y = 0.0d;
		
		double[][] counter = getCounter();	
		
		x = counter[P][P];
		y = counter[P][P] + counter[P][N];
		
		if (y == 0) { 
			return Double.NaN; 
		}		
		
		double se = x / y;
				
		x = counter[N][N];
		y = counter[N][N] + counter[N][P];
		
		if (y == 0) { 
			return Double.NaN; 
		}	
		
		double sp = x / y;
		
		return Math.sqrt(se * sp);
			
	}
	
	@Override
	public String getName() {
		return "geometric_mean";
	}

	@Override
	public String getDescription() {
		return "Geometric mean of sensitivity ans specificity";
	} 
	
}
