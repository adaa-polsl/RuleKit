/**
 * Copyright (C) 2001-2019 by RapidMiner and the contributors
 * <p>
 * Complete list of developers available at our web site:
 * <p>
 * http://rapidminer.com
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */
package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * The logistic loss of a classifier, defined as the average over all ln(1 + exp(-y * f(x)))
 *
 * @author Ingo Mierswa
 */
public class LogisticLoss extends AbstractPerformanceCounter {


    /**
     * The value of the loss.
     */
    private double loss = Double.NaN;

    private double counter = 0.0d;

    /**
     * Clone constructor.
     */
    public LogisticLoss() {
    }

    /**
     * Calculates the margin.
     */
    @Override
    public void startCounting(IExampleSet exampleSet) {
        // compute margin
        Iterator<Example> reader = exampleSet.iterator();
        this.loss = 0.0d;
        this.counter = 0.0d;
        IAttribute labelAttr = exampleSet.getAttributes().getLabel();
        IAttribute weightAttr = null;
        weightAttr = exampleSet.getAttributes().getWeight();


        while (reader.hasNext()) {
            Example example = reader.next();
            String trueLabel = example.getNominalValue(labelAttr);
            double confidence = example.getConfidence(trueLabel);
            double weight = 1.0d;
            if (weightAttr != null) {
                weight = example.getValue(weightAttr);
            }
            double currentMargin = weight * Math.log(1.0d + Math.exp(-1 * confidence));
            this.loss += currentMargin;
            this.counter += weight;
        }
    }

    @Override
    public void countExample(Example example) {
    }

    @Override
    public double getAverage() {
        return this.loss / counter;
    }

    @Override
    public String getName() {
        return "logistic_loss";
    }

}
