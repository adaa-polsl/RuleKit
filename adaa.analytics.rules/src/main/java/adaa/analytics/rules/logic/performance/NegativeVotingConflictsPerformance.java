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
 * Class gathering  number of negative voting conflicts).
 *
 * @author Adam Gudys
 */
public class NegativeVotingConflictsPerformance extends AbstractPerformanceCounter {


    @Override
    public PerformanceResult countExample(IExampleSet testSet) {

        int negativeConflictCount = 0;

        for (Example e : testSet) {
            int label = (int) e.getLabel();

            // get conflict measures
            String[] counts = e.getValueAsString(e.getAttributes().getSpecial(ClassificationRuleSet.ATTRIBUTE_VOTING_RESULTS_COUNTS)).split(" ");

            BitSet mask = new BitSet(counts.length);

            for (int i = 0; i < counts.length; ++i) {
                int k = Integer.parseInt(counts[i]);
                if (k > 0) {
                    mask.set(i);
                }
            }

            // when more than one bit is set - conflict
            if (mask.cardinality() > 1) {
                if (label != (int) e.getPredictedLabel()) {
                    ++negativeConflictCount;
                }
            }
        }

        PerformanceResult ret = new PerformanceResult();
        ret.setName("#negative_voting_conflicts");
        ret.setValue(negativeConflictCount);
        return ret;
    }


}
