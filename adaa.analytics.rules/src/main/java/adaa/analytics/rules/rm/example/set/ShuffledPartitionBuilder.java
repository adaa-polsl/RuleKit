package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.tools.RandomGenerator;

import java.util.Random;

public class ShuffledPartitionBuilder extends SimplePartitionBuilder {
    private Random random;

    public ShuffledPartitionBuilder(boolean useLocalRandomSeed, int seed) {
        this.random = RandomGenerator.getGlobalRandomGenerator();
    }

    public int[] createPartition(double[] ratio, int size) {
        int[] part = super.createPartition(ratio, size);

        for(int i = part.length - 1; i >= 1; --i) {
            int swap = this.random.nextInt(i);
            int dummy = part[i];
            part[i] = part[swap];
            part[swap] = dummy;
        }

        return part;
    }
}
