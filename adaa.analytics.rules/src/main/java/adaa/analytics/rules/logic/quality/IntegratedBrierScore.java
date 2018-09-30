package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.logic.representation.SurvivalRuleSet;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.MeasuredPerformance;
import com.rapidminer.tools.math.Averagable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IntegratedBrierScore extends MeasuredPerformance {

	private static final long serialVersionUID = 8509874136756469476L;
	
	protected double score;
	 
	 public IntegratedBrierScore() {
		 
	 }
	 
	 @Override
	 public void startCounting(ExampleSet testSet, boolean useExampleWeights) throws OperatorException {
		 	 
		Attribute survTime = testSet.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE); 
		Attribute survStat = testSet.getAttributes().getSpecial(SurvivalRule.SURVIVAL_STATUS_ROLE);
		if (survStat == null) {
			survStat = testSet.getAttributes().getLabel();
		}
	    	
		List<SurvInfo> info = new ArrayList<SurvInfo>();	
		for (int i = 0; i < testSet.size(); i++) {
			Example e = testSet.getExample(i);
			double t = e.getValue(survTime);
			boolean isCensored = e.getValue(survStat) == 0;
			
			String textKaplan = e.getValueAsString(e.getAttributes().getSpecial(SurvivalRuleSet.ATTRIBUTE_ESTIMATOR));
			KaplanMeierEstimator kaplan = new KaplanMeierEstimator();
			kaplan.load(textKaplan);
		
			info.add(new SurvInfo(t, isCensored, kaplan));
		}
		
		info.sort(new Comparator<SurvInfo>() {
			@Override
			public int compare(SurvInfo x, SurvInfo y) {
	                return Double.compare(x.getTime(), y.getTime());
			}
		});
	
	 // get KM from training set
		String textKM = testSet.getAnnotations().getAnnotation(SurvivalRuleSet.ANNOTATION_TRAINING_ESTIMATOR_REV);
		KaplanMeierEstimator censoringKM = new KaplanMeierEstimator();
		censoringKM.load(textKM);
		
	    List<Double> brierScores = new ArrayList<Double>();
	    
	    // use all surv infos
	    for (int i = 0; i < info.size(); i++) {
	        
	    	double bt = info.get(i).getTime();
	        if (i > 0 && bt == info.get(i - 1).getTime()) {
	            brierScores.add(info.get(i - 1).getTime());
	        } else {
	        	double brierSum = 0.0;
	            for (SurvInfo si : info) {
	            	if (si.getTime() <= bt && si.getIsCensored() == false) {
	 	            	double g = censoringKM.getProbabilityAt(si.getTime());
	                    if (g > 0) {
	                        double p = si.getEstimator().getProbabilityAt(bt);
	                        brierSum += (p * p) / g;
	                    }
	                } else if (si.getTime() > bt) {
	                    double g = censoringKM.getProbabilityAt(bt);
	                    if (g > 0) {
	                        double p = 1.0 - si.getEstimator().getProbabilityAt(bt);
	                        brierSum += (p * p) / g;                               
	                    }
	                }
	
	                assert(!Double.isNaN(brierSum));
	                assert(!Double.isInfinite(brierSum));
	            }
	
	            brierScores.add(brierSum / info.size());
	        }
	 	}
	 	
	    List<Double> diffs = new ArrayList<Double>();
	    diffs.add(info.get(0).getTime());
	    
	    for (int i = 1; i < info.size(); i++) {
	        diffs.add(info.get(i).getTime() - info.get(i - 1).getTime());
	    }
	
	    double sum = 0.0;
	    for (int i = 0; i < info.size(); i++) {
	        sum += diffs.get(i) * brierScores.get(i);
	    }
	
	    score = sum / info.get(info.size() - 1).getTime();
	     
	    assert(!Double.isNaN(score));
	    assert(!Double.isInfinite(score));
	}
	 
	
	@Override
	public void countExample(Example example) {
		// do nothing..
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getExampleCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getFitness() {
		return getAverage();
	}

	@Override
	public String getName() {
		return "integrated_brier_score";
	}

	@Override
	public double getMikroAverage() {
		return score;
	}

	@Override
	public double getMikroVariance() {
		return Double.NaN;
	}

	@Override
	protected void buildSingleAverage(Averagable averagable) {
		// TODO Auto-generated method stub
		
	}
	
	private class SurvInfo {
        
		protected double time;
		protected boolean isCensored;
		protected KaplanMeierEstimator estimator;
		
		public double getTime() { return time; }
		public void setTime(double v) { time = v; } 
		
		public boolean getIsCensored() { return isCensored; }
		public void setIsCensored(boolean v) { isCensored = v; } 
		
		public KaplanMeierEstimator getEstimator() { return estimator; }
		public void setEstimator(KaplanMeierEstimator v) { estimator = v; } 
		
		public SurvInfo(double time, boolean isCensored, KaplanMeierEstimator km) {
            this.time = time;
            this.isCensored = isCensored;
            this.estimator = km;
        }
    }

}
