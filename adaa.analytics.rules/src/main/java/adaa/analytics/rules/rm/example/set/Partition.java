package adaa.analytics.rules.rm.example.set;

import java.io.Serializable;
import java.util.logging.Level;

public class Partition implements Cloneable, Serializable {
    private static final long serialVersionUID = 6126334515107973287L;
    private boolean[] mask;
    private int[] partitionSizes;
    private int[] elements;
    private int[] lastElementIndex;
    private int[] tableIndexMap = null;

    public Partition(double[] ratio, int size, IPartitionBuilder builder) {
        this.init(ratio, size, builder);
    }

    public Partition(int noPartitions, int size, IPartitionBuilder builder) {
        double[] ratio = new double[noPartitions];

        for(int i = 0; i < ratio.length; ++i) {
            ratio[i] = 1.0 / (double)noPartitions;
        }

        this.init(ratio, size, builder);
    }

    public Partition(int[] elements, int numberOfPartitions) {
        this.init(elements, numberOfPartitions);
    }

    private Partition(int numberOfNonHiddenPartitions, int[] newElements) {
        this.partitionSizes = new int[numberOfNonHiddenPartitions];
        this.lastElementIndex = new int[numberOfNonHiddenPartitions];
        this.elements = newElements;

        int i;
        for(i = 0; i < this.elements.length; ++i) {
            if (this.elements[i] >= 0 && this.elements[i] < numberOfNonHiddenPartitions) {
                int var10002 = this.partitionSizes[this.elements[i]]++;
                this.lastElementIndex[this.elements[i]] = i;
            }
        }

        this.mask = new boolean[numberOfNonHiddenPartitions + 1];

        for(i = 0; i < numberOfNonHiddenPartitions; ++i) {
            this.mask[i] = true;
        }

        this.recalculateTableIndices();
    }

    private Partition(Partition p) {
        this.partitionSizes = new int[p.partitionSizes.length];
        System.arraycopy(p.partitionSizes, 0, this.partitionSizes, 0, p.partitionSizes.length);
        this.mask = new boolean[p.mask.length];
        System.arraycopy(p.mask, 0, this.mask, 0, p.mask.length);
        this.elements = new int[p.elements.length];
        System.arraycopy(p.elements, 0, this.elements, 0, p.elements.length);
        this.lastElementIndex = new int[p.lastElementIndex.length];
        System.arraycopy(p.lastElementIndex, 0, this.lastElementIndex, 0, p.lastElementIndex.length);
        this.recalculateTableIndices();
    }

    private void init(double[] ratio, int size, IPartitionBuilder builder) {
        // TODO log
//        LogService.getRoot().log(Level.FINE, "Partition.creating_new_partition_using", builder.getClass().getName());
        this.elements = builder.createPartition(ratio, size);
        this.init(this.elements, ratio.length);
    }

    private void init(int[] newElements, int noOfPartitions) {
        // TODO log
//        LogService.getRoot().log(Level.FINE, "Partition.creating_new_partition_with", new Object[]{newElements.length, noOfPartitions});
        this.partitionSizes = new int[noOfPartitions];
        this.lastElementIndex = new int[noOfPartitions];
        this.elements = newElements;

        int i;
        for(i = 0; i < this.elements.length; ++i) {
            if (this.elements[i] >= 0) {
                int var10002 = this.partitionSizes[this.elements[i]]++;
                this.lastElementIndex[this.elements[i]] = i;
            }
        }

        this.mask = new boolean[noOfPartitions];

        for(i = 0; i < this.mask.length; ++i) {
            this.mask[i] = true;
        }

        this.recalculateTableIndices();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Partition)) {
            return false;
        } else {
            Partition other = (Partition)o;

            int i;
            for(i = 0; i < this.mask.length; ++i) {
                if (this.mask[i] != other.mask[i]) {
                    return false;
                }
            }

            for(i = 0; i < this.elements.length; ++i) {
                if (this.elements[i] != other.elements[i]) {
                    return false;
                }
            }

            return true;
        }
    }

    public int hashCode() {
        int hc = 17;
        int hashMultiplier = 59;
        hc = hc * hashMultiplier + this.mask.length;

        int i;
        for(i = 1; i < this.mask.length; i <<= 1) {
            hc = hc * hashMultiplier + Boolean.valueOf(this.mask[i]).hashCode();
        }

        hc = hc * hashMultiplier + this.elements.length;

        for(i = 1; i < this.elements.length; i <<= 1) {
            hc = hc * hashMultiplier + Integer.valueOf(this.elements[i]).hashCode();
        }

        return hc;
    }

    public boolean hasNext(int index) {
        for(int p = 0; p < this.mask.length; ++p) {
            if (this.mask[p] && index <= this.lastElementIndex[p]) {
                return true;
            }
        }

        return false;
    }

    public void clearSelection() {
        this.mask = new boolean[this.mask.length];
        this.recalculateTableIndices();
    }

    public void invertSelection() {
        for(int i = 0; i < this.mask.length; ++i) {
            this.mask[i] = !this.mask[i];
        }

        this.recalculateTableIndices();
    }

    public void selectSubset(int i) {
        this.mask[i] = true;
        this.recalculateTableIndices();
    }

    public void deselectSubset(int i) {
        this.mask[i] = false;
        this.recalculateTableIndices();
    }

    public int getNumberOfSubsets() {
        return this.partitionSizes.length;
    }

    public int getSelectionSize() {
        int s = 0;

        for(int i = 0; i < this.partitionSizes.length; ++i) {
            if (this.mask[i]) {
                s += this.partitionSizes[i];
            }
        }

        return s;
    }

    public int getTotalSize() {
        return this.elements.length;
    }

    public boolean isSelected(int index) {
        return this.mask[this.elements[index]];
    }

    private void recalculateTableIndices() {
        int length = 0;

        int j;
        for(j = 0; j < this.elements.length; ++j) {
            if (this.mask[this.elements[j]]) {
                ++length;
            }
        }

        this.tableIndexMap = new int[length];
        j = 0;

        for(int i = 0; i < this.elements.length; ++i) {
            if (this.mask[this.elements[i]]) {
                this.tableIndexMap[j] = i;
                ++j;
            }
        }

    }

    public int mapIndex(int index) {
        return this.tableIndexMap[index];
    }

    public String toString() {
        StringBuffer str = new StringBuffer("(");

        for(int i = 0; i < this.partitionSizes.length; ++i) {
            str.append((i != 0 ? "/" : "") + this.partitionSizes[i]);
        }

        str.append(")");
        return str.toString();
    }

    public Object clone() {
        return new Partition(this);
    }

    static Partition compose(Partition parentPartition, Partition childPartition) {
        int numberOfElements = parentPartition.elements.length;
        int[] newElements = new int[numberOfElements];
        int numberOfNonHiddenPartitions = childPartition.getNumberOfSubsets();
        int additionalIndex = numberOfNonHiddenPartitions;
        int indexInChild = 0;

        for(int i = 0; i < numberOfElements; ++i) {
            if (parentPartition.isSelected(i) && indexInChild < childPartition.elements.length) {
                newElements[i] = childPartition.elements[indexInChild++];
            } else {
                newElements[i] = additionalIndex;
            }
        }

        return new Partition(numberOfNonHiddenPartitions, newElements);
    }
}
