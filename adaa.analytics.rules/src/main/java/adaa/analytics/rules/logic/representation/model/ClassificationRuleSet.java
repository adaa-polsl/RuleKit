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
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.attributes.AttributeFactory;
import adaa.analytics.rules.utils.OperatorException;
import adaa.analytics.rules.utils.Ontology;

import java.util.List;

/**
 * Class representing a set of classification rules.
 *
 * @author Adam Gudys
 */
public class ClassificationRuleSet extends RuleSetBase {

    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = -767459208536480802L;

    /**
     * Name of the prediction attribute representing results of voting (weights).
     */
    public static final String ATTRIBUTE_VOTING_RESULTS_WEIGHTS = "voting_result_weights";

    /**
     * Name of the prediction attribute representing results of voting (counts).
     */
    public static final String ATTRIBUTE_VOTING_RESULTS_COUNTS = "voting_results_count";

    /**
     * Identifier of the default class.
     */
    private int defaultClass = -1;


    /**
     * Sets {@link #defaultClass}
     */
    public void setDefaultClass(int defaultClass) {
        this.defaultClass = defaultClass;
    }

    /**
     * Invokes base class constructor.
     *
     * @param exampleSet Training set.
     * @param isVoting   Voting flag.
     * @param params     Induction parameters.
     * @param knowledge  User's knowledge.
     */
    public ClassificationRuleSet(IExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
        super(exampleSet, isVoting, params, knowledge);
    }

    /**
     * Predicts class label for a given example. Sets output attributes describing voting results.
     *
     * @param example Example to be examined.
     * @return Predicted class label.
     * @throws OperatorException
     */
    @Override
    public double predict(Example example) throws OperatorException{
        IAttribute label = example.getAttributes().getLabel();
        assert (label.isNominal());
        int result = defaultClass;

        double[] votes = new double[label.getMapping().size()];
        int[] voteCounts = new int[label.getMapping().size()];

        for (Rule rule : rules) {
            if (rule.getPremise().evaluate(example)) {
                ConditionBase c = rule.getConsequence();

                if (c instanceof ElementaryCondition) {
                    ElementaryCondition consequence = ((ElementaryCondition) c);
                    SingletonSet d = (SingletonSet) consequence.getValueSet();
                    result = (int) (Math.round(d.getValue()));
                }

                if (isVoting) {
                    votes[result] += rule.getWeight();
                    ++voteCounts[result];
                } else {
                    break;
                }
            }
        }

        StringBuilder sb_weights = new StringBuilder();
        StringBuilder sb_counts = new StringBuilder();

        // select decision with highest voting power
        double maxVote = 0;
        double votesSum = 0;
        if (isVoting) {
            for (int i = 0; i < votes.length; ++i) {
                votesSum += votes[i];
                sb_weights.append(votes[i] + " ");
                sb_counts.append(voteCounts[i] + " ");
                if (votes[i] > maxVote) {
                    maxVote = votes[i];
                    result = i;
                }
            }
            calculateConfidence(example, votes, votesSum, result);
        }

        example.setValue(example.getAttributes().getSpecial(ATTRIBUTE_VOTING_RESULTS_WEIGHTS), sb_weights.toString());
        example.setValue(example.getAttributes().getSpecial(ATTRIBUTE_VOTING_RESULTS_COUNTS), sb_counts.toString());

        return (double) result;
    }

    private void calculateConfidence(Example example, double[] votes, double votesSum, int result) {

        IAttribute label = example.getAttributes().getLabel();
        List<String> labelValues = label.getMapping().getValues();
        int i = 0;
        double maxConfidence = Double.NaN;
        for (String labelValue : labelValues) {
            double confidence;
            if (votesSum == 0) {
                confidence = i == result ? 1 : 0;
            } else {
                confidence = votes[i] / votesSum;
            }
            example.setValue(example.getAttributes().get("confidence_" + labelValue), confidence);
            if (Double.isNaN(maxConfidence) || maxConfidence<confidence)
            {
                maxConfidence = confidence;
            }
            i++;
        }
        example.setValue(example.getAttributes().get("confidence"), maxConfidence);
    }

    /**
     * Applies the rule model on a given set (makes predictions for all examples).
     *
     * @param exampleSet Example set to be examined.
     * @return Example set with filled predictions and voting attributes.
     * @throws OperatorException
     */
    @Override
    public IExampleSet apply(IExampleSet exampleSet) throws OperatorException {
//        IExampleSet mappedExampleSet = new RemappedExampleSet(exampleSet, getTrainingHeader(), false);
        IExampleSet mappedExampleSet = exampleSet.updateMapping(getTrainingHeader());
        checkCompatibility(mappedExampleSet);
        IAttribute predictedLabel = createPredictionAttributes(mappedExampleSet, getLabel());
        IExampleSet result = performPrediction(mappedExampleSet, predictedLabel);

        // generate testing report
        if (exampleSet.getAttributes().getLabel() != null) {
            StringBuilder sb = new StringBuilder();
            int rid = 1;
            for (Rule r: rules) {
                double p = 0;
                double n = 0;
                double P = 0;
                double N = 0;
                for (Example e: exampleSet) {

                    boolean premiseAgree = r.getPremise().evaluate(e);
                    boolean consequenceAgree = r.getConsequence().evaluate(e);

                    if (consequenceAgree) {
                        P += 1.0;  // positive
                        if (premiseAgree) {
                            p += 1.0; // covered positive
                        }
                    } else {
                        N += 1.0; // negative
                        if (premiseAgree) { // covered negative
                            n += 1.0;
                        }
                    }

                }

                sb.append("r" + rid + ": " + r.toString() + "(p=" + p + ", n=" + n + ", P=" + P + ", N=" + N + ")\n");
                ++rid;
            }

            result.getAnnotations().put(ANNOTATION_TEST_REPORT, sb.toString());
        }

        // Copy in order to avoid RemappedExampleSets wrapped around each other accumulating over time
        //copyPredictedLabel(result, exampleSet);
        return result;
    }

    /**
     * Computes prediction attributes (voting results) for a given set.
     *
     * @param exampleSet Example set to be examined.
     * @param label      Input label attribute.
     * @return Output label attribute.
     */
    @Override
    protected IAttribute createPredictionAttributes(IExampleSet exampleSet, IAttribute label) {
        IAttribute predictedLabel = super.createPredictionAttributes(exampleSet, label);

        IAttribute attr = AttributeFactory.createAttribute(ATTRIBUTE_VOTING_RESULTS_WEIGHTS, Ontology.STRING);
        exampleSet.addAttribute(attr);
        exampleSet.getAttributes().setSpecialAttribute(attr, attr.getName());

        attr = AttributeFactory.createAttribute(ATTRIBUTE_VOTING_RESULTS_COUNTS, Ontology.STRING);
        exampleSet.addAttribute(attr);
        exampleSet.getAttributes().setSpecialAttribute(attr, attr.getName());

        return predictedLabel;
    }
}
