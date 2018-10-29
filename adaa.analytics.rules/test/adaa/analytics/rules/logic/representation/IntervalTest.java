package adaa.analytics.rules.logic.representation;

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

}
