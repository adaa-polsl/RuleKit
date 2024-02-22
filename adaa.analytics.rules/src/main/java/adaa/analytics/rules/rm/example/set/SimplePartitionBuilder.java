package adaa.analytics.rules.rm.example.set;

public class SimplePartitionBuilder implements IPartitionBuilder {
    public SimplePartitionBuilder() {
    }

    public int[] createPartition(double[] ratio, int size) {
        int[] startNewP = new int[ratio.length + 1];
        startNewP[0] = 0;
        double ratioSum = 0.0;

        int p;
        for(p = 1; p < startNewP.length; ++p) {
            ratioSum += ratio[p - 1];
            startNewP[p] = (int)Math.round((double)size * ratioSum);
        }

        p = 0;
        int[] part = new int[size];

        for(int i = 0; i < part.length; ++i) {
            if (i >= startNewP[p + 1]) {
                ++p;
            }

            part[i] = p;
        }

        return part;
    }
}
