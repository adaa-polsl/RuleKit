package adaa.analytics.rules.logic.induction;

import java.util.Set;

import adaa.analytics.rules.logic.representation.Rule;

import com.rapidminer.example.ExampleSet;

public interface IExpertFinder {
	public void adjust(
			Rule rule,
			ExampleSet dataset, 
			Set<Integer> uncovered);
}
