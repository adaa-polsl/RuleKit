package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.table.IExampleTable;
import adaa.analytics.rules.rm.operator.Annotations;
import adaa.analytics.rules.rm.tools.Tools;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ConditionedExampleSet extends AbstractExampleSet {
    private static final long serialVersionUID = 877488093216198777L;

    private IExampleSet parent;
    private int[] mapping;

    public ConditionedExampleSet(IExampleSet parent, ICondition condition) {
        this(parent, condition, false);
    }


    public ConditionedExampleSet(IExampleSet parent, ICondition condition, boolean inverted) {
        this.parent = (IExampleSet)parent.clone();
        this.mapping = this.calculateMapping(condition, inverted);
    }

    public ConditionedExampleSet(ConditionedExampleSet exampleSet) {
        this.parent = (IExampleSet)exampleSet.parent.clone();
        this.mapping = new int[exampleSet.mapping.length];
        System.arraycopy(exampleSet.mapping, 0, this.mapping, 0, exampleSet.mapping.length);
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        } else if (!(o instanceof ConditionedExampleSet)) {
            return false;
        } else {
            ConditionedExampleSet other = (ConditionedExampleSet)o;
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

    private int[] calculateMapping(ICondition condition, boolean inverted) {
//        if (progress != null) {
//            progress.setTotal(this.parent.size() + 1);
//        }

        List<Integer> indices = new LinkedList();
        int exampleCounter = 0;
        Iterator var6 = this.parent.iterator();

        while(var6.hasNext()) {
            Example example = (Example)var6.next();
            if (!inverted) {
                if (condition.conditionOk(example)) {
                    indices.add(exampleCounter);
                }
            } else if (!condition.conditionOk(example)) {
                indices.add(exampleCounter);
            }

            ++exampleCounter;
//            if (progress != null && exampleCounter % 1000 == 0) {
//                progress.setCompleted(exampleCounter);
//            }
        }

        int[] mapping = new int[indices.size()];
        int m = 0;

        int index;
        for(Iterator var8 = indices.iterator(); var8.hasNext(); mapping[m++] = index) {
            index = (Integer)var8.next();
        }

        return mapping;
    }

    public Iterator<Example> iterator() {
        return new MappedExampleReader(this.parent.iterator(), this.mapping);
    }

    public Example getExample(int index) {
        if (index >= 0 && index < this.mapping.length) {
            return this.parent.getExample(this.mapping[index]);
        } else {
            throw new RuntimeException("Given index '" + index + "' does not fit the filtered ExampleSet!");
        }
    }

    public int size() {
        return this.mapping.length;
    }

    public IAttributes getAttributes() {
        return this.parent.getAttributes();
    }

    public IExampleTable getExampleTable() {
        return this.parent.getExampleTable();
    }



    public Annotations getAnnotations() {
        return this.parent.getAnnotations();
    }

    public void cleanup() {
        this.parent.cleanup();
    }

    public boolean isThreadSafeView() {
        return this.parent instanceof AbstractExampleSet && ((AbstractExampleSet)this.parent).isThreadSafeView();
    }
}
