package adaa.analytics.rules.stream;

import adaa.analytics.rules.logic.induction.AbstractSeparateAndConquer;
import adaa.analytics.rules.logic.induction.ClassificationExpertFinder;
import adaa.analytics.rules.logic.induction.ClassificationExpertSnC;
import adaa.analytics.rules.logic.induction.ClassificationFinder;
import adaa.analytics.rules.logic.induction.ClassificationSnC;
import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.induction.RegressionFinder;
import adaa.analytics.rules.logic.induction.RegressionSnC;
import adaa.analytics.rules.logic.induction.SurvivalClassificationSnC;
import adaa.analytics.rules.logic.induction.SurvivalLogRankFinder;
import adaa.analytics.rules.logic.induction.SurvivalLogRankSnC;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.RuleGenerator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SimplePartitionBuilder;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;

public class RuleGeneratorFromStream extends ExpertRuleGenerator {

	public RuleGeneratorFromStream(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}


	public static final String PARAMETER_BATCH_SIZE = "batch_size";
	
	private RuleSetBase rules = null;
	
	@Override
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		Model model = null;
		
		try{
			InductionParameters params = new InductionParameters();
			params.setInductionMeasure(this.createMeasure(PARAMETER_INDUCTION_MEASURE));
			params.setPruningMeasure(this.createMeasure(PARAMETER_PRUNING_MEASURE)); 
			params.setMaximumUncoveredFraction(getParameterAsDouble(PARAMETER_MAX_UNCOVERED_FRACTION));
			params.setMinimumCovered(getParameterAsDouble(PARAMETER_MIN_RULE_COVERED));
			params.setEnablePruning(getParameterAsBoolean(PARAMETER_PRUNING_ENABLED));
			params.setIgnoreMissing(getParameterAsBoolean(PARAMETER_IGNORE_MISSING));

			AbstractSeparateAndConquer snc; 
			
			if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
				// survival problem
				if (getParameterAsBoolean(PARAMETER_LOGRANK_SURVIVAL)) {
					params.setInductionMeasure(new LogRank());
					params.setPruningMeasure(new LogRank());
					SurvivalLogRankFinder finder = new SurvivalLogRankFinder(params);
					snc = new SurvivalLogRankSnC(finder, params);
				} else {
					ClassificationFinder finder = new ClassificationFinder(params);
					snc = new SurvivalClassificationSnC(finder, params);
				}
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
