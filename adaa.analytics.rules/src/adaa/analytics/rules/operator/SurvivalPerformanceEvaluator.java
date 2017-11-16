package adaa.analytics.rules.operator;

import java.util.LinkedList;
import java.util.List;

import adaa.analytics.rules.logic.representation.IntegratedBrierScore;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.parameter.UndefinedParameterError;

public class SurvivalPerformanceEvaluator extends AbstractPerformanceEvaluator {
	
	public SurvivalPerformanceEvaluator(OperatorDescription description) {	
		super(description);
	}
	
	@Override
	protected boolean canEvaluate(int valueType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<PerformanceCriterion> getCriteria() {
		List<PerformanceCriterion> performanceCriteria = new LinkedList<PerformanceCriterion>();
		
		performanceCriteria.add(new IntegratedBrierScore());
		
		return performanceCriteria;
	}

	@Override
	protected double[] getClassWeights(Attribute label)
			throws UndefinedParameterError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void checkCompatibility(ExampleSet exampleSet)
			throws OperatorException {
		// TODO Auto-generated method stub
		
	}
	
}
