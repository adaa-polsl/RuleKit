package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.table.IExampleTable;
import adaa.analytics.rules.rm.operator.Annotations;
import adaa.analytics.rules.rm.tools.Ontology;

import java.util.*;

public class SortedExampleSet extends AbstractExampleSet {
    private static final long serialVersionUID = 3937175786207007275L;
    public static final String[] SORTING_DIRECTIONS = new String[]{"increasing", "decreasing"};
    public static final int INCREASING = 0;
    public static final int DECREASING = 1;
    private IExampleSet parent;
    private int[] mapping;

    public SortedExampleSet(IExampleSet parent, IAttribute sortingAttribute, int sortingDirection) {
//        try {
//            this.createSortedExampleSet(parent, sortingAttribute, sortingDirection, (OperatorProgress)null);
//        } catch (ProcessStoppedException var5) {
//        }
        this.createSortedExampleSet(parent, sortingAttribute, sortingDirection);
    }

//    public SortedExampleSet(IExampleSet parent, IAttribute sortingAttribute, int sortingDirection, OperatorProgress progress) throws ProcessStoppedException {
//        this.createSortedExampleSet(parent, sortingAttribute, sortingDirection, progress);
//    }

    private void createSortedExampleSet(IExampleSet parent, final IAttribute sortingAttribute, int sortingDirection) {
        this.parent = (IExampleSet)parent.clone();
        List<SortedExampleSet.SortingIndex> sortingIndex = new ArrayList(parent.size());
//        if (progress != null) {
//            progress.setTotal(100);
//        }

        int exampleCounter = 0;
        int progressTriggerCounter = 0;
        Iterator<Example> i = parent.iterator();

        while(i.hasNext()) {
            Example example = (Example)i.next();
            if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(sortingAttribute.getValueType(), 9)) {
                sortingIndex.add(new SortedExampleSet.SortingIndex(example.getDateValue(sortingAttribute), exampleCounter));
            } else if (sortingAttribute.isNumerical()) {
                sortingIndex.add(new SortedExampleSet.SortingIndex(example.getNumericalValue(sortingAttribute), exampleCounter));
            } else {
                sortingIndex.add(new SortedExampleSet.SortingIndex(example.getNominalValue(sortingAttribute), exampleCounter));
            }

            ++exampleCounter;
            ++progressTriggerCounter;
//            if (progress != null && progressTriggerCounter > 2000000) {
//                progressTriggerCounter = 0;
//                progress.setCompleted((int)((long)exampleCounter * 40L / (long)parent.size()));
//            }
        }

//        if (progress != null) {
//            progress.setCompleted(40);
//        }

        Comparator<SortedExampleSet.SortingIndex> sortComparator = new Comparator<SortedExampleSet.SortingIndex>() {
            public int compare(SortedExampleSet.SortingIndex o1, SortedExampleSet.SortingIndex o2) {
                if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(sortingAttribute.getValueType(), 9)) {
                    Date firstDate = o1.getKeyAsDate();
                    Date secondDate = o2.getKeyAsDate();
                    return firstDate != secondDate && firstDate != null ? firstDate.compareTo(secondDate) : 0;
                } else if (sortingAttribute.isNumerical()) {
                    Double firstDouble = o1.getKeyAsDouble();
                    Double secondDouble = o2.getKeyAsDouble();
                    return firstDouble != null && !firstDouble.equals(secondDouble) ? firstDouble.compareTo(secondDouble) : 0;
                } else if (sortingAttribute.isNominal()) {
                    String firstString = o1.getKeyAsString();
                    String secondString = o2.getKeyAsString();
                    return firstString != null && !firstString.equals(secondString) ? firstString.compareTo(secondString) : 0;
                } else {
                    return 0;
                }
            }
        };
        if (sortingDirection == 0) {
            Collections.sort(sortingIndex, sortComparator);
        } else {
            Collections.sort(sortingIndex, Collections.reverseOrder(sortComparator));
        }

//        if (progress != null) {
//            progress.setCompleted(60);
//        }

        int[] mapping = new int[parent.size()];
        exampleCounter = 0;
        progressTriggerCounter = 0;
        Iterator<SortedExampleSet.SortingIndex> k = sortingIndex.iterator();

        while(k.hasNext()) {
            Integer index = ((SortedExampleSet.SortingIndex)k.next()).getIndex();
            mapping[exampleCounter++] = index;
            ++progressTriggerCounter;
//            if (progress != null && progressTriggerCounter > 2000000) {
//                progressTriggerCounter = 0;
//                progress.setCompleted((int)(60L + (long)exampleCounter * 40L / (long)sortingIndex.size()));
//            }
        }

        this.mapping = mapping;
    }

    public SortedExampleSet(IExampleSet parent, int[] mapping) {
        this.parent = (IExampleSet)parent.clone();
        this.mapping = mapping;
    }

    public SortedExampleSet(SortedExampleSet exampleSet) {
        this.parent = (IExampleSet)exampleSet.parent.clone();
        this.mapping = new int[exampleSet.mapping.length];
        System.arraycopy(exampleSet.mapping, 0, this.mapping, 0, exampleSet.mapping.length);
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        } else if (!(o instanceof SortedExampleSet)) {
            return false;
        } else {
            SortedExampleSet other = (SortedExampleSet)o;
            if (this.mapping.length != other.mapping.length) {
                return false;
            } else {
                for(int i = 0; i < this.mapping.length; ++i) {
                    if (this.mapping[i] != other.mapping[i]) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(this.mapping);
    }

    public Iterator<Example> iterator() {
        return new SortedExampleReader(this);
    }

    public Example getExample(int index) {
        if (index >= 0 && index < this.mapping.length) {
            return this.parent.getExample(this.mapping[index]);
        } else {
            throw new RuntimeException("Given index '" + index + "' does not fit the mapped ExampleSet!");
        }
    }

    public int size() {
        return this.mapping.length;
    }

    public IAttributes getAttributes() {
        return this.parent.getAttributes();
    }

    public Annotations getAnnotations() {
        return this.parent.getAnnotations();
    }

    public IExampleTable getExampleTable() {
        return this.parent.getExampleTable();
    }

    public void cleanup() {
        this.parent.cleanup();
    }

    public boolean isThreadSafeView() {
        return this.parent instanceof AbstractExampleSet && ((AbstractExampleSet)this.parent).isThreadSafeView();
    }

    private static class SortingIndex {
        private final Object key;
        private final int index;

        public SortingIndex(Object key, int index) {
            this.key = key;
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public Date getKeyAsDate() {
            return (Date)this.key;
        }

        public String getKeyAsString() {
            return (String)this.key;
        }

        public Double getKeyAsDouble() {
            return (Double)this.key;
        }

        public String toString() {
            return this.key + " --> " + this.index;
        }
    }
}
