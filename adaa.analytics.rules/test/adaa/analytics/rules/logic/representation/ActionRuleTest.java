package adaa.analytics.rules.logic.representation;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ActionRuleTest {
    protected Rule sourceRule;
    protected Rule targetRule;
    protected ActionRule actionRule;

    @Before
    public void prepare() {
        List<String> mapping = ImmutableList.of("good", "bad");

        ElementaryCondition ec_left_one = new ElementaryCondition("attribute1", new Interval(3.0, 5.0, false, false));
        ElementaryCondition ec_righ_one = new ElementaryCondition("attribute1", new Interval(5.0, 6.0, false, false));
        ElementaryCondition ec_left_two = new ElementaryCondition("attribute2", new Interval( 0.0, 1.0, false, false)) ;
        ElementaryCondition ec_right_two = new ElementaryCondition("attribute2", new Interval( 10.0, 11.0, false, false)) ;

        ElementaryCondition conclusionLeft = new ElementaryCondition("class", new SingletonSet(0.0, mapping));
        ElementaryCondition conclusionRight = new ElementaryCondition("class", new SingletonSet(1.0, mapping));

        CompoundCondition premiseLeft = new CompoundCondition();
        premiseLeft.addSubcondition(ec_left_one);
        premiseLeft.addSubcondition(ec_left_two);

        CompoundCondition premiseRight = new CompoundCondition();
        premiseRight.addSubcondition(ec_righ_one);
        premiseRight.addSubcondition(ec_right_two);

        sourceRule = new ClassificationRule(premiseLeft, conclusionLeft);
        targetRule = new ClassificationRule(premiseRight, conclusionRight);

        CompoundCondition actionPremise = new CompoundCondition();
        actionPremise.addSubcondition(new Action(ec_left_one, ec_righ_one));
        actionPremise.addSubcondition(new Action(ec_left_two, ec_right_two));

        actionRule = new ActionRule(actionPremise, new Action(conclusionLeft, conclusionRight));
    }

    @Test
    public void getLeftRule_ExpectSourceRule() {
        Assert.assertTrue(actionRule.getLeftRule().equals(sourceRule));
    }

    @Test
    public void getRightRule_ExpectTargetRule() {
        Assert.assertEquals(actionRule.getRightRule(), targetRule);
    }
}