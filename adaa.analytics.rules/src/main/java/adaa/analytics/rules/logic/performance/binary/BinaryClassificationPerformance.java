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
package adaa.analytics.rules.logic.performance.binary;

import adaa.analytics.rules.logic.performance.MeasuredPerformance;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;


/**
 * This class encapsulates the well known binary classification criteria precision and recall.
 * Furthermore it can be used to calculate the fallout, the equally weighted f-measure (f1-measure),
 * the lift, and the values for TRUE_POSITIVE, FALSE_POSITIVE, TRUE_NEGATIVE, and FALSE_NEGATIVE.
 * With &quot;positive&quot; we refer to the first class and with &quot;negative&quot; we refer to
 * the second.
 *
 * @author Ingo Mierswa, Simon Fischer
 */
public class BinaryClassificationPerformance extends MeasuredPerformance {


	public static final int PRECISION = 0;

	public static final int RECALL = 1;

	public static final int LIFT = 2;

	public static final int FALLOUT = 3;

	public static final int F_MEASURE = 4;

	public static final int FALSE_POSITIVE = 5;

	public static final int FALSE_NEGATIVE = 6;

	public static final int TRUE_POSITIVE = 7;

	public static final int TRUE_NEGATIVE = 8;

	public static final int SENSITIVITY = 9;

	public static final int SPECIFICITY = 10;

	public static final int YOUDEN = 11;

	public static final int POSITIVE_PREDICTIVE_VALUE = 12;

	public static final int NEGATIVE_PREDICTIVE_VALUE = 13;

	public static final int PSEP = 14;

	private static final int N = 0;

	private static final int P = 1;

	public static final String[] NAMES = { "precision", "recall", "lift", "fallout", "f_measure", "false_positive",
			"false_negative", "true_positive", "true_negative", "sensitivity", "specificity", "youden",
			"positive_predictive_value", "negative_predictive_value", "psep" };

	private int type = 0;

	/** true label, predicted label. PP = TP, PN = FN, NP = FP, NN = TN. */
	private double[][] counter = new double[2][2];

	/** Name of the positive class. */
	private String positiveClassName = "";

	/** Name of the negative class. */
	private String negativeClassName = "";

	/** The predicted label attribute. */
	private IAttribute predictedLabelAttribute;

	/** The label attribute. */
	private IAttribute labelAttribute;

	/** The weight attribute. Might be null. */
	private IAttribute weightAttribute;

	/**
	 * True if the user defined positive class should be used instead of the label's default mapping.
	 */
	private boolean userDefinedPositiveClass = false;

	public BinaryClassificationPerformance() {
		type = -1;
	}

	public BinaryClassificationPerformance(int type) {
		this.type = type;
	}



	// ================================================================================

	@Override
	public void startCounting(IExampleSet eSet, boolean useExampleWeights) {
		super.startCounting(eSet, useExampleWeights);
		this.predictedLabelAttribute = eSet.getAttributes().getPredictedLabel();
		this.labelAttribute = eSet.getAttributes().getLabel();
		if (!labelAttribute.isNominal()) {
//			throw new UserError(null, 120, labelAttribute.getName(), Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(labelAttribute
//					.getValueType()), Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(Ontology.NOMINAL));
			throw new IllegalStateException();
		}
		if (!predictedLabelAttribute.isNominal()) {
//			throw new UserError(null, 120, predictedLabelAttribute.getName(),
//					Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(predictedLabelAttribute.getValueType()),
//					Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(Ontology.NOMINAL));
			throw new IllegalStateException();
		}
		if (labelAttribute.getMapping().size() != 2) {
//			throw new UserError(null, 118, new Object[] { "'" + labelAttribute.getName() + "'",
//					Integer.valueOf(labelAttribute.getMapping().getValues().size()),
//					"2 for calculation of '" + getName() + "'" });
			throw new IllegalStateException();
		}
		if (predictedLabelAttribute.getMapping().size() != 2) {
//			throw new UserError(null, 118, new Object[] { "'" + predictedLabelAttribute.getName() + "'",
//					Integer.valueOf(predictedLabelAttribute.getMapping().getValues().size()),
//					"2 for calculation of '" + getName() + "'" });
			throw new IllegalStateException();
		}
		if (!labelAttribute.getMapping().equals(predictedLabelAttribute.getMapping())) {
//			throw new UserError(null, 157);
			throw new IllegalStateException();
		}


		updatePosNegClassNames();

		if (useExampleWeights) {
			this.weightAttribute = eSet.getAttributes().getWeight();
		}
		this.counter = new double[2][2];
	}

