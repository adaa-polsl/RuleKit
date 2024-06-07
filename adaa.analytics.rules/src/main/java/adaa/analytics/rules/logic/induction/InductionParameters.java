/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.induction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;

/**
 * Class representing all parameters of rule induction algorithm.
 *
 * @author Adam Gudys
 */
public class InductionParameters implements Serializable {

	/** Serialization identifier. */
	private static final long serialVersionUID = -7902085678266232822L;

	/** Quality measure used for induction. */
	private IQualityMeasure inductionMeasure = new ClassificationMeasure(ClassificationMeasure.Correlation);

	/** Quality measure used for pruning. */
	private IQualityMeasure pruningMeasure = new ClassificationMeasure(ClassificationMeasure.Correlation);

	/** Quality measure used for voting. */
	private IQualityMeasure votingMeasure = new ClassificationMeasure(ClassificationMeasure.Correlation);

	/** Minimum number of previously uncovered examples that a new rule has to cover. */
	private double minimumCovered = 0.05;
	private boolean adjustMinimumCovered = true;
	private double minimumCoveredAll = 0.0;
	private int maxRuleCount = 0;
	private double maximumUncoveredFraction = 0;
	private boolean conditionComplementEnabled = false;

	private boolean ignoreMissing = false;
	private boolean pruningEnabled = true;
	private double maxGrowingConditions = 0;
	private boolean selectBestCandidate = false;
	private double maxcovNegative = Double.MAX_VALUE;

	private double penaltyStrength = 0;
	private double penaltySaturation = 0;
	private boolean binaryContrastIncluded = false;

	private List<Double> minimumCoveredAll_list = new ArrayList<Double>();

	private int maxPassesCount = 1;
	private boolean meanBasedRegression = true;
	private boolean controlAprioriPrecision = true;

	private boolean approximateInduction = false;
	private int approximateBinsCount = 100;

	public IQualityMeasure getInductionMeasure() {return inductionMeasure;}
	public void setInductionMeasure(IQualityMeasure inductionMeasure) {this.inductionMeasure = inductionMeasure;}

	public IQualityMeasure getPruningMeasure() {return pruningMeasure;}
	public void setPruningMeasure(IQualityMeasure pruningMeasure) {this.pruningMeasure = pruningMeasure;}

	public IQualityMeasure getVotingMeasure() {return votingMeasure;}
	public void setVotingMeasure(IQualityMeasure pruningMeasure) {this.votingMeasure = pruningMeasure;}

	public double getMinimumCovered() {return minimumCovered;}
	public double getAbsoluteMinimumCovered(double size) {
		return Math.max(1, minimumCovered * (minimumCovered >= 1 ? 1 : size));
	}
	public void setMinimumCovered(double minimumCovered) {this.minimumCovered = minimumCovered;}
	public boolean isAdjustMinimumCovered() { return adjustMinimumCovered; }
	public void setAdjustMinimumCovered(boolean v) { adjustMinimumCovered = v; }

	public int getMaxRuleCount() {
		return maxRuleCount;
	}

	public void setMaxRuleCount(int maxRuleCount) {
		this.maxRuleCount = maxRuleCount;
	}

	public double getMinimumCoveredAll() {return minimumCoveredAll;}
	public double getAbsoluteMinimumCoveredAll(double size) { return minimumCoveredAll * (minimumCoveredAll >= 1 ? 1 : size); }
	public void setMinimumCoveredAll(double minimumCoveredAll) {this.minimumCoveredAll = minimumCoveredAll;}
	
	public double getMaximumUncoveredFraction() {return maximumUncoveredFraction;}
	public void setMaximumUncoveredFraction(double v) {this.maximumUncoveredFraction = v;}

	public boolean isIgnoreMissing() {return ignoreMissing;}
	public void setIgnoreMissing(boolean ignoreMissing) {this.ignoreMissing = ignoreMissing;}

