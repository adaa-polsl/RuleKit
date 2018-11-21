package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.RuleSetBase;
import com.rapidminer.example.ExampleSet;

/***
 * Abstract base class for all separate and conquer rule-based models induction algorithms 
 * (classification, regression, survival).
 * 
 * @author Adam Gudyœ
 * 
 */
public abstract class AbstractSeparateAndConquer {

	/**
	 * Induction parameters.
	 */
	protected final InductionParameters params;
	
	/**
	 * Rule factory.
	 */
	protected RuleFactory factory;
	
	/**
	 * Sets induction parameters.
	 * @param params Induction paremeters.
	 */
	public AbstractSeparateAndConquer(final InductionParameters params) {
		this.params = params;
	}
	
	/**
	 * Trains a rule classifier.
	 * @param trainSet Training set.
	 * @return Rule-based model.
	 */
	public abstract RuleSetBase run(final ExampleSet trainSet);
}
