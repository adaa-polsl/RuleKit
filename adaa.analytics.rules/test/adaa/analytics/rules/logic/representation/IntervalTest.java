package adaa.analytics.rules.logic.representation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class IntervalTest {

	@Test
	public void testGetDifference() {
		Interval i1 = new Interval(1.0, 5.0, true, true);
		Interval i2 = new Interval(2.0, 3.0, true, true);
		Interval o1 = new Interval(1.0, 2.0, true, true);
		Interval o2 = new Interval(3.0, 5.0, true, true);
		List<IValueSet> common =  i1.getDifference(i2);
		Assert.assertEquals(2, common.size());
		Assert.assertTrue(common.get(0).equals(o1));
		Assert.assertTrue(common.get(1).equals(o2));
		
		i2 = new Interval(4.0, 6.0, true, true);
		common = i1.getDifference(i2);
		Assert.assertEquals(1, common.size());
		Assert.assertEquals(common.get(0), new Interval(1.0, 4.0, true, true));
		
		i2 = new Interval(-1.0, 2.0, true, true);
		common = i1.getDifference(i2);
		Assert.assertEquals(1, common.size());
		Assert.assertEquals(common.get(0), new Interval(2.0, 5.0, true, true));
	}

	@Test
	public void getIntersection_commonPart() {
		// Given
		Interval i1 = new Interval(3.5, Interval.INF, true, false);
		Interval i2 = new Interval(1.5, Interval.INF, true, false);
		Interval expected = new Interval(3.5, Interval.INF, true, false);

		// When
		IValueSet intersection1 = i1.getIntersection(i2);
		Interval intersectionInterval1 = (Interval)intersection1;
		IValueSet intersection2 = i2.getIntersection(i1);
		Interval intersectionInterval2 = (Interval)intersection2;

		// Then
		assertEquals(expected, intersectionInterval1);
		assertTrue(intersectionInterval1.equals(intersectionInterval2));
	}

	@Test
	public void getIntersection_noCommonPart() {
		// Given
		Interval i1 = new Interval(7, 10, true, false);
		Interval i2 = new Interval(1, 4, true, false);

		// When
		IValueSet intersection1 = i1.getIntersection(i2);
		Interval intersectionInterval1 = (Interval)intersection1;
		IValueSet intersection2 = i2.getIntersection(i1);
		Interval intersectionInterval2 = (Interval)intersection2;

		// Then
		assertEquals(null, intersectionInterval2);
		assertEquals(null, intersectionInterval1);
	}

	@Test
	public void getIntersection_AnyValueSet() {
		// Given
		Interval i1 = new Interval(4, 10, true, false);
		AnyValueSet i2 = new AnyValueSet();

		// When
		IValueSet intersection1 = i1.getIntersection(i2);
		Interval intersectionInterval1 = (Interval)intersection1;
		IValueSet intersection2 = i2.getIntersection(i1);
		Interval intersectionInterval2 = (Interval)intersection2;

		// Then
		assertEquals(i1, intersectionInterval1);
		assertTrue(intersectionInterval1.equals(intersectionInterval2));
	}
}
