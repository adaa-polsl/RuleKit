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
import adaa.analytics.rules.logic.representation.ruleset.RuleSetBase;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.utils.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;

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
    private Boolean useExpert;
    public RuleGenerator() {
        operatorCommandProxy = new OperatorCommandProxy();
    }

    /**
     * Used by python wrapper
     * */
    public RuleGenerator(boolean useExpert) {
        this.useExpert = useExpert;
        operatorCommandProxy = new OperatorCommandProxy();
    }

    private void configureLogger(PrintStream printStream, String level)
    {
        Logger.getInstance().addStream(printStream, level.contains("vv") ? Level.FINEST : (level.contains("v")? Level.FINE : Level.INFO));
    }
    /**
     * Used by python wrapper
     * */
    public void configureLogger(String level)
    {
        configureLogger(System.out, level);
    }

    /**
     * Used by python wrapper
     * */
    public void configureLogger(String filePath, String level) throws FileNotFoundException {
        configureLogger(new PrintStream(new FileOutputStream(filePath)), level);
    }

    /**
     * Used by python wrapper
     * */
    public void addOperatorListener(ICommandListener commandListener) {
        operatorCommandProxy.addCommandListener(commandListener);
    }

    public RuleSetBase learn(IExampleSet exampleSet) {
        RuleSetBase m;
        if (useExpert==null)
        {
            useExpert = ruleGeneratorParams.getParameterAsBoolean(RuleGeneratorParams.PARAMETER_USE_EXPERT);
        }
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

    /**
     * Used by python wrapper
     * */
    public boolean containsParameter(String key) {
        return ruleGeneratorParams.contains(key);
    }

    public void setRuleGeneratorParams(RuleGeneratorParams ruleGeneratorParams) {
        this.ruleGeneratorParams = ruleGeneratorParams;
    }
    /**
     * Used by python wrapper
     * */
    public void setParameter(String key, String o) {
        ruleGeneratorParams.setParameter(key, o);
    }
    /**
     * Used by python wrapper
     * */
    public void setListParameter(String key, List<String[]> o) {
        ruleGeneratorParams.setListParameter(key, o);
    }
    /**
     * Used by python wrapper
     * */
    public void setUserMeasureInductionObject(IUserMeasure userMeasureInductionObject) {
        ruleGeneratorParams.setUserMeasureInductionObject(userMeasureInductionObject);
    }
    /**
     * Used by python wrapper
     * */
    public void setUserMeasurePurningObject(IUserMeasure userMeasurePurningObject) {
        ruleGeneratorParams.setUserMeasurePurningObject(userMeasurePurningObject);
    }
    /**
     * Used by python wrapper
     * */
    public void setUserMeasureVotingObject(IUserMeasure userMeasureVotingObject) {
        ruleGeneratorParams.setUserMeasureVotingObject(userMeasureVotingObject);
    }

    public String getParamsAsJsonString()
    {
        return ruleGeneratorParams.toJsonString();
    }

}
