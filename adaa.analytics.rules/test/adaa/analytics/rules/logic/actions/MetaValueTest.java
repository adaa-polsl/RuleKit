package adaa.analytics.rules.logic.actions;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Interval;
import utils.InMemoryDataSet;

public class MetaValueTest {

	@Test
	public void test() {
		List<Attribute> atrs = new ArrayList<Attribute>();
		atrs.add(AttributeFactory.createAttribute("class", Ontology.NOMINAL));
		atrs.add(AttributeFactory.createAttribute("numerical1", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("numerical2", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("nominal", Ontology.NOMINAL));
		
		List<String> data = Collections.unmodifiableList(Arrays.asList(
				"1, 3.0, 150.0, a"
				));
		
		ExampleSet set = (new InMemoryDataSet(atrs, data)).getExampleSet();
		
		Example ex = set.getExample(0);
		
		ElementaryCondition ec = new ElementaryCondition("numerical1", new Interval(0.0, 5.0, true, true));
		MetaValue mv = new MetaValue(ec, null);
		
		assertTrue(mv.contains(ex));
	}

}
