package adaa.analytics.rules.logic.actions;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rapidminer.RapidMiner;
import org.junit.Test;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.SingletonSet;
import utils.InMemoryDataSet;

public class MetaExampleTest {

	@Test
	public void test() {
		RapidMiner.init();
		MetaExample me = new MetaExample();
		List<String> mapping = new ArrayList<>();
		mapping.add("a");
		mapping.add("b");
		
		ElementaryCondition ec1 = new ElementaryCondition("numerical1", new Interval(0.0, 25.0, true, true));
		DistributionEntry de1 = new DistributionEntry();
		MetaValue mv1 = new MetaValue(ec1, de1);
		
		ElementaryCondition ec2 = new ElementaryCondition("nominal", new SingletonSet(0.0, mapping));
		MetaValue mv2 = new MetaValue(ec2, de1);
		
		me.add(mv1);
		me.add(mv2);
		
		List<Attribute> atrs = new ArrayList<>();
		atrs.add(AttributeFactory.createAttribute("class", Ontology.NOMINAL));
		atrs.add(AttributeFactory.createAttribute("numerical1", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("numerical2", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("nominal", Ontology.NOMINAL));
		
		List<String> data = Collections.unmodifiableList(Arrays.asList(
				"1, 15.0, 150.0, a"
				));
		
		ExampleSet set = (new InMemoryDataSet(atrs, data)).getExampleSet();
		
		Example ex = set.getExample(0);
		
		assertTrue(me.covers(ex));
	}


	@Test
	public void testMetaExamplesEquality_WhenTheSameOrder() {
		MetaExample me = new MetaExample();
		MetaExample me2 = new MetaExample();
		List<String> mapping = new ArrayList<>();
		mapping.add("a");
		mapping.add("b");

		ElementaryCondition ec1 = new ElementaryCondition("numerical1", new Interval(0.0, 25.0, true, true));
		DistributionEntry de1 = new DistributionEntry();
		MetaValue mv1 = new MetaValue(ec1, de1);

		ElementaryCondition ec2 = new ElementaryCondition("nominal", new SingletonSet(0.0, mapping));
		MetaValue mv2 = new MetaValue(ec2, de1);

		me.add(mv1);
		me.add(mv2);

		me2.add(mv1);
		me2.add(mv2);

		assertEquals(me, me2);
		assertEquals(me2, me);
	}

	@Test
	public void testMetaExamplesEquality_WhenTheSameButDifferInOrderOfMetaValues() {
		MetaExample me = new MetaExample();
		MetaExample me2 = new MetaExample();
		List<String> mapping = new ArrayList<>();
		mapping.add("a");
		mapping.add("b");

		ElementaryCondition ec1 = new ElementaryCondition("numerical1", new Interval(0.0, 25.0, true, true));
		DistributionEntry de1 = new DistributionEntry();
		MetaValue mv1 = new MetaValue(ec1, de1);

		ElementaryCondition ec2 = new ElementaryCondition("nominal", new SingletonSet(0.0, mapping));
		MetaValue mv2 = new MetaValue(ec2, de1);

		me.add(mv1);
		me.add(mv2);

		me2.add(mv2);
		me2.add(mv1);

		assertEquals(me, me2);
		assertEquals(me2, me);
	}
}
