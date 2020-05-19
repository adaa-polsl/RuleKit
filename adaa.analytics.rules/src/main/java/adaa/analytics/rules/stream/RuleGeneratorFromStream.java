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
package adaa.analytics.rules.stream;

import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;

public class RuleGeneratorFromStream extends ExpertRuleGenerator {

	public RuleGeneratorFromStream(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}


	public static final String PARAMETER_BATCH_SIZE = "batch_size";
	
	@Override
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		Model model = null;
		
		try{
			InductionParameters params = new InductionParameters();
			params.setInductionMeasure(createMeasure(MeasureDestination.INDUCTION, new ClassificationMeasure(ClassificationMeasure.Correlation)));
			params.setPruningMeasure(createMeasure(MeasureDestination.PRUNING, params.getInductionMeasure() )); 
			params.setVotingMeasure(createMeasure(MeasureDestination.VOTING, params.getInductionMeasure()));

			params.setMaximumUncoveredFraction(getParameterAsDouble(PARAMETER_MAX_UNCOVERED_FRACTION));
			params.setMinimumCovered(getParameterAsDouble(PARAMETER_MIN_RULE_COVERED));
			params.setEnablePruning(getParameterAsBoolean(PARAMETER_ENABLE_PRUNING));
			params.setIgnoreMissing(getParameterAsBoolean(PARAMETER_IGNORE_MISSING));

			AbstractSeparateAndConquer snc; 
			
			if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
				// survival problem
			//	if (getParameterAsBoolean(PARAMETER_LOGRANK_SURVIVAL)) {
					params.setInductionMeasure(new LogRank());
					params.setPruningMeasure(new LogRank());
					params.setVotingMeasure(new LogRank());
					SurvivalLogRankFinder finder = new SurvivalLogRankFinder(params);
					snc = new SurvivalLogRankSnC(finder, params);
			//	} else {
			//		ClassificationFinder finder = new ClassificationFinder(params);
			//		snc = new SurvivalClassificationSnC(finder, params);
			//	}
			} else if (exampleSet.getAttributes().getLabel().isNumerical()) {
				// regression problem
				RegressionFinder finder = new RegressionFinder(params);
				snc = new RegressionSnC(finder, params);
			} else {
				// classification problem
				ClassificationFinder finder = new ClassificationFinder(params);
				snc = new ClassificationSnC(finder, params);
			}
			
			final int batchSize = getParameterAsInt(PARAMETER_BATCH_SIZE);
			
			//AbstractStreamAnalyzer analyzer = new ExpertStreamAnalyzer(snc, batchSize, params);
			AbstractStreamAnalyzer analyzer = new SimpleStreamAnalyzer(snc, batchSize);
			
			model = analyzer.learn(exampleSet);
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}
	
}
