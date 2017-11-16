package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;

public class InductionParameters {
	
	private IQualityMeasure inductionMeasure = new ClassificationMeasure(ClassificationMeasure.Correlation);
	private IQualityMeasure pruningMeasure = new ClassificationMeasure(ClassificationMeasure.Correlation);
	private double minimumCovered = 5.0;
	private double maximumUncoveredFraction = 0.01;
	private boolean ignoreMissing = false;
	private boolean pruningEnabled = true;
	private double maxGrowingConditions = 0;
	
	public IQualityMeasure getInductionMeasure() {return inductionMeasure;}
	public void setInductionMeasure(IQualityMeasure inductionMeasure) {this.inductionMeasure = inductionMeasure;}

	public IQualityMeasure getPruningMeasure() {return pruningMeasure;}
	public void setPruningMeasure(IQualityMeasure pruningMeasure) {this.pruningMeasure = pruningMeasure;}

	public double getMinimumCovered() {return minimumCovered;}
	public void setMinimumCovered(double minimumCovered) {this.minimumCovered = minimumCovered;}
	
	public double getMaximumUncoveredFraction() {return maximumUncoveredFraction;}
	public void setMaximumUncoveredFraction(double v) {this.maximumUncoveredFraction = v;}

	public boolean isIgnoreMissing() {return ignoreMissing;}
	public void setIgnoreMissing(boolean ignoreMissing) {this.ignoreMissing = ignoreMissing;}

	public boolean isPruningEnabled() {return pruningEnabled;}
	public void setEnablePruning(boolean enablePruning) {this.pruningEnabled = enablePruning;}
	
	public double getMaxGrowingConditions() { return maxGrowingConditions; }
	public void setMaxGrowingConditions(double maxGrowingConditions) { this.maxGrowingConditions = maxGrowingConditions; }
}
