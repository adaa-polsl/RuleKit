package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;

import adaa.analytics.rules.logic.representation.ActionRuleSet;
import utils.InMemoryActionRuleRepository;
import utils.InMemoryDataSet;

public class ActionMetaTableTest {

	protected InMemoryActionRuleRepository repo;
	protected ExampleSet set;
	
	@Before
	public void prepare() {
		List<Attribute> atrs = new ArrayList<Attribute>();
		atrs.add(AttributeFactory.createAttribute("class", Ontology.NOMINAL));
		atrs.add(AttributeFactory.createAttribute("numerical1", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("numerical2", Ontology.NUMERICAL));
		atrs.add(AttributeFactory.createAttribute("nominal", Ontology.NOMINAL));
		
		List<String> data = Collections.unmodifiableList(Arrays.asList(
				"1, 15.0, 150.0, a",
				"1, 13.0, 151.0, a",
				"1, 12.0, 130.3, a",
				"2, 0.0, 150.0, b",
				"2, 1.0, 150.0, b",
				"2, -1.0, 150.0, b",
				"3, 0.0, 150.0, a",
				"3, 1.0, 150.0, a",
				"3, -1.0, 150.0, b"
				));
		
		set = (new InMemoryDataSet(atrs, data)).getExampleSet();
		
		repo = new InMemoryActionRuleRepository(set);
	}
	
	@Test
	public void testGetExamples() {
		
		ActionRuleSet actions = repo.getActionRulest();
		
		ActionRangeDistribution dist = new ActionRangeDistribution(actions, set);
		dist.calculateActionDistribution();
		ActionMetaTable table = new ActionMetaTable(dist);
	
		Set<MetaExample> examples = table.getExamples();
		System.out.println("Count:" + examples.size());
		
		for (MetaExample ex : examples) {
			System.out.println(ex);
		}
		
	}

}
