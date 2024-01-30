package adaa.analytics.rules.rm.example.table;

import java.util.Arrays;

public abstract class AbstractSparseArrayDataRow extends DataRow implements ISparseDataRow {
    private static final long serialVersionUID = 4946925205115859758L;
    private int[] x;
    private int counter;

    public AbstractSparseArrayDataRow() {
        this(0);
    }

    public AbstractSparseArrayDataRow(int size) {
        this.counter = 0;
        this.x = new int[size];

        for(int i = 0; i < this.x.length; ++i) {
            this.x[i] = Integer.MAX_VALUE;
        }

    }

    protected abstract void removeValue(int var1);

    protected abstract void resizeValues(int var1);

    protected abstract void setValue(int var1, double var2);

    protected abstract double getValue(int var1);

    protected abstract void swapValues(int var1, int var2);

    protected abstract double[] getAllValues();

    private void sort(int off, int len) {
        int m;
        int l;
        if (len < 7) {
            for(m = off; m < len + off; ++m) {
                for(l = m; l > off && this.x[l - 1] > this.x[l]; --l) {
                    this.swap(l, l - 1);
                }
            }

        } else {
            m = off + (len >> 1);
            int a;
            if (len > 7) {
                l = off;
                int n = off + len - 1;
                if (len > 40) {
                    a = len / 8;
                    l = this.med3(off, off + a, off + 2 * a);
                    m = this.med3(m - a, m, m + a);
                    n = this.med3(n - 2 * a, n - a, n);
                }

                m = this.med3(l, m, n);
            }

            long v = (long)this.x[m];
            a = off;
            int b = off;
            int c = off + len - 1;
            int d = c;

            while(true) {
                while(b > c || (long)this.x[b] > v) {
                    for(; c >= b && (long)this.x[c] >= v; --c) {
                        if ((long)this.x[c] == v) {
                            this.swap(c, d--);
                        }
                    }

                    if (b > c) {
                        int n = off + len;
                        int s = Math.min(a - off, b - a);
                        this.vecswap(off, b - s, s);
                        s = Math.min(d - c, n - d - 1);
                        this.vecswap(b, n - s, s);
                        if ((s = b - a) > 1) {
                            this.sort(off, s);
                        }

                        if ((s = d - c) > 1) {
                            this.sort(n - s, s);
                        }

                        return;
                    }

                    this.swap(b++, c--);
                }

                if ((long)this.x[b] == v) {
                    this.swap(a++, b);
                }

                ++b;
            }
        }
    }

    private void vecswap(int a, int b, int n) {
        for(int i = 0; i < n; ++b) {
            this.swap(a, b);
            ++i;
            ++a;
        }

    }

    private int med3(int a, int b, int c) {
        return this.x[a] < this.x[b] ? (this.x[b] < this.x[c] ? b : (this.x[a] < this.x[c] ? c : a)) : (this.x[b] > this.x[c] ? b : (this.x[a] > this.x[c] ? c : a));
    }

    protected void swap(int a, int b) {
        int t = this.x[a];
        this.x[a] = this.x[b];
        this.x[b] = t;
        this.swapValues(a, b);
    }

    protected double get(int val, double defaultValue) {
        int index = Arrays.binarySearch(this.x, val);
        return index < 0 ? defaultValue : this.getValue(index);
    }

    protected synchronized void set(int index, double value, double defaultValue) {
//        int index1 = Arrays.binarySearch(this.x, index);
//        if (Tools.isDefault(defaultValue, value)) {
//            if (index1 >= 0) {
//                System.arraycopy(this.x, index1 + 1, this.x, index1, this.counter - (index1 + 1));
//                this.x[this.counter - 1] = Integer.MAX_VALUE;
//                this.removeValue(index1);
//                --this.counter;
//            }
//        } else if (index1 < 0) {
//            if (this.counter >= this.x.length) {
//                int newlength = this.x.length + (this.x.length >> 1) + 1;
//                int[] y = new int[newlength];
//                System.arraycopy(this.x, 0, y, 0, this.x.length);
//
//                for(int i = this.x.length; i < y.length; ++i) {
//                    y[i] = Integer.MAX_VALUE;
//                }
//
//                this.x = y;
//                this.resizeValues(newlength);
//            }
//
//            this.x[this.counter] = index;
//            this.setValue(this.counter, value);
//            if (this.counter > 0 && index < this.x[this.counter - 1]) {
//                this.sort(0, this.x.length);
//            }
//
//            ++this.counter;
//        } else {
//            this.setValue(index1, value);
//        }

    }

    public int[] getNonDefaultIndices() {
        this.trim();
        return this.x;
    }

    public double[] getNonDefaultValues() {
        this.trim();
        return this.getAllValues();
    }

    public void ensureNumberOfColumns(int numberOfColumns) {
    }

    public synchronized void trim() {
        if (this.counter < this.x.length) {
            int[] y = new int[this.counter];
            System.arraycopy(this.x, 0, y, 0, this.counter);
            this.x = y;
            this.resizeValues(this.counter);
        }

    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < this.x.length; ++i) {
            if (i != 0) {
                result.append(", ");
            }

            result.append(this.x[i] + ":" + this.getValue(i));
        }

        result.append(", counter: " + this.counter);
        return result.toString();
    }
}
