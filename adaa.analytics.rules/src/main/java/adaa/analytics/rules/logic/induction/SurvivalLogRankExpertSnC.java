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

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;

import java.util.Set;
import java.util.logging.Level;

/**
 *  Separate'n'conquer algorithm for generating log rank-based survival rule sets with user's knowledge.
 * @author Adam Gudys
 *
 */
public class SurvivalLogRankExpertSnC extends RegressionExpertSnC {

	public SurvivalLogRankExpertSnC(RegressionFinder finder,
			InductionParameters params, Knowledge knowledge) {
		super(finder, params, knowledge);
		this.factory = new RuleFactory(RuleFactory.SURVIVAL, true, params, knowledge);
	}

}
