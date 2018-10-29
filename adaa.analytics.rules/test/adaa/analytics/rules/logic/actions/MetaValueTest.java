package adaa.analytics.rules.logic.actions;

import static org.junit.Assert.*;

import org.junit.Test;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Interval;

public class MetaValueTest {

	@Test
	public void test() {
		ElementaryCondition ec = new ElementaryCondition("numeric1", new Interval(0.0, 5.0, true, true));
		MetaValue mv = new MetaValue(ec, null);
		
		assertTrue(mv.contains(3.0));
	}

}
