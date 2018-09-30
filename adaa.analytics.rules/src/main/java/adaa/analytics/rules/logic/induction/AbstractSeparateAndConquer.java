package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.RuleSetBase;
import com.rapidminer.example.ExampleSet;

/***
 * Base abstract class for all separate'n'conquer rule induction algorithms.
 * @author Adam
 *
 */
public abstract class AbstractSeparateAndConquer {

	/**
	 * Induction parameters.
	 */
	protected final InductionParameters params;
	
	protected RuleFactory factory;
	
	/**
	 * Sets induction parameters.
	 * @param params
	 */
	public AbstractSeparateAndConquer(final InductionParameters params) {
		this.params = params;
	}
	
	/**
	 * Trains a rule classifier.
	 * @param trainSet Training set.
	 * @return Rule set.
	 */
	public abstract RuleSetBase run(final ExampleSet trainSet);
}
