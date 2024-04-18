package adaa.analytics.rules.rm.comp;

import adaa.analytics.rules.data.EColumnRole;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;
import org.junit.Assert;
import org.junit.Test;

public class TsExampleSetTest {

    @Test
    public void CreateTsExampleSetTest() throws Exception {

        Object[][] values = new Object[][]{
                {1.0, "val1", true, 5, 6},
                {2.0, "val2", false, 4, 3},
                {4.0, "val1", false, 7, 2},
                {10.0, "val1", true, 1, 6},
                {5.0, "val2", false, 2, 7}
        };

        String[] attsName = new String[]{ "d_att", "t_att", "b_att", "att_1", "att_2" };

        IExampleSet es = new TsExampleSet(values, attsName, "t_att", "d_att");

        Assert.assertEquals(3, es.getAttributes().size());
        Assert.assertEquals("t_att", es.getAttributes().getLabel().getName());
        Assert.assertEquals("d_att", es.getAttributes().getSpecial(EColumnRole.survival_time.name()).getName());
        Assert.assertEquals(5, es.size());
        Assert.assertEquals(2, es.getAttributes().getLabel().getMapping().getValues().size());
        Assert.assertEquals(1.0, es.getExample(0).getDataRow().get(es.getAttributes().get("d_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("t_att").getMapping().mapString("val2"), es.getExample(1).getDataRow().get(es.getAttributes().get("t_att")), 0.0000001);
        Assert.assertEquals(0.0, es.getExample(2).getDataRow().get(es.getAttributes().get("b_att")), 0.0000001);
        Assert.assertEquals(1, es.getExample(3).getDataRow().get(es.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(7, es.getExample(4).getDataRow().get(es.getAttributes().get("att_2")), 0.0000001);
    }
}
