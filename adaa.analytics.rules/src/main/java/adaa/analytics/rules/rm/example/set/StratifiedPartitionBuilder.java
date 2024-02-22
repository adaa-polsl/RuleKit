package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.tools.RandomGenerator;

import java.util.*;
import java.util.logging.Level;

public class StratifiedPartitionBuilder implements IPartitionBuilder {
    private IExampleSet exampleSet;
    private Random random;

    public StratifiedPartitionBuilder(IExampleSet exampleSet, boolean useLocalRandomSeed, int seed) {
        this.exampleSet = exampleSet;
        this.random = RandomGenerator.getGlobalRandomGenerator();
    }

    public int[] createPartition(double[] ratio, int size) {
        IAttribute label = this.exampleSet.getAttributes().getLabel();
        if (size != this.exampleSet.size()) {
            throw new RuntimeException("Cannot create stratified Partition: given size and size of the example set must be equal!");
        } else if (label == null) {
            throw new RuntimeException("Cannot create stratified Partition: example set must have a label!");
        } else if (!label.isNominal()) {
            throw new RuntimeException("Cannot create stratified Partition: label of example set must be nominal!");
        } else {
            double firstValue = ratio[0];

            for(int i = 1; i < ratio.length; ++i) {
                if (ratio[i] != firstValue) {
                    // TODO Log
//                    LogService.getRoot().log(Level.FINE, "not_all_ratio_values_are_equal");
                    return this.createNonEqualPartition(ratio, size, label);
                }
            }

            // TODO Log
//            LogService.getRoot().log(Level.FINE, "all_ratio_values_are_equal");
            return this.createEqualPartition(ratio, size, label);
        }
    }

    private int[] createEqualPartition(double[] ratio, int size, IAttribute label) {
        List<ExampleIndex> examples = new ArrayList(size);
        Iterator<Example> reader = this.exampleSet.iterator();
        int index = 0;

        while(reader.hasNext()) {
            Example example = (Example)reader.next();
            examples.add(new ExampleIndex(index++, example.getNominalValue(label)));
        }

        Collections.shuffle(examples, this.random);
        Collections.sort(examples);
        List<ExampleIndex> newExamples = new ArrayList(size);
        int start = 0;

        for(int numberOfPartitions = ratio.length; newExamples.size() < size; ++start) {
            for(int i = start; i < examples.size(); i += numberOfPartitions) {
                newExamples.add(examples.get(i));
            }
        }

        int[] startNewP = new int[ratio.length + 1];
        startNewP[0] = 0;
        double ratioSum = 0.0;

        for(int i = 1; i < startNewP.length; ++i) {
            ratioSum += ratio[i - 1];
            startNewP[i] = (int)Math.round((double)newExamples.size() * ratioSum);
        }

        int[] part = new int[newExamples.size()];
        int p = 0;
        int counter = 0;

        for(Iterator<ExampleIndex> n = newExamples.iterator(); n.hasNext(); ++counter) {
            if (counter >= startNewP[p + 1]) {
                ++p;
            }

            ExampleIndex exampleIndex = (ExampleIndex)n.next();
            part[exampleIndex.exampleIndex] = p;
        }

        return part;
    }

    private int[] createNonEqualPartition(double[] ratio, int size, IAttribute label) {
        Map<String, List<Integer>> classLists = new LinkedHashMap<>();
        Iterator<Example> reader = this.exampleSet.iterator();
        int index = 0;

        List classList;
        while(reader.hasNext()) {
            Example example = (Example)reader.next();
            String value = example.getNominalValue(label);
            classList = classLists.get(value);
            if (classList == null) {
                List<Integer> classList2 = new LinkedList<>();
                classList2.add(index++);
                classLists.put(value, classList2);
            } else {
                classList.add(index++);
            }
        }

        int[] part = new int[this.exampleSet.size()];
        Iterator<List<Integer>> c = classLists.values().iterator();

        while(c.hasNext()) {
            classList = c.next();
            Collections.shuffle(classList, this.random);
            int[] startNewP = new int[ratio.length + 1];
            startNewP[0] = 0;
            double ratioSum = 0.0;

            int p;
            for(p = 1; p < startNewP.length; ++p) {
                ratioSum += ratio[p - 1];
                startNewP[p] = (int)Math.round((double)classList.size() * ratioSum);
            }

            p = 0;
            int counter = 0;

            for(Iterator<Integer> n = classList.iterator(); n.hasNext(); ++counter) {
                if (counter >= startNewP[p + 1]) {
                    ++p;
                }

                Integer exampleIndex = (Integer)n.next();
                part[exampleIndex] = p;
            }
        }

        return part;
    }

    private static class ExampleIndex implements Comparable<ExampleIndex> {
        int exampleIndex;
        String className;

        public ExampleIndex(int exampleIndex, String className) {
            this.exampleIndex = exampleIndex;
            this.className = className;
        }

        public int compareTo(ExampleIndex e) {
            return this.className.compareTo(e.className);
        }

        public boolean equals(Object o) {
            if (!(o instanceof ExampleIndex)) {
                return false;
            } else {
                ExampleIndex other = (ExampleIndex)o;
                return this.exampleIndex == other.exampleIndex;
            }
        }

        public int hashCode() {
            return Integer.valueOf(this.exampleIndex).hashCode();
        }

        public String toString() {
            return this.exampleIndex + "(" + this.className + ")";
        }
    }
}
