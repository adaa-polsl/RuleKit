package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;

public interface IContrastExampleSet extends ExampleSet {

    public Attribute getContrastAttribute();
}
