package adaa.analytics.rules.rm.comp;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.metadata.EColumnRole;
import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import adaa.analytics.rules.data.IExampleSet;
import org.junit.Assert;
import org.junit.Test;

public class TsExampleSetTest {

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

    /**
     * Create TsExampleSet with:
     * - decisionAttribute
     * - survivalTimeAttribute
     * - contrastAttribute
     */
    @Test
    public void CreateTsExampleSetAllTest() {

        IExampleSet es = new DataTable(crateValues(), crateAttsNames(), "l_att", "s_att", "c_att");

        Assert.assertEquals(3, es.getAttributes().regularSize());
        Assert.assertEquals("l_att", es.getAttributes().getLabel().getName());
        Assert.assertEquals("s_att", es.getAttributes().getColumnByRole(EColumnRole.survival_time.name()).getName());
        Assert.assertEquals("c_att", es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getName());
        Assert.assertEquals(5, es.size());
        Assert.assertEquals(2, es.getAttributes().getLabel().getMapping().getValues().size());
        Assert.assertEquals(3, es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getMapping().getValues().size());
        Assert.assertEquals(1.0, es.getExample(0).getValue(es.getAttributes().get("s_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("l_att").getMapping().mapString("val2"), es.getExample(1).getValue(es.getAttributes().get("l_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("c_att").getMapping().mapString("c2"), es.getExample(1).getValue(es.getAttributes().get("c_att")), 0.0000001);
        Assert.assertEquals(0.0, es.getExample(2).getValue(es.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(1, es.getExample(3).getValue(es.getAttributes().get("att_2")), 0.0000001);
        Assert.assertEquals(7, es.getExample(4).getValue(es.getAttributes().get("att_3")), 0.0000001);
    }

    /**
     * Create TsExampleSet with:
     * - decisionAttribute
     */
    @Test
    public void CreateTsExampleSetLabelTest() {

        IExampleSet es = new DataTable(crateValues(), crateAttsNames(), "l_att", null, null);

        Assert.assertEquals(5, es.getAttributes().regularSize());
        Assert.assertEquals("l_att", es.getAttributes().getLabel().getName());
        Assert.assertNull(es.getAttributes().getColumnByRole(EColumnRole.survival_time.name()));
        Assert.assertNull(es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE));
        Assert.assertEquals(5, es.size());
        Assert.assertEquals(2, es.getAttributes().getLabel().getMapping().getValues().size());
        Assert.assertEquals(1.0, es.getExample(0).getValue(es.getAttributes().get("s_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("l_att").getMapping().mapString("val2"), es.getExample(1).getValue(es.getAttributes().get("l_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("c_att").getMapping().mapString("c2"), es.getExample(1).getValue(es.getAttributes().get("c_att")), 0.0000001);
        Assert.assertEquals(0.0, es.getExample(2).getValue(es.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(1, es.getExample(3).getValue(es.getAttributes().get("att_2")), 0.0000001);
        Assert.assertEquals(7, es.getExample(4).getValue(es.getAttributes().get("att_3")), 0.0000001);
    }

    /**
     * Create TsExampleSet with:
     * - survivalTimeAttribute
     */
    @Test
    public void CreateTsExampleSetSurvivalTest() {

        IExampleSet es = new DataTable(crateValues(), crateAttsNames(), null, "s_att", null);

        Assert.assertEquals(5, es.getAttributes().regularSize());
        Assert.assertNull(es.getAttributes().getLabel());
        Assert.assertEquals("s_att", es.getAttributes().getColumnByRole(EColumnRole.survival_time.name()).getName());
        Assert.assertNull(es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE));
        Assert.assertEquals(5, es.size());
        Assert.assertEquals(2, es.getAttributes().get("l_att").getMapping().getValues().size());
        Assert.assertEquals(3, es.getAttributes().get("c_att").getMapping().getValues().size());
        Assert.assertEquals(1.0, es.getExample(0).getValue(es.getAttributes().get("s_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("l_att").getMapping().mapString("val2"), es.getExample(1).getValue(es.getAttributes().get("l_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("c_att").getMapping().mapString("c2"), es.getExample(1).getValue(es.getAttributes().get("c_att")), 0.0000001);
        Assert.assertEquals(0.0, es.getExample(2).getValue(es.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(1, es.getExample(3).getValue(es.getAttributes().get("att_2")), 0.0000001);
        Assert.assertEquals(7, es.getExample(4).getValue(es.getAttributes().get("att_3")), 0.0000001);
    }

    /**
     * Create TsExampleSet with:
     * - contrastAttribute
     */
    @Test
    public void CreateTsExampleSetContrastTest() {

        IExampleSet es = new DataTable(crateValues(), crateAttsNames(), null, null, "c_att");

        Assert.assertEquals(5, es.getAttributes().regularSize());
        Assert.assertNull(es.getAttributes().getLabel());
        Assert.assertNull(es.getAttributes().getColumnByRole(EColumnRole.survival_time.name()));
        Assert.assertEquals("c_att", es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getName());
        Assert.assertEquals(5, es.size());
        Assert.assertEquals(2, es.getAttributes().get("l_att").getMapping().getValues().size());
        Assert.assertEquals(3, es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getMapping().getValues().size());
        Assert.assertEquals(1.0, es.getExample(0).getValue(es.getAttributes().get("s_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("l_att").getMapping().mapString("val2"), es.getExample(1).getValue(es.getAttributes().get("l_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("c_att").getMapping().mapString("c2"), es.getExample(1).getValue(es.getAttributes().get("c_att")), 0.0000001);
        Assert.assertEquals(0.0, es.getExample(2).getValue(es.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(1, es.getExample(3).getValue(es.getAttributes().get("att_2")), 0.0000001);
        Assert.assertEquals(7, es.getExample(4).getValue(es.getAttributes().get("att_3")), 0.0000001);
    }

    /**
     * Create TsExampleSet with:
     * - decisionAttribute
     * - survivalTimeAttribute
     */
    @Test
    public void CreateTsExampleSetLabelSurvivalTest() {

        IExampleSet es = new DataTable(crateValues(), crateAttsNames(), "l_att", "s_att", null);

        Assert.assertEquals(4, es.getAttributes().regularSize());
        Assert.assertEquals("l_att", es.getAttributes().getLabel().getName());
        Assert.assertEquals("s_att", es.getAttributes().getColumnByRole(EColumnRole.survival_time.name()).getName());
        Assert.assertNull(es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE));
        Assert.assertEquals(5, es.size());
        Assert.assertEquals(2, es.getAttributes().getLabel().getMapping().getValues().size());
        Assert.assertEquals(3, es.getAttributes().get("c_att").getMapping().getValues().size());
        Assert.assertEquals(1.0, es.getExample(0).getValue(es.getAttributes().get("s_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("l_att").getMapping().mapString("val2"), es.getExample(1).getValue(es.getAttributes().get("l_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("c_att").getMapping().mapString("c2"), es.getExample(1).getValue(es.getAttributes().get("c_att")), 0.0000001);
        Assert.assertEquals(0.0, es.getExample(2).getValue(es.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(1, es.getExample(3).getValue(es.getAttributes().get("att_2")), 0.0000001);
        Assert.assertEquals(7, es.getExample(4).getValue(es.getAttributes().get("att_3")), 0.0000001);
    }

    /**
     * Create TsExampleSet with:
     * - decisionAttribute
     * - contrastAttribute
     */
    @Test
    public void CreateTsExampleSetLabelContrastTest() {

        IExampleSet es = new DataTable(crateValues(), crateAttsNames(), "l_att", null, "c_att");

        Assert.assertEquals(4, es.getAttributes().regularSize());
        Assert.assertEquals("l_att", es.getAttributes().getLabel().getName());
        Assert.assertNull(es.getAttributes().getColumnByRole(EColumnRole.survival_time.name()));
        Assert.assertEquals("c_att", es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getName());
        Assert.assertEquals(5, es.size());
        Assert.assertEquals(2, es.getAttributes().getLabel().getMapping().getValues().size());
        Assert.assertEquals(3, es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getMapping().getValues().size());
        Assert.assertEquals(1.0, es.getExample(0).getValue(es.getAttributes().get("s_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("l_att").getMapping().mapString("val2"), es.getExample(1).getValue(es.getAttributes().get("l_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("c_att").getMapping().mapString("c2"), es.getExample(1).getValue(es.getAttributes().get("c_att")), 0.0000001);
        Assert.assertEquals(0.0, es.getExample(2).getValue(es.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(1, es.getExample(3).getValue(es.getAttributes().get("att_2")), 0.0000001);
        Assert.assertEquals(7, es.getExample(4).getValue(es.getAttributes().get("att_3")), 0.0000001);
    }

    /**
     * Create TsExampleSet with:
     * - survivalTimeAttribute
     * - contrastAttribute
     */
    @Test
    public void CreateTsExampleSetSurvivalContrastTest() {

        IExampleSet es = new DataTable(crateValues(), crateAttsNames(), null, "s_att", "c_att");

        Assert.assertEquals(4, es.getAttributes().regularSize());
        Assert.assertNull(es.getAttributes().getLabel());
        Assert.assertEquals("s_att", es.getAttributes().getColumnByRole(EColumnRole.survival_time.name()).getName());
        Assert.assertEquals("c_att", es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getName());
        Assert.assertEquals(5, es.size());
        Assert.assertEquals(2, es.getAttributes().get("l_att").getMapping().getValues().size());
        Assert.assertEquals(3, es.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE).getMapping().getValues().size());
        Assert.assertEquals(1.0, es.getExample(0).getValue(es.getAttributes().get("s_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("l_att").getMapping().mapString("val2"), es.getExample(1).getValue(es.getAttributes().get("l_att")), 0.0000001);
        Assert.assertEquals(es.getAttributes().get("c_att").getMapping().mapString("c2"), es.getExample(1).getValue(es.getAttributes().get("c_att")), 0.0000001);
        Assert.assertEquals(0.0, es.getExample(2).getValue(es.getAttributes().get("att_1")), 0.0000001);
        Assert.assertEquals(1, es.getExample(3).getValue(es.getAttributes().get("att_2")), 0.0000001);
        Assert.assertEquals(7, es.getExample(4).getValue(es.getAttributes().get("att_3")), 0.0000001);
    }
}
