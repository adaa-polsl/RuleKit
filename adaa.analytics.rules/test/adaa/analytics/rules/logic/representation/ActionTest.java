package adaa.analytics.rules.logic.representation;


import org.junit.Assert;
import org.junit.Test;

public class ActionTest {

    @Test
    public void reversedAction() {
        Interval left = new Interval(1.0, 2.0, false, false);
        Interval right = new Interval(2.0, 3.0, false, false);
        Action action = new Action("attribute", left, right);
        Action expectedReversedAction = new Action("attribute", right, left);


        Action reversedAction = Action.ReversedAction(action);

        Assert.assertEquals(expectedReversedAction, reversedAction);
    }

    @Test
    public void isLeftEqualRight_notNil_expectTrue() {
        Interval interval = new Interval(1.0, 2.0, false, false);
        Action action = new Action("atr", interval, interval);


        Assert.assertTrue(action.isLeftEqualRight());
    }

    @Test
    public void isLeftEqualRight_actionNil_expectFalse() {
        Interval interval = new Interval(1.0, 2.0, false, false);
        Action action = new Action("atr", interval, null);


        Assert.assertFalse(action.isLeftEqualRight());
    }

    @Test
    public void intersect_sourceSetsIntersecting() {
        Action left = new Action("atr1", new Interval(1.0, 10.0, false, false), new Interval(100.0, 110.0, false, false));
        Action right = new Action("atr1", new Interval(-5.0, 5.0, false, false), new Interval(1.0, 10.0, false, false));

        Action expected = new Action("atr1", new Interval(1.0, 5.0, false, false), null);

        ElementaryCondition intersection = left.intersect(right);

        Assert.assertEquals(expected, intersection);
    }

    @Test
    public void intersect_sourceAndTargetSetsIntersecting() {
        Action left = new Action("atr1", new Interval(1.0, 10.0, false, false), new Interval(100.0, 110.0, false, false));
        Action right = new Action("atr1", new Interval(-5.0, 5.0, false, false), new Interval(101.0, 109.0, false, false));

        Action expected = new Action("atr1", new Interval(1.0, 5.0, false, false), new Interval(101.0, 109.0, false, false));

        ElementaryCondition intersection = left.intersect(right);


        Assert.assertEquals(expected, intersection);
    }

    @Test
    public void intersect_withNilActionOnTheRight() {
        Action left = new Action("atr1", new Interval(1.0, 10.0, false, false), new Interval(100.0, 110.0, false, false));
        Action right = new Action("atr1", new Interval(-5.0, 5.0, false, false), null);

        Action expected = new Action("atr1", new Interval(1.0, 5.0, false, false), new Interval(100.0, 110.0, false, false));

        ElementaryCondition intersection = left.intersect(right);

        Assert.assertEquals(expected, intersection);
    }

    @Test
    public void intersect_withNilActionOnTheLeft() {
        Action left = new Action("atr1", new Interval(1.0, 10.0, false, false), null);
        Action right = new Action("atr1", new Interval(-5.0, 5.0, false, false), new Interval(100.0, 110.0, false, false));

        Action expected = new Action("atr1", new Interval(1.0, 5.0, false, false), new Interval(100.0, 110.0, false, false));

        ElementaryCondition intersection = left.intersect(right);

        Assert.assertEquals(expected, intersection);
    }

    @Test
    public void intersect_withNilActionOnBoth() {
        Action left = new Action("atr1", new Interval(1.0, 10.0, false, false), null);
        Action right = new Action("atr1", new Interval(-5.0, 5.0, false, false), null);

        Action expected = new Action("atr1", new Interval(1.0, 5.0, false, false), null);

        ElementaryCondition intersection = left.intersect(right);

        Assert.assertEquals(expected, intersection);
    }
}