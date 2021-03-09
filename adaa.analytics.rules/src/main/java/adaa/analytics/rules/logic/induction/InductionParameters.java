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
	/** Minimum number of previously uncovered examples that a new rule has to cover. */
	private double minimumCovered = 5.0;
	private double minimumCoveredAll = 0.0;
	private double maximumUncoveredFraction = 0;

	private boolean ignoreMissing = false;
	private boolean pruningEnabled = true;
	private double maxGrowingConditions = 0;
	private boolean selectBestCandidate = false;

	public IQualityMeasure getInductionMeasure() {return inductionMeasure;}
	public void setInductionMeasure(IQualityMeasure inductionMeasure) {this.inductionMeasure = inductionMeasure;}

	public IQualityMeasure getPruningMeasure() {return pruningMeasure;}
	public void setPruningMeasure(IQualityMeasure pruningMeasure) {this.pruningMeasure = pruningMeasure;}

	public IQualityMeasure getVotingMeasure() {return votingMeasure;}
	public void setVotingMeasure(IQualityMeasure pruningMeasure) {this.votingMeasure = pruningMeasure;}

	public double getMinimumCovered() {return minimumCovered;}
	public double getAbsoluteMinimumCovered(double size) { return minimumCovered * (minimumCovered >= 1 ? 1 : size); }
	public void setMinimumCovered(double minimumCovered) {this.minimumCovered = minimumCovered;}

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

	public String toString() {
		return	
			"min_rule_covered=" + minimumCovered + "\n" +
			"induction_measure=" + inductionMeasure.getName() + "\n" +
			"pruning_measure=" + pruningMeasure.getName() + "\n" +
			"voting_measure=" + votingMeasure.getName() + "\n";

	}
}
