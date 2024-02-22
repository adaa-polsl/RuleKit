package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.table.IExampleTable;
import adaa.analytics.rules.rm.operator.Annotations;
import adaa.analytics.rules.rm.tools.Tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SplittedExampleSet extends AbstractExampleSet {
    private static final long serialVersionUID = 4573262969007377183L;
    public static final String[] SAMPLING_NAMES = new String[]{"linear sampling", "shuffled sampling", "stratified sampling", "automatic"};
    public static final int LINEAR_SAMPLING = 0;
    public static final int SHUFFLED_SAMPLING = 1;
    public static final int STRATIFIED_SAMPLING = 2;
    public static final int AUTOMATIC = 3;
    private Partition partition;
    private IExampleSet parent;

    public SplittedExampleSet(IExampleSet exampleSet, Partition partition) {
        this.parent = (IExampleSet)exampleSet.clone();
        this.partition = partition;
    }

    public SplittedExampleSet(IExampleSet exampleSet, Partition partition, boolean tryCompose) {
        if (tryCompose && exampleSet instanceof SplittedExampleSet) {
            SplittedExampleSet splitted = (SplittedExampleSet)exampleSet;
            this.parent = (IExampleSet)splitted.parent.clone();
            Partition oldPartition = splitted.partition;
            this.partition = Partition.compose(oldPartition, partition);
        } else {
            this.parent = (IExampleSet)exampleSet.clone();
            this.partition = partition;
        }

    }

    public SplittedExampleSet(IExampleSet exampleSet, double splitRatio, int samplingType, boolean useLocalRandomSeed, int seed, boolean autoSwitchToShuffled) {
        this(exampleSet, new double[]{splitRatio, 1.0 - splitRatio}, samplingType, useLocalRandomSeed, seed, autoSwitchToShuffled);
    }

    public SplittedExampleSet(IExampleSet exampleSet, double splitRatio, int samplingType, boolean useLocalRandomSeed, int seed) {
        this(exampleSet, new double[]{splitRatio, 1.0 - splitRatio}, samplingType, useLocalRandomSeed, seed);
    }

    public SplittedExampleSet(IExampleSet exampleSet, double splitRatio, int samplingType, boolean useLocalRandomSeed, boolean tryCompose, int seed) {
        this(exampleSet, new double[]{splitRatio, 1.0 - splitRatio}, samplingType, useLocalRandomSeed, tryCompose, seed);
    }

    public SplittedExampleSet(IExampleSet exampleSet, double[] splitRatios, int samplingType, boolean useLocalRandomSeed, int seed) {
        this(exampleSet, new Partition(splitRatios, exampleSet.size(), createPartitionBuilder(exampleSet, samplingType, useLocalRandomSeed, seed, true)));
    }

    public SplittedExampleSet(IExampleSet exampleSet, double[] splitRatios, int samplingType, boolean useLocalRandomSeed, boolean tryCompose, int seed) {
        this(exampleSet, new Partition(splitRatios, exampleSet.size(), createPartitionBuilder(exampleSet, samplingType, useLocalRandomSeed, seed, true)), tryCompose);
    }

    public SplittedExampleSet(IExampleSet exampleSet, double[] splitRatios, int samplingType, boolean useLocalRandomSeed, int seed, boolean autoSwitchToShuffled) {
        this(exampleSet, new Partition(splitRatios, exampleSet.size(), createPartitionBuilder(exampleSet, samplingType, useLocalRandomSeed, seed, autoSwitchToShuffled)));
    }

    public SplittedExampleSet(IExampleSet exampleSet, int numberOfSubsets, int samplingType, boolean useLocalRandomSeed, int seed) {
        this(exampleSet, new Partition(numberOfSubsets, exampleSet.size(), createPartitionBuilder(exampleSet, samplingType, useLocalRandomSeed, seed, true)));
    }

    public SplittedExampleSet(IExampleSet exampleSet, int numberOfSubsets, int samplingType, boolean useLocalRandomSeed, int seed, boolean autoSwitchToShuffled) {
        this(exampleSet, new Partition(numberOfSubsets, exampleSet.size(), createPartitionBuilder(exampleSet, samplingType, useLocalRandomSeed, seed, autoSwitchToShuffled)));
    }

    public SplittedExampleSet(SplittedExampleSet exampleSet) {
        this.parent = (IExampleSet)exampleSet.parent.clone();
        this.partition = (Partition)exampleSet.partition.clone();
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        } else {
            return !(o instanceof SplittedExampleSet) ? false : this.partition.equals(((SplittedExampleSet)o).partition);
        }
    }

    public int hashCode() {
        return super.hashCode() ^ this.partition.hashCode();
    }

    private static IPartitionBuilder createPartitionBuilder(IExampleSet exampleSet, int samplingType, boolean useLocalRandomSeed, int seed, boolean autoSwitchToShuffled) {
        IPartitionBuilder builder = null;
        switch (samplingType) {
            case 0:
                builder = new SimplePartitionBuilder();
                break;
            case 1:
                builder = new ShuffledPartitionBuilder(useLocalRandomSeed, seed);
                break;
            case 2:
            case 3:
            default:
                IAttribute label = exampleSet.getAttributes().getLabel();
                if (label != null && label.isNominal()) {
                    builder = new StratifiedPartitionBuilder(exampleSet, useLocalRandomSeed, seed);
                } else {
                    if ((autoSwitchToShuffled || samplingType == 3) && (label == null || !label.isNominal())) {
                        // TODO log
//                        exampleSet.getLog().logWarning("Example set has no nominal label: using shuffled partition instead of stratified partition");
                        return new ShuffledPartitionBuilder(useLocalRandomSeed, seed);
                    }

                    Tools.hasNominalLabels(exampleSet, "stratified sampling");
                    builder = new ShuffledPartitionBuilder(useLocalRandomSeed, seed);
                }
        }

        return (IPartitionBuilder)builder;
    }

    public void selectAdditionalSubset(int index) {
        this.partition.selectSubset(index);
    }

    public void selectSingleSubset(int index) {
        this.partition.clearSelection();
        this.partition.selectSubset(index);
    }

    public void selectAllSubsetsBut(int index) {
        this.partition.clearSelection();

        for(int i = 0; i < this.partition.getNumberOfSubsets(); ++i) {
            if (i != index) {
                this.partition.selectSubset(i);
            }
        }

    }

    public void selectAllSubsets() {
        this.partition.clearSelection();

        for(int i = 0; i < this.partition.getNumberOfSubsets(); ++i) {
            this.partition.selectSubset(i);
        }

    }

    public void invertSelection() {
        this.partition.invertSelection();
    }

    public void clearSelection() {
        this.partition.clearSelection();
    }

    public int getNumberOfSubsets() {
        return this.partition.getNumberOfSubsets();
    }

    public Iterator<Example> iterator() {
        return new IndexBasedExampleSetReader(this);
    }

    public int size() {
        return this.partition.getSelectionSize();
    }

    public Example getExample(int index) {
        int actualIndex = this.partition.mapIndex(index);
        return this.parent.getExample(actualIndex);
    }

    public int getActualParentIndex(int index) {
        return this.partition.mapIndex(index);
    }

    public IExampleTable getExampleTable() {
        return this.parent.getExampleTable();
    }

    public IAttributes getAttributes() {
        return this.parent.getAttributes();
    }

    public Annotations getAnnotations() {
        return this.parent.getAnnotations();
    }

    public static SplittedExampleSet splitByAttribute(IExampleSet exampleSet, IAttribute attribute) {
        int[] elements = new int[exampleSet.size()];
        int i = 0;
        Map<Integer, Integer> indexMap = new HashMap();
        AtomicInteger currentIndex = new AtomicInteger(0);

        int intValue;
        for(Iterator var6 = exampleSet.iterator(); var6.hasNext(); elements[i++] = intValue) {
            Example example = (Example)var6.next();
            int value = (int)example.getValue(attribute);
            Integer indexObject = (Integer)indexMap.get(value);
            if (indexObject == null) {
                indexMap.put(value, currentIndex.getAndIncrement());
            }

            intValue = (Integer)indexMap.get(value);
        }

        int maxNumber = indexMap.size();
        indexMap.clear();
        Partition partition = new Partition(elements, maxNumber);
        return new SplittedExampleSet(exampleSet, partition);
    }

    public static SplittedExampleSet splitByAttribute(IExampleSet exampleSet, IAttribute attribute, double value) {
        int[] elements = new int[exampleSet.size()];
        Iterator<Example> reader = exampleSet.iterator();
        int i = 0;

        while(reader.hasNext()) {
            Example example = (Example)reader.next();
            double currentValue = example.getValue(attribute);
            if (Tools.isLessEqual(currentValue, value)) {
                elements[i++] = 0;
            } else {
                elements[i++] = 1;
            }
        }

        Partition partition = new Partition(elements, 2);
        return new SplittedExampleSet(exampleSet, partition);
    }

    public void cleanup() {
        this.parent.cleanup();
    }
}
