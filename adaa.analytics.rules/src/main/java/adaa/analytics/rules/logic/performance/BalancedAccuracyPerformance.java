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
package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.logic.representation.model.ClassificationRuleSet;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.BitSet;

/**
 * Class gathering additional performance measures for balanced accuracy.
 *
 * @author Adam Gudys
 */
public class BalancedAccuracyPerformance extends AbstractPerformanceCounter {



    @Override
    public PerformanceResult countExample(IExampleSet testSet) {

        int numClasses = testSet.getAttributes().getLabel().getMapping().size();

        int[] good = new int[numClasses];
        int[] bad = new int[numClasses];

        for (Example e : testSet) {
            int label = (int) e.getLabel();
            if (label == (int) e.getPredictedLabel()) {
                ++good[label];
            } else {
                ++bad[label];
            }
        }

        double bacc = 0;
        double denominator = 0;
        for (int i = 0; i < numClasses; ++i) {
            if (good[i] + bad[i] > 0) {
                bacc += (double) good[i] / (good[i] + bad[i]);
                denominator += 1.0;
            }
        }
        bacc /= denominator;

        PerformanceResult ret = new PerformanceResult();
        ret.setName("balanced_accuracy");
        ret.setValue(bacc);
        return ret;
    }


}
