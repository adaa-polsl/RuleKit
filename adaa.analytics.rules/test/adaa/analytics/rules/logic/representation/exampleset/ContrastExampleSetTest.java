package adaa.analytics.rules.logic.representation.exampleset;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.metadata.EColumnRole;
import org.junit.Assert;
import org.junit.Test;

public class ContrastExampleSetTest {

    private Object[][] crateValues() {
        return new Object[][]{
                {1.0, "val1", "c1", true, 5, 6},
                {2.0, "val2", "c2", false, 4, 3},
                {4.0, "val1", "c3", false, 7, 2},
                {10.0, "val1", "c2", true, 1, 6},
                {5.0, "val2", "c1", false, 2, 7}
        };
    }

    private String[] crateAttsNames() {
        return new String[]{ "s_att", "l_att", "c_att", "att_1", "att_2", "att_3" };
    }

    @Test
    public void CreateTsExampleSetContrastTest() {

        DataTable tsExampleSet = new DataTable(crateValues(), crateAttsNames(), null, null, "c_att");
        ContrastExampleSet cExampleSet = new ContrastExampleSet(tsExampleSet);

        Assert.assertEquals(5, cExampleSet.getAttributes().regularSize());
        Assert.assertNull(cExampleSet.getAttributes().getLabel());
        Assert.assertNull(cExampleSet.getAttributes().getColumnByRole(EColumnRole.survival_time.name()));
        Assert.assertEquals("c_att", cExampleSet.getContrastAttribute().getName());
        Assert.assertEquals(5, cExampleSet.size());
        Assert.assertEquals(2, cExampleSet.getAttributes().get("l_att").getMapping().getValues().size());
        Assert.assertEquals(3, cExampleSet.getContrastAttribute().getMapping().getValues().size());
        Assert.assertEquals(1.0, cExampleSet.getExample(0).getValue(cExampleSet.getAttributes().get("s_att")), 0.0000001);
        Assert.assertEquals(cExampleSet.getAttributes().get("l_att").getMapping().mapString("val2"), cExampleSet.getExample(1).getValue(cExampleSet.getAttributes().get("l_att")), 0.0000001);
        Assert.assertEquals(cExampleSet.getAttributes().get("c_att").getMapping().mapString("c2"), cExampleSet.getExample(1).getValue(cExampleSet.getAttributes().get("c_att")), 0.0000001);
        Assert.assertEquals(0.0, cExampleSet.getExample(2).getValue(cExampleSet.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(1, cExampleSet.getExample(3).getValue(cExampleSet.getAttributes().get("att_2")), 0.0000001);
        Assert.assertEquals(7, cExampleSet.getExample(4).getValue(cExampleSet.getAttributes().get("att_3")), 0.0000001);
    }
}
