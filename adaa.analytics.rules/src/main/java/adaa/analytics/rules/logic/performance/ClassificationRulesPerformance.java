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
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.IExampleSet;

import java.util.BitSet;

/**
 * Class gathering additional performance measures for classification models (avg. number of rules covering an
 * example, number of voting conflicts).
 *
 * @author Adam Gudys
 */
public class ClassificationRulesPerformance extends AbstractPerformanceCounter {

    public static final int RULES_PER_EXAMPLE = 1;

    public static final int VOTING_CONFLICTS = 2;


    private int type;


    public ClassificationRulesPerformance(int type) {
        this.type = type;
    }


    @Override
    public PerformanceResult countExample(IExampleSet testSet) {
        int conflictCount = 0;
        int covCounts = 0;
        double value = 0;

        for (Example e : testSet) {
            // get conflict measures
            String[] counts = e.getValueAsString(testSet.getAttributes().getColumnByRole(ClassificationRuleSet.ATTRIBUTE_VOTING_RESULTS_COUNTS)).split(" ");

            BitSet mask = new BitSet(counts.length);

            for (int i = 0; i < counts.length; ++i) {
                int k = Integer.parseInt(counts[i]);
                covCounts += k;
                if (k > 0) {
                    mask.set(i);
                }
            }

            // when more than one bit is set - conflict
            if (mask.cardinality() > 1) {
                ++conflictCount;
            }
        }


        if (type == VOTING_CONFLICTS) {
            value = (double) conflictCount;
        } else if (type == RULES_PER_EXAMPLE) {
            value = (double) covCounts / testSet.size();
        }
        PerformanceResult ret = new PerformanceResult();
        switch (type) {
            case RULES_PER_EXAMPLE:
                ret.setName("#rules_per_example");
                break;
            case VOTING_CONFLICTS:
                ret.setName("#voting_conflicts");
                break;
            default:
                ret.setName("unspecified_name");
        }

        ret.setValue(value);
        return ret;
    }


}
