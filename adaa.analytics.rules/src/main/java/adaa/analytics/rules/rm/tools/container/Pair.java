package adaa.analytics.rules.rm.tools.container;

import java.io.Serializable;
import java.util.Objects;

public class Pair<T, K> implements Serializable {
    private static final long serialVersionUID = 1L;
    private T first;
    private K second;

    public Pair(T t, K k) {
        this.setFirst(t);
        this.setSecond(k);
    }

    public T getFirst() {
        return this.first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public K getSecond() {
        return this.second;
    }

    public void setSecond(K second) {
        this.second = second;
    }

    public String toString() {
        return this.first + " : " + this.second;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.first, this.second});
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            Pair<?, ?> other = (Pair)obj;
            return Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second);
        } else {
            return false;
        }
    }
}