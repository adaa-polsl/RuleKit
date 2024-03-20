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

import adaa.analytics.rules.logic.quality.IUserMeasure;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.rm.example.IExampleSet;

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

    public void addOperatorListener(ICommandListener commandListener) {
        operatorCommandProxy.addCommandListener(commandListener);
    }

    public RuleSetBase learn(IExampleSet exampleSet) {
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

    public boolean containsParameter(String key) {
        return ruleGeneratorParams.contains(key);
    }

    public void setParameter(String key, String o) {
        ruleGeneratorParams.setParameter(key, o);
    }

    public void setListParameter(String key, List<String[]> o) {
        ruleGeneratorParams.setListParameter(key, o);
    }

    public void setUserMeasureInductionObject(IUserMeasure userMeasureInductionObject) {
        ruleGeneratorParams.setUserMeasureInductionObject(userMeasureInductionObject);
    }

    public void setUserMeasurePurningObject(IUserMeasure userMeasurePurningObject) {
        ruleGeneratorParams.setUserMeasurePurningObject(userMeasurePurningObject);
    }

    public void setUserMeasureVotingObject(IUserMeasure userMeasureVotingObject) {
        ruleGeneratorParams.setUserMeasureVotingObject(userMeasureVotingObject);
    }
}
