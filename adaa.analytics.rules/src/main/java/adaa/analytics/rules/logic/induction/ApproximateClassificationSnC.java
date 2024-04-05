package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.container.Pair;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ApproximateClassificationSnC extends ClassificationSnC {
    public ApproximateClassificationSnC(AbstractFinder finder, InductionParameters params) {
        super(finder, params);
    }

    @Override
    public void preprocessClass(ExampleSet dataset, int classId) {
        ApproximateClassificationFinder apx = (ApproximateClassificationFinder)finder;
        apx.resetArrays(dataset, classId);
    }
}
