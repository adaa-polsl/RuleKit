/**
 * Copyright (C) 2001-2019 by RapidMiner and the contributors
 * 
 * Complete list of developers available at our web site:
 * 
 * http://rapidminer.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
*/
package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;


/**
 * The margin of a classifier, defined as the minimal confidence for the correct label.
 * 
 * @author Martin Scholz, Ingo Mierswa
 */
public class Margin extends AbstractPerformanceCounter {


	/** The value of the criterion. */
	private double margin = Double.NaN;

	private double counter = 1.0d;

	/** Clone constructor. */
	public Margin() {}

	/** Calculates the margin. */
	@Override
	public void startCounting(IExampleSet exampleSet) {
		// compute margin
		Iterator<Example> reader = exampleSet.iterator();
		this.margin = 1.0d;
		IAttribute labelAttr = exampleSet.getAttributes().getLabel();
		while (reader.hasNext()) {
			Example example = reader.next();
			String trueLabel = example.getNominalValue(labelAttr);
			double confidence = example.getConfidence(trueLabel);
			this.margin = Math.min(margin, confidence);
		}
	}

	@Override
	public void countExample(Example example) {}

	@Override
	public double getAverage() {
		return margin / counter;
	}


	@Override
	public String getName() {
		return "margin";
	}

}