	public boolean isPruningEnabled() {return pruningEnabled;}
	public void setEnablePruning(boolean enablePruning) {this.pruningEnabled = enablePruning;}
	
	public double getMaxGrowingConditions() { return maxGrowingConditions; }
	public void setMaxGrowingConditions(double maxGrowingConditions) { this.maxGrowingConditions = maxGrowingConditions; }

	public boolean getSelectBestCandidate() { return selectBestCandidate; }
	public void setSelectBestCandidate(boolean selectBestCandidate) { this.selectBestCandidate = selectBestCandidate; }

	public double getMaxcovNegative() {return maxcovNegative;}
	public void setMaxcovNegative(double v) {this.maxcovNegative = v;}

	public double getPenaltyStrength() {return penaltyStrength;}
	public void setPenaltyStrength(double v) {this.penaltyStrength = v;}

	public double getPenaltySaturation() {return penaltySaturation;}
	public void setPenaltySaturation(double v) {this.penaltySaturation = v;}

	public boolean isBinaryContrastIncluded() {return binaryContrastIncluded;}
	public void setBinaryContrastIncluded(boolean v) {this.binaryContrastIncluded = v;}

	public int getMaxPassesCount() { return maxPassesCount; }
	public void setMaxPassesCount(int maxCoverageCount) {  this.maxPassesCount = maxCoverageCount; }

	public boolean isConditionComplementEnabled() { return conditionComplementEnabled; }
	public void setConditionComplementEnabled(boolean value) { this.conditionComplementEnabled = value; }

	public boolean isMeanBasedRegression() { return meanBasedRegression; }
	public void setMeanBasedRegression(boolean value) { this.meanBasedRegression = value; }

	public boolean isControlAprioriPrecision() { return controlAprioriPrecision; }
	public void setControlAprioriPrecision(boolean v) { controlAprioriPrecision = v; }

	public boolean isApproximateInduction() { return approximateInduction; }
	public void setApproximateInduction(boolean v) { approximateInduction = v; }

	public int getApproximateBinsCount() { return approximateBinsCount; }
	public void setApproximateBinsCount(int v) { approximateBinsCount = v; }

	public List<Double> getMinimumCoveredAll_list() { return minimumCoveredAll_list; }
	public void setMinimumCoveredAll_list(List<Double> minimumCovered) {this.minimumCoveredAll_list.addAll(minimumCovered);}


	public String toString() {

		String mincov_all_desc = "minsupp_all=";
		if (minimumCoveredAll_list.size() > 0) {
			mincov_all_desc += minimumCoveredAll_list;
		} else {
			mincov_all_desc += minimumCoveredAll;
		}


		return "minsupp_new=" + minimumCovered + "\n" +
				"adjust_minsupp_new=" + adjustMinimumCovered + "\n" +
				mincov_all_desc + "\n" +
				"max_rule_count=" + maxRuleCount + "\n" +
				"max_neg2pos=" + (maxcovNegative == Double.MAX_VALUE ? "OFF" : maxcovNegative) + "\n" +
				"max_uncovered_fraction=" + maximumUncoveredFraction + "\n" +
				"induction_measure=" + inductionMeasure.getName() + "\n" +
				"pruning_measure=" + (pruningEnabled ? pruningMeasure.getName() : "OFF") + "\n" +
				"voting_measure=" + votingMeasure.getName() + "\n" +
				"penalty_strength=" + penaltyStrength + "\n" +
				"penalty_saturation=" + penaltySaturation + "\n" +
				"select_best_candidate=" + selectBestCandidate + "\n" +
				"max_passes_count=" + maxPassesCount + "\n" +
				"complementary_conditions=" + conditionComplementEnabled + "\n" +
				"approximate_induction=" + approximateInduction + "\n" +
				"approximate_bins_count=" + (approximateInduction ? approximateBinsCount : "OFF") + "\n";

	}

}
