package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import adaa.analytics.rules.logic.representation.Interval;

public class IntersectionFinderTest {
	
	protected IntersectionFinder finder;
	
	
	@Before
	public void SetUp() {
		
		finder = new IntersectionFinder();
	}

	@Test
	public void testCalculateAllIntersectionsOf_() {
		
		List<Interval> intervals = new ArrayList<Interval>();
		intervals.add(new Interval(-1, 5, true, true));
		intervals.add(new Interval(3, 10, true, true));
		intervals.add(new Interval(7, 15, true, true));
		
		List<Interval> expected = new ArrayList<Interval>();
		expected.add(Interval.create_le(-1));
		expected.add(new Interval(-1, 3, true, true));
		expected.add(new Interval(3, 5, true, true));
		expected.add(new Interval(5, 7, true, true));
		expected.add(new Interval(7, 10, true, true));
		expected.add(new Interval(10, 15, true, true));
		expected.add(Interval.create_geq(15));
		
		List<Interval> actual = finder.calculateAllIntersectionsOf(intervals);
		
		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}
	
	@Test
	public void testCalculateIntersection() {
		
		List<Interval> intervals = new ArrayList<Interval>();
		intervals.add(new Interval(1,5, true, true));
		intervals.add(new Interval(3,7, true, true));
		intervals.add(new Interval(2,6, true, true));
		intervals.add(new Interval(6.5,9, true, true));
		intervals.add(new Interval(8,12, true, true));
		
		List<Interval> actual = finder.calculateAllIntersectionsOf(intervals);
		System.out.println(actual);
	}
	
	@Test
	public void testNonContionusValues() {
		
		List<Interval> intervals = new ArrayList<Interval>();
		intervals.add(new Interval(-1, 5, true, true));
		intervals.add(new Interval(7, 15, true, true));
		
		List<Interval> expected = new ArrayList<Interval>();
		expected.add(Interval.create_le(-1));
		
		expected.add(new Interval(-1, 5, true, true));
		expected.add(new Interval(5, 7, true, true));
		expected.add(new Interval(7, 15, true, true));
		
		expected.add(Interval.create_geq(15));
		
		List<Interval> actual = finder.calculateAllIntersectionsOf(intervals);
		
		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}

}
