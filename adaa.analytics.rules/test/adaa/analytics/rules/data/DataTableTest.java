package adaa.analytics.rules.data;

import adaa.analytics.rules.data.metadata.EColumnRole;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.metadata.ESortAlgorithm;
import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import ioutils.ArffFileLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class DataTableTest {

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
    public void CreateDataTableAllTest() {

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
    public void CreateDataTableLabelTest() {

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
    public void CreateDataTableSurvivalTest() {

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
    public void CreateDataTableContrastTest() {

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
    public void CreateDataTableLabelSurvivalTest() {

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
    public void CreateDataTableLabelContrastTest() {

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
    public void CreateDataTableSurvivalContrastTest() {

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

    @Test
    public void UpdateMappingTest() throws IOException {

        String workingDir = System.getProperty("user.dir");
        String dataDir1 = Paths.get(workingDir, "/test/resources/data/unit-test-mapping-1.arff").toString();
        String dataDir2 = Paths.get(workingDir, "/test/resources/data/unit-test-mapping-2.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es1 = arffFileLoader.loadDataTable(dataDir1, "", "");
        IExampleSet es2 = arffFileLoader.loadDataTable(dataDir2, "", "");

        IExampleSet es3 = es2.updateMapping(es1);

        Assert.assertNotNull(es1);
        Assert.assertNotNull(es2);
        Assert.assertNotNull(es3);

        Assert.assertNotEquals(es1.getAttributes().get("att").getMapping().getIndex("val1"), es2.getAttributes().get("att").getMapping().getIndex("val1"));
        Assert.assertEquals(es1.getAttributes().get("att").getMapping().getIndex("val1"), es3.getAttributes().get("att").getMapping().getIndex("val1"));
    }

    @Test
    public void BubbleSortTest() {

        Object [][] data = new Object[][]{
                {1.0, "val1"},
                {4.0, "val2"},
                {10.0, "val1"},
                {3.0, "val2"},
                {7.0, "val1"},
                {11.0, "val2"},
                {2.0, "val1"},
                {5.0, "val2"},
                {4.0, "val1"},
                {3.0, "val2"}
        };

        String [] colNames = new String[]{ "c1", "c2" };

        DataTable dt = new DataTable(data, colNames, "c2", "c1", null);

        dt.customSort("c2", EColumnSortDirections.INCREASING, ESortAlgorithm.BubbleSort);

        Assert.assertEquals(11.0, dt.getExample(2).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(2).getValue("c2"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(5).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(5).getValue("c2"), 0.0000001);
        Assert.assertEquals(4.0, dt.getExample(9).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(9).getValue("c2"), 0.0000001);

        dt.sortBy("c1", EColumnSortDirections.INCREASING);

        Assert.assertEquals(3.0, dt.getExample(2).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(2).getValue("c2"), 0.0000001);
        Assert.assertEquals(4.0, dt.getExample(5).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(5).getValue("c2"), 0.0000001);
        Assert.assertEquals(11.0, dt.getExample(9).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(9).getValue("c2"), 0.0000001);

        dt.sortBy("c2", EColumnSortDirections.DECREASING);

        Assert.assertEquals(4.0, dt.getExample(2).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(2).getValue("c2"), 0.0000001);
        Assert.assertEquals(3.0, dt.getExample(5).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(5).getValue("c2"), 0.0000001);
        Assert.assertEquals(11.0, dt.getExample(9).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(9).getValue("c2"), 0.0000001);

        dt.sortBy("c1", EColumnSortDirections.DECREASING);

        Assert.assertEquals(7.0, dt.getExample(2).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(2).getValue("c2"), 0.0000001);
        Assert.assertEquals(4.0, dt.getExample(5).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(5).getValue("c2"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(9).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(9).getValue("c2"), 0.0000001);
    }

    @Test
    public void QuickSortTest() {

        Object [][] data = new Object[][]{
                {1.0, "val1"},
                {4.0, "val2"},
                {10.0, "val1"},
                {3.0, "val2"},
                {7.0, "val1"},
                {11.0, "val2"},
                {2.0, "val1"},
                {5.0, "val2"},
                {4.0, "val1"},
                {3.0, "val2"}
        };

        String [] colNames = new String[]{ "c1", "c2" };

        DataTable dt = new DataTable(data, colNames, "c2", "c1", null);

        dt.customSort("c2", EColumnSortDirections.INCREASING, ESortAlgorithm.QuickSort);

        Assert.assertEquals(11.0, dt.getExample(2).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(2).getValue("c2"), 0.0000001);
        Assert.assertEquals(10.0, dt.getExample(5).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(5).getValue("c2"), 0.0000001);
        Assert.assertEquals(7.0, dt.getExample(9).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(9).getValue("c2"), 0.0000001);

        dt.sortBy("c1", EColumnSortDirections.INCREASING);

        Assert.assertEquals(3.0, dt.getExample(2).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(2).getValue("c2"), 0.0000001);
        Assert.assertEquals(4.0, dt.getExample(5).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(5).getValue("c2"), 0.0000001);
        Assert.assertEquals(11.0, dt.getExample(9).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(9).getValue("c2"), 0.0000001);

        dt.sortBy("c2", EColumnSortDirections.DECREASING);

        Assert.assertEquals(4.0, dt.getExample(2).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(2).getValue("c2"), 0.0000001);
        Assert.assertEquals(3.0, dt.getExample(5).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(5).getValue("c2"), 0.0000001);
        Assert.assertEquals(11.0, dt.getExample(9).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(9).getValue("c2"), 0.0000001);

        dt.sortBy("c1", EColumnSortDirections.DECREASING);

        Assert.assertEquals(7.0, dt.getExample(2).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(2).getValue("c2"), 0.0000001);
        Assert.assertEquals(4.0, dt.getExample(5).getValue("c1"), 0.0000001);
        Assert.assertEquals(0.0, dt.getExample(5).getValue("c2"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(9).getValue("c1"), 0.0000001);
        Assert.assertEquals(1.0, dt.getExample(9).getValue("c2"), 0.0000001);
    }
}
