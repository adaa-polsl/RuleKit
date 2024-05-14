package adaa.analytics.rules.logic.representation.model;

import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.INominalMapping;
import adaa.analytics.rules.data.metadata.AttributeFactory;
import adaa.analytics.rules.data.metadata.EColumnRole;
import adaa.analytics.rules.data.metadata.EColumnType;
import adaa.analytics.rules.utils.Logger;
import adaa.analytics.rules.utils.OperatorException;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public abstract class PredictionModel implements Serializable {

	/** Serialization identifier. */
	private static final long serialVersionUID = -7112032011739715168L;

	private IExampleSet headerExampleSet;

	protected PredictionModel(IExampleSet trainingExampleSet) {
			this.headerExampleSet = trainingExampleSet;
	}

	public IExampleSet getTrainingHeader() {
		return this.headerExampleSet;
	}
	/**
	 * Subclasses should iterate through the given example set and set the prediction for each
	 * example. The given predicted label attribute was already be added to the example set and
	 * should be used to set the predicted values.
	 */
	public abstract IExampleSet performPrediction(IExampleSet exampleSet, IAttribute predictedLabel) throws OperatorException;

	/**
	 * Applies the model by creating a predicted label attribute and setting the predicted label
	 * values.
	 */

	public IExampleSet apply(IExampleSet exampleSet) throws OperatorException
	{

//		IExampleSet mappedExampleSet = RemappedExampleSet.create(exampleSet, getTrainingHeader(), false, true);
		IExampleSet mappedExampleSet = exampleSet.updateMapping(getTrainingHeader());
		checkCompatibility(mappedExampleSet);
		IAttribute predictedLabel = createPredictionAttributes(mappedExampleSet, getLabel());
		IExampleSet result = performPrediction(mappedExampleSet, predictedLabel);

		// Copy in order to avoid RemappedExampleSets wrapped around each other accumulating over
		// time
		/*MK-WG instead of copying just return result exampleset
		exampleSet = (IExampleSet) exampleSet.clone();
		copyPredictedLabel(result, exampleSet);
		return exampleSet;
	*/
		return result;
	}

	/** Returns the label attribute. */
	public IAttribute getLabel() {
		return getTrainingHeader().getAttributes().getLabel();
	}

	/**
	 * This method is invoked before the model is actually applied. The default implementation
	 * performs some basic compatibility checks and writes warnings if the given example set (for
	 * applying the model) does not fit the training example set. Subclasses might override this
	 * method and might throw exceptions which will prevent the application of the model.
	 */
	protected void checkCompatibility(IExampleSet exampleSet) throws OperatorException {
		IExampleSet trainingHeaderSet = getTrainingHeader();

		// check number of attributes
		if (exampleSet.getAttributes().regularSize() != trainingHeaderSet.getAttributes().regularSize()) {
			Logger.log("The number of regular attributes of the given example set does not fit the number of attributes of the training example set, training: "
					+ trainingHeaderSet.getAttributes().regularSize() + ", application: " + exampleSet.getAttributes().regularSize(), Level.WARNING);
		} else {
			// check order of attributes
			Iterator<IAttribute> trainingIt = trainingHeaderSet.getAttributes().iterator();
			Iterator<IAttribute> applyIt = exampleSet.getAttributes().iterator();
			while (trainingIt.hasNext() && applyIt.hasNext()) {
				if (!trainingIt.next().getName().equals(applyIt.next().getName())) {
					Logger.log("The order of attributes is not equal for the training and the application example set. This might lead to problems for some models.", Level.WARNING);
					break;
				}
			}
		}

		// check if all training attributes are part of the example set and have the same value
		// types and values
		for (IAttribute trainingAttribute : trainingHeaderSet.getAttributes()) {
			String name = trainingAttribute.getName();
			IAttribute attribute = exampleSet.getAttributes().getRegular(name);
			if (attribute == null) {
				Logger.log("The given example set does not contain a regular attribute with name '" + name
						+ "'. This might cause problems for some models depending on this particular attribute.", Level.WARNING);
			} else {
				if (!trainingAttribute.getColumnType().equals(attribute.getColumnType())) {
					Logger.log("The value types between training and application differ for attribute '" + name
							+ "', training: " + trainingAttribute.getColumnType()
							+ ", application: " + attribute.getColumnType(), Level.WARNING);
				} else {
					// check nominal values
					if (trainingAttribute.isNominal()) {
						if (trainingAttribute.getMapping().size() != attribute.getMapping().size()) {
							Logger.log("The number of nominal values is not the same for training and application for attribute '"
									+ name
									+ "', training: "
									+ trainingAttribute.getMapping().size()
									+ ", application: "
									+ attribute.getMapping().size(), Level.WARNING);
						} else {
							INominalMapping mapping = trainingAttribute.getMapping();
							List<String> values = mapping.getValues();
							for (String v : values) {
								int trainingIndex = trainingAttribute.getMapping().getIndex(v);
								int applicationIndex = attribute.getMapping().getIndex(v);
								if (trainingIndex != applicationIndex) {
									Logger.log("The internal nominal mappings are not the same between training and application for attribute '"
											+ name + "'. This will probably lead to wrong results during model application.", Level.WARNING);
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This method creates prediction attributes like the predicted label and confidences if needed.
	 */
	protected IAttribute createPredictionAttributes(IExampleSet exampleSet, IAttribute label) {
		// create and add prediction attribute
		IAttribute predictedLabel = AttributeFactory.createAttribute(label, EColumnRole.prediction.name());
		exampleSet.addNewColumn(predictedLabel);
		exampleSet.getAttributes().setPredictedLabel(predictedLabel);

		// check whether confidence labels should be constructed
		if (supportsConfidences(label)) {
			for (String value : predictedLabel.getMapping().getValues()) {
				IAttribute confidence = AttributeFactory.createAttribute(EColumnRole.confidence.name()+ "(" + value + ")",
						EColumnType.NUMERICAL);
				exampleSet.addNewColumn(confidence);
				exampleSet.getAttributes().setSpecialAttribute(confidence, EColumnRole.confidence.name() + "_" + value);
			}
			IAttribute confidence = AttributeFactory.createAttribute(EColumnRole.confidence.name(),
					EColumnType.NUMERICAL);
			exampleSet.addNewColumn(confidence);
			exampleSet.getAttributes().setSpecialAttribute(confidence, EColumnRole.confidence.name());
		}
		return predictedLabel;
	}

	/**
	 * This method determines if confidence attributes are created depending on the current label.
	 * Usually this depends only on the fact that the label is nominal, but subclasses might
	 * override this to avoid attribute construction for confidences.
	 */
	protected boolean supportsConfidences(IAttribute label) {
		return label != null && label.isNominal();
	}



}
