package utils;

import adaa.analytics.rules.logic.representation.rule.SurvivalRule;
import adaa.analytics.rules.data.IExampleSet;
import ioutils.ArffFileLoader;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Paths;

public class ArffFileLoaderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void ArffFileLoadTest() throws Exception {

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/deals-train.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "Future Customer", "");

        Assert.assertEquals(3, es.getAttributes().regularSize());
        Assert.assertEquals(1000, es.size());
        Assert.assertNotNull(es.getAttributes().getLabel());
        Assert.assertNull(es.getAttributes().getColumnByRole(SurvivalRule.SURVIVAL_TIME_ROLE));
    }

    @Test
    public void ArffFileLoad2Test() throws Exception {

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-universal-data.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "col2", null);

        Assert.assertEquals(4, es.getAttributes().regularSize());
        Assert.assertEquals(5, es.size());
        Assert.assertNotNull(es.getAttributes().getLabel());
        Assert.assertNull(es.getAttributes().getColumnByRole(SurvivalRule.SURVIVAL_TIME_ROLE));
        Assert.assertTrue(es.getAttributes().get("col 4").isNumerical());
        Assert.assertEquals("1", es.getAttributes().get("col5").getMapping().getValue(0));
    }

    @Test
    public void ArffFileLoad2SurvivalTimeTest() throws Exception {

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-universal-data.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "col5", "col2");

        Assert.assertEquals(3, es.getAttributes().regularSize());
        Assert.assertEquals(5, es.size());
        Assert.assertNotNull(es.getAttributes().getLabel());
        Assert.assertNotNull(es.getAttributes().getColumnByRole(SurvivalRule.SURVIVAL_TIME_ROLE));
        Assert.assertEquals("0", es.getAttributes().getLabel().getMapping().getValue(0));
    }

    @Test
    public void ArffFileLoad2SurvivalTime2Test() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Label attribute has wrong possible values {0, 1}. It has {a, b}");

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-universal-data.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "col2", "col5");
    }

    @Test
    public void ArffFileLoad2SurvivalTime3Test() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Label attribute has to have 2 possible values {0, 1}. It has 5");

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-universal-data.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "col 3", "col5");
    }

    @Test
    public void ArffFileLoad2SurvivalTime4Test() throws Exception {

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-universal-data.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "", "col2");
        es.getAttributes().setLabel(es.getAttributes().get("col5"));

        Assert.assertEquals(3, es.getAttributes().regularSize());
        Assert.assertEquals(5, es.size());
        Assert.assertNotNull(es.getAttributes().getLabel());
        Assert.assertNotNull(es.getAttributes().getColumnByRole(SurvivalRule.SURVIVAL_TIME_ROLE));
        Assert.assertEquals("0", es.getAttributes().getLabel().getMapping().getValue(0));
    }

    @Test
    public void ArffFileLoad2SurvivalTime5Test() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Label attribute has wrong possible values {0, 1}. It has {a, b}");

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-universal-data.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "", "col5");
        es.getAttributes().setLabel(es.getAttributes().get("col2"));
    }

    @Test
    public void ArffFileLoad2SurvivalTime6Test() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Label attribute has to have 2 possible values {0, 1}. It has 5");

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-universal-data.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "", "col5");
        es.getAttributes().setLabel(es.getAttributes().get("col 3"));
    }
}