	@Override
	public void countExample(Example example) {
		String labelString = example.getNominalValue(labelAttribute);
		int label = positiveClassName.equals(labelString) ? P : N;
		String predString = example.getNominalValue(predictedLabelAttribute);
		int plabel = positiveClassName.equals(predString) ? P : N;

		double weight = 1.0d;
		if (weightAttribute != null) {
			weight = example.getValue(weightAttribute);
		}
		counter[label][plabel] += weight;
	}

	@Override
	public double getAverage() {
		double x = 0.0d, y = 0.0d;
		switch (type) {
			case PRECISION:
				x = counter[P][P];
				y = counter[P][P] + counter[N][P];
				break;
			case RECALL:
				x = counter[P][P];
				y = counter[P][P] + counter[P][N];
				break;
			case LIFT:
				x = counter[P][P] / (counter[P][P] + counter[P][N]);
				y = (counter[P][P] + counter[N][P]) / (counter[P][P] + counter[P][N] + counter[N][P] + counter[N][N]);
				break;
			case FALLOUT:
				x = counter[N][P];
				y = counter[N][P] + counter[N][N];
				break;

			case F_MEASURE:
				x = counter[P][P];
				x *= x;
				x *= 2;
				y = x + counter[P][P] * counter[P][N] + counter[P][P] * counter[N][P];
				break;

			case FALSE_NEGATIVE:
				x = counter[P][N];
				y = 1;
				break;
			case FALSE_POSITIVE:
				x = counter[N][P];
				y = 1;
				break;
			case TRUE_NEGATIVE:
				x = counter[N][N];
				y = 1;
				break;
			case TRUE_POSITIVE:
				x = counter[P][P];
				y = 1;
				break;
			case SENSITIVITY:
				x = counter[P][P];
				y = counter[P][P] + counter[P][N];
				break;
			case SPECIFICITY:
				x = counter[N][N];
				y = counter[N][N] + counter[N][P];
				break;
			case YOUDEN:
				x = counter[N][N] * counter[P][P] - counter[P][N] * counter[N][P];
				y = (counter[P][P] + counter[P][N]) * (counter[N][P] + counter[N][N]);
				break;
			case POSITIVE_PREDICTIVE_VALUE:
				x = counter[P][P];
				y = counter[P][P] + counter[N][P];
				break;
			case NEGATIVE_PREDICTIVE_VALUE:
				x = counter[N][N];
				y = counter[N][N] + counter[P][N];
				break;
			case PSEP:
				x = counter[N][N] * counter[P][P] + counter[N][N] * counter[N][P] - counter[N][P] * counter[N][N]
						- counter[N][P] * counter[P][N];
				y = counter[P][P] * counter[N][N] + counter[P][P] * counter[P][N] + counter[N][P] * counter[N][N]
						+ counter[N][P] * counter[P][N];
				break;
			default:
				throw new RuntimeException("Illegal value for type in BinaryClassificationPerformance: " + type);
		}
		if (y == 0) {
			return Double.NaN;
		}
		return x / y;
	}

	@Override
	public String getName() {
		return NAMES[type];
	}


	public double[][] getCounter() {
		return counter;
	}

	private void updatePosNegClassNames()  {
		String mapNegativeClassName = predictedLabelAttribute.getMapping().getNegativeString();
		String mapPositiveClassName = predictedLabelAttribute.getMapping().getPositiveString();
		if (userDefinedPositiveClass) {
			if (positiveClassName.equals(mapPositiveClassName)) {
				negativeClassName = mapNegativeClassName;
			} else if (positiveClassName.equals(mapNegativeClassName)) {
				negativeClassName = mapPositiveClassName;
			} else {
//				throw new UserError(null, "invalid_positive_class", positiveClassName);
				throw new IllegalStateException("invalid_positive_class "+positiveClassName);
			}
		} else {
			positiveClassName = mapPositiveClassName;
			negativeClassName = mapNegativeClassName;
		}
	}
}
