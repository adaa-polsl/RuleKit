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
package adaa.analytics.rules.logic.representation.model;

import adaa.analytics.rules.logic.induction.InductionParameters;

import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;
import adaa.analytics.rules.logic.representation.Knowledge;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.RemappedExampleSet;
import adaa.analytics.rules.rm.example.table.AttributeFactory;
import adaa.analytics.rules.rm.example.table.IExampleTable;
import adaa.analytics.rules.rm.operator.OperatorException;
import adaa.analytics.rules.rm.tools.Ontology;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a set of survival rules.
 * @author Adam Gudys
 *
 */
public class SurvivalRuleSet extends RuleSetBase {

	/** Serialization identifier. */
	private static final long serialVersionUID = -1186396337399240471L;
	
	/** Name of the prediction attribute representing survival function estimator (in a text form). */
	public static final String ATTRIBUTE_ESTIMATOR = "estimator";
	
	/** Annotation representing survival function estimator of the training set (in a text form).  */
	public static final String ANNOTATION_TRAINING_ESTIMATOR = "training_estimator";
	
	/** Annotation storing reveresed survival estimator of the training set (in a text form). */
	public static final String ANNOTATION_TRAINING_ESTIMATOR_REV = "training_estimator_rev";

	/** Training set estimator. */
	protected KaplanMeierEstimator trainingEstimator;
	
	/** Gets {@link #trainingEstimator}}. */
	public KaplanMeierEstimator getTrainingEstimator() { return trainingEstimator; }
	
	/**
	 * Invokes base class constructor and calculates survival function estimator for the training set. 
	 * @param exampleSet Training set.
	 * @param isVoting Voting flag.
	 * @param params Induction parameters.
	 * @param knowledge User's knowledge.
	 */
	public SurvivalRuleSet(IExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
		super(exampleSet, isVoting, params, knowledge);
		
		trainingEstimator = new KaplanMeierEstimator(exampleSet);
	}

	/**
	 * Estimates survival function for a given example and stores in a text form in ATTRIBUTE_ESTIMATOR attribute. 
	 * @param example Example to be examined.
	 * @return Should be ignored (always 0).
	 * @throws OperatorException
	 */
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
	
	/**
	 * Applies the rule model on a given set (estimates survival functions for all examples).
	 * @param exampleSet Example set to be examined.
	 * @return Example set with filled estimates and annotations.
	 * @throws OperatorException
	 */
	@Override
	public IExampleSet apply(IExampleSet exampleSet) throws OperatorException {
		IExampleSet mappedExampleSet = new RemappedExampleSet(exampleSet, getTrainingHeader(), false);
        checkCompatibility(mappedExampleSet);
		IAttribute predictedLabel = createPredictionAttributes(mappedExampleSet, getLabel());
		IExampleSet result = performPrediction(mappedExampleSet, predictedLabel);
		
		// Copy in order to avoid RemappedExampleSets wrapped around each other accumulating over time
		//copyPredictedLabel(result, exampleSet);

        // add annotation
		KaplanMeierEstimator revEstimator = trainingEstimator.reverse();
		result.getAnnotations().setAnnotation(ANNOTATION_TRAINING_ESTIMATOR, trainingEstimator.save());
		result.getAnnotations().setAnnotation(ANNOTATION_TRAINING_ESTIMATOR_REV, revEstimator.save());
		
        return result;
	}
	
	/**
	 * Computes prediction attributes (survival estimator) for a given set.
	 * @param exampleSet Example set to be examined.
	 * @param label Input label attribute.
	 * @return Output label attribute.
	 */
	@Override
	protected IAttribute createPredictionAttributes(IExampleSet exampleSet, IAttribute label) {
		IAttribute predictedLabel = super.createPredictionAttributes(exampleSet, label);
		
		IExampleTable table = exampleSet.getExampleTable();
		IAttribute attr = AttributeFactory.createAttribute(ATTRIBUTE_ESTIMATOR, Ontology.STRING);
		table.addAttribute(attr);
		exampleSet.getAttributes().setSpecialAttribute(attr, attr.getName());
		
		return predictedLabel;
	}
	
	/**
	 * Generates text representation of the survival rule set. Beside list of rules,
	 * it contains survival function estimates of the entire training set and particular rules.
	 * @return Rule set in the text form.
	 */
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
