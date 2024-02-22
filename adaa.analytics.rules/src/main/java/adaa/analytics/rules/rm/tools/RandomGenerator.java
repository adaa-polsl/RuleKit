package adaa.analytics.rules.rm.tools;

import java.lang.ref.WeakReference;
import java.util.Random;

public class RandomGenerator extends Random {

    private static final ThreadLocal<RandomGenerator> GLOBAL_RANDOM_GENERATOR = new ThreadLocal();

    public static RandomGenerator getGlobalRandomGenerator() {

        return (RandomGenerator)GLOBAL_RANDOM_GENERATOR.get();
    }
}
