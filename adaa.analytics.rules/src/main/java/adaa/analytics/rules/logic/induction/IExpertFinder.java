package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.Rule;
import com.rapidminer.example.ExampleSet;

import java.util.Set;

public interface IExpertFinder {
	public void adjust(
			Rule rule,
			ExampleSet dataset, 
			Set<Integer> uncovered);
}
