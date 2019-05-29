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
package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.InductionParameters;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.RemappedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;

import java.util.ArrayList;
import java.util.List;

public class SurvivalRuleSet extends RuleSetBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1186396337399240471L;
	
	public static final String ATTRIBUTE_ESTIMATOR = "estimator";
	
	public static final String ANNOTATION_TRAINING_ESTIMATOR = "training_estimator";
	public static final String ANNOTATION_TRAINING_ESTIMATOR_REV = "training_estimator_rev";

	protected KaplanMeierEstimator trainingEstimator;
	
	public KaplanMeierEstimator getTrainingEstimator() { return trainingEstimator; }
	
	

	
	public SurvivalRuleSet(ExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
		super(exampleSet, isVoting, params, knowledge);
		
		trainingEstimator = new KaplanMeierEstimator(exampleSet);
	}

	@Override 
	public double predict(Example example) throws OperatorException {
		
		// predict estimator
		List<SurvivalRule> matchingRules = new ArrayList<SurvivalRule>();
		
		for (Rule rule : rules) {
			SurvivalRule survRule = (SurvivalRule)rule; 
			if (rule.getPremise().evaluate(example)) {
				matchingRules.add(survRule);
			}
		}
		
 	    KaplanMeierEstimator kaplan;
 	    if (matchingRules.size() == 0) {
 	        kaplan = trainingEstimator;
 	    } else {
 	        KaplanMeierEstimator[] survfits = new KaplanMeierEstimator[matchingRules.size()];
 	        for (int ruleidx = 0; ruleidx < matchingRules.size(); ruleidx++) {
 	        	survfits[ruleidx] = matchingRules.get(ruleidx).getEstimator();
 	        }
 	
 	        kaplan = KaplanMeierEstimator.average(survfits);
 	    }
 	    
 	    String textKaplan = kaplan.save();
		example.setValue(example.getAttributes().getSpecial(ATTRIBUTE_ESTIMATOR), textKaplan);
	
		return 0;
	}
	
	
	@Override
	public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
		ExampleSet mappedExampleSet = new RemappedExampleSet(exampleSet, getTrainingHeader(), false);
        checkCompatibility(mappedExampleSet);
		Attribute predictedLabel = createPredictionAttributes(mappedExampleSet, getLabel());
		ExampleSet result = performPrediction(mappedExampleSet, predictedLabel);
		
		// Copy in order to avoid RemappedExampleSets wrapped around each other accumulating over time
		//copyPredictedLabel(result, exampleSet);

        // add annotation
		KaplanMeierEstimator revEstimator = trainingEstimator.reverse();
		result.getAnnotations().setAnnotation(ANNOTATION_TRAINING_ESTIMATOR, trainingEstimator.save());
		result.getAnnotations().setAnnotation(ANNOTATION_TRAINING_ESTIMATOR_REV, revEstimator.save());
		
        return result;
	}
	
	
	@Override
	protected Attribute createPredictionAttributes(ExampleSet exampleSet, Attribute label) {
		Attribute predictedLabel = super.createPredictionAttributes(exampleSet, label);
		
		ExampleTable table = exampleSet.getExampleTable();
		Attribute attr = AttributeFactory.createAttribute(ATTRIBUTE_ESTIMATOR, Ontology.STRING);
		table.addAttribute(attr);
		exampleSet.getAttributes().setSpecialAttribute(attr, attr.getName());
		
		return predictedLabel;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("\nEstimator:\n");	
		
		// get times from training estimator
		ArrayList<Double> times = trainingEstimator.getTimes();
		
		// build header
		sb.append("time,entire-set");
		for (int i = 0; i < rules.size(); ++i) {
			sb.append(",r" + (i + 1));
		}
		sb.append("\n");
		
		for (double t : times) {
			sb.append(t + "," + trainingEstimator.getProbabilityAt(t));
			for (Rule r: rules) {
				KaplanMeierEstimator kme = ((SurvivalRule)r).getEstimator();
				sb.append("," + kme.getProbabilityAt(t));
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
