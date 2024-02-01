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

import adaa.analytics.rules.logic.performance.MeasuredPerformance;
import adaa.analytics.rules.logic.performance.RecountedPerformance;
import adaa.analytics.rules.logic.representation.ContrastIndicators;
import adaa.analytics.rules.logic.representation.ContrastRuleSet;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * The basic RuleKit learner operator. It enables inducing classification, regression,
 * and survival rules - the problem type is established automatically using metadata of
 * the training set.
 *
 * @author Adam Gudys
 */
public class RuleGenerator {

    protected RuleGeneratorParams ruleGeneratorParams = new RuleGeneratorParams();


    protected OperatorCommandProxy operatorCommandProxy;
    private boolean useExpert = false;
    public RuleGenerator() {
        useExpert = ruleGeneratorParams.getParameterAsBoolean(RuleGeneratorParams.PARAMETER_USE_EXPERT);
        operatorCommandProxy = new OperatorCommandProxy();
    }

    public RuleGenerator(boolean useExpert) {
        this.useExpert = useExpert;
        operatorCommandProxy = new OperatorCommandProxy();
    }

    public RuleGeneratorParams getRuleGeneratorParams() {
        return ruleGeneratorParams;
    }

    public void addOperatorListener(ICommandListener commandListener) {
        operatorCommandProxy.addCommandListener(commandListener);
    }

    public RuleSetBase learn(ExampleSet exampleSet) {
        RuleSetBase m;
        // do not use expert knowledge in when option is not set
        if (useExpert) {
            ExpertRule ruleGenerator = new ExpertRule(ruleGeneratorParams, operatorCommandProxy);
            m = ruleGenerator.learnWithExpert(exampleSet);
        } else {
            StandardRule ruleGenerator = new StandardRule(ruleGeneratorParams, operatorCommandProxy);
            m = ruleGenerator.learnStarndard(exampleSet);
        }
        return m;
    }
}
