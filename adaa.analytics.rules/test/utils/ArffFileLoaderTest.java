package utils;

import adaa.analytics.rules.logic.representation.SurvivalRule;
import adaa.analytics.rules.data.IExampleSet;
import ioutils.ArffFileLoader;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

public class ArffFileLoaderTest {

    @Test
    public void ArffFileLoadTest() throws Exception {

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/deals-train.arff").toString();

        ArffFileLoader arffFileLoader = new ArffFileLoader();
        IExampleSet es = arffFileLoader.loadDataTable(dataDir, "Future Customer", "");

        Assert.assertEquals(3, es.getAttributes().size());
        Assert.assertEquals(1000, es.size());
        Assert.assertNotNull(es.getAttributes().getLabel());
        Assert.assertNull(es.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE));
    }
}
