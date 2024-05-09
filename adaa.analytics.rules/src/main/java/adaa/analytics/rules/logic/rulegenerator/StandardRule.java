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
package adaa.analytics.rules.logic.rulegenerator;

import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.LogRank;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.DataTableAnnotations;

/**
 * The basic RuleKit learner operator. It enables inducing classification, regression,
 * and survival rules - the problem type is established automatically using metadata of
 * the training set.
 *
 * @author Adam Gudys
 */
public class StandardRule {
    private RuleGeneratorParams ruleGeneratorParams;

    private OperatorCommandProxy operatorCommandProxy;

    private AbstractSeparateAndConquer snc;
    private AbstractFinder finder;

    public StandardRule(RuleGeneratorParams ruleGeneratorParams, OperatorCommandProxy operatorCommandProxy) {
        this.ruleGeneratorParams = ruleGeneratorParams;
        this.operatorCommandProxy = operatorCommandProxy;
    }

    private void prepareSncAndFinder(IExampleSet exampleSet, IAttribute contrastAttr, InductionParameters params)
    {
        if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
            // survival problem
            finder = contrastAttr != null
                    ? new ContrastSurvivalFinder(params)
                    : new SurvivalLogRankFinder(params);
            snc = new SurvivalLogRankSnC((SurvivalLogRankFinder) finder, params);
        } else if (exampleSet.getAttributes().getLabel().isNumerical()) {
            // regression problem
            finder = contrastAttr != null
                    ? new ContrastRegressionFinder(params)
                    : new RegressionFinder(params);

            snc = new RegressionSnC((RegressionFinder) finder, params);
        } else {
            // classification problem
            if (contrastAttr != null) {
                finder = new ContrastClassificationFinder(params);
                snc = new ClassificationSnC((ClassificationFinder) finder, params);
            } else {

                if (params.isApproximateInduction()) {
                    finder = new ApproximateClassificationFinder(params);
                    snc = new ApproximateClassificationSnC((ClassificationFinder) finder, params);
                } else {
                    finder = new ClassificationFinder(params);
                    snc = new ClassificationSnC((ClassificationFinder) finder, params);
                }
            }

        }

        // overwrite snc for contrast sets
        if (contrastAttr != null) {
            snc = new ContrastSnC(finder, params);
        }

        snc.setOperatorCommandProxy(operatorCommandProxy);
    }

    private IAttribute prepareContrastAttribute(IExampleSet exampleSet)
    {
        IAttribute contrastAttr = null;
        DataTableAnnotations annotations = exampleSet.getAnnotations();

        if (annotations != null && annotations.containsKey(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            contrastAttr = exampleSet.getAttributes().get(exampleSet.getAnnotations().getAnnotation(ContrastRule.CONTRAST_ATTRIBUTE_ROLE));
        }

        // set role only when not null and different than label attribute
        if (contrastAttr != null && contrastAttr != exampleSet.getAttributes().getLabel()) {
            exampleSet.getAttributes().setSpecialAttribute(contrastAttr, ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
        }

        return contrastAttr;
    }

    private InductionParameters prepareInductionParams(IExampleSet exampleSet,IAttribute contrastAttr)
    {
        InductionParameters params = ruleGeneratorParams.generateInductionParameters();

        if (exampleSet.getAttributes().findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
            // survival problem
            //	if (getParameterAsBoolean(PARAMETER_LOGRANK_SURVIVAL)) {
            params.setInductionMeasure(new LogRank());
            params.setPruningMeasure(new LogRank());
            params.setVotingMeasure(new LogRank());

        }

        // overwrite snc for contrast sets
        if (contrastAttr != null) {
            params.setConditionComplementEnabled(true);
            params.setSelectBestCandidate(true);
        }
        return params;
    }
    public RuleSetBase learnStarndard(IExampleSet exampleSet) {

        IAttribute contrastAttr = prepareContrastAttribute(exampleSet);

        InductionParameters params =  prepareInductionParams(exampleSet,contrastAttr);

        prepareSncAndFinder(exampleSet,contrastAttr,params);

        double beginTime = System.nanoTime();
        RuleSetBase rs = snc.run(exampleSet);
        rs.setTotalTime((System.nanoTime() - beginTime) / 1e9);
        finder.close();

        return rs;
    }





}
