package adaa.analytics.rules.operator;

import adaa.analytics.rules.logic.representation.SurvivalRule;

import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.conditions.ParameterCondition;

public class SurvivalMetaCondition extends ParameterCondition{

	protected AbstractLearner learner;
	
	public SurvivalMetaCondition(ParameterHandler parameterHandler, boolean becomeMandatory, AbstractLearner learner) {
		super(parameterHandler, becomeMandatory);
		this.learner = learner;
	}
	
	
	@Override
	public boolean isConditionFullfilled() {
		ExampleSetMetaData setMeta = (ExampleSetMetaData) learner.getExampleSetInputPort().getMetaData();
		
		boolean out =
				setMeta != null && 
				setMeta.getLabelMetaData() != null &&
				setMeta.getAttributeByRole(SurvivalRule.SURVIVAL_TIME_ROLE) != null;
		return out;	
	}

}