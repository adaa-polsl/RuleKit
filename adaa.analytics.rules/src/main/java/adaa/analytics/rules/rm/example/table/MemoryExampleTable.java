package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.IAttribute;

import java.util.*;

public class MemoryExampleTable extends AbstractExampleTable implements IGrowingExampleTable {
    private static final long serialVersionUID = -3000023475208774934L;
    private List<DataRow> dataList;
    private int columns;
    private static final int INCREMENT = 10;

    /** @deprecated */
    @Deprecated
    public MemoryExampleTable(IAttribute... attributes) {
        this(Arrays.asList(attributes));
    }

    public MemoryExampleTable(List<IAttribute> attributes) {
        super(attributes);
        this.dataList = new ArrayList();
        this.columns = attributes.size();
    }

    /** @deprecated */
    @Deprecated
    public MemoryExampleTable(List<IAttribute> attributes, int expectedSize) {
        this(attributes);
        this.dataList = new ArrayList(expectedSize);
    }

    /** @deprecated */
    @Deprecated
    public MemoryExampleTable(List<IAttribute> attributes, DataRowFactory factory, int size) {
        this(attributes);
        this.dataList = new ArrayList(size);

        for(int i = 0; i < size; ++i) {
            DataRow dataRow = factory.create(attributes.size());
            Iterator var6 = attributes.iterator();

            while(var6.hasNext()) {
                IAttribute attribute = (IAttribute)var6.next();
                dataRow.set(attribute, Double.NaN);
            }

            this.dataList.add(dataRow);
        }

    }

    /** @deprecated */
    @Deprecated
    public MemoryExampleTable(List<IAttribute> attributes, IDataRowReader i) {
        this(attributes, i, false);
    }

    /** @deprecated */
    @Deprecated
    public MemoryExampleTable(List<IAttribute> attributes, IDataRowReader i, boolean permute) {
        this(attributes);
        this.readExamples(i, permute);
    }

    public void readExamples(IDataRowReader i) {
        this.readExamples(i, false);
    }

    public void readExamples(IDataRowReader i, boolean permute) {
        this.readExamples(i, permute, (Random)null);
    }

    public void readExamples(IDataRowReader i, boolean permute, Random random) {
        this.dataList.clear();

        while(i.hasNext()) {
            if (permute) {
                if (random == null) {
                    random = new Random();
                }

                int index = random.nextInt(this.dataList.size() + 1);
                this.dataList.add(index, i.next());
            } else {
                this.dataList.add(i.next());
            }
        }

    }

    public IDataRowReader getDataRowReader() {
        return new ListDataRowReader(this.dataList.iterator());
    }

    public DataRow getDataRow(int index) {
        return (DataRow)this.dataList.get(index);
    }

    public int size() {
        return this.dataList.size();
    }

    public void addDataRow(DataRow dataRow) {
        dataRow.trim();
        this.dataList.add(dataRow);
        dataRow.ensureNumberOfColumns(this.columns);
    }

    public boolean removeDataRow(DataRow dataRow) {
        return this.dataList.remove(dataRow);
    }

    public DataRow removeDataRow(int index) {
        return (DataRow)this.dataList.remove(index);
    }

    public void clear() {
        this.dataList.clear();
    }

    public void addAttributes(Collection<IAttribute> newAttributes) {
        Iterator i;
        if (this.dataList != null && this.getNumberOfAttributes() + newAttributes.size() > this.columns) {
            this.columns = this.getNumberOfAttributes() + newAttributes.size();
            i = this.dataList.iterator();

            while(i.hasNext()) {
                ((DataRow)i.next()).ensureNumberOfColumns(this.columns);
            }
        }

        i = newAttributes.iterator();

        while(i.hasNext()) {
            IAttribute att = (IAttribute)i.next();
            this.addAttribute(att);
        }

    }

    public synchronized int addAttribute(IAttribute attribute) {
        int index = super.addAttribute(attribute);
        if (this.dataList == null) {
            return index;
        } else {
            int n = this.getNumberOfAttributes();
            if (n <= this.columns) {
                return index;
            } else {
                int newSize = n + 10;
//                LogService.getRoot().log(Level.FINE, "MemoryExampleTable.rezising_example_table", new Object[]{this.columns, newSize});
                this.columns = newSize;
                if (this.dataList != null) {
                    Iterator<DataRow> i = this.dataList.iterator();

                    while(i.hasNext()) {
                        ((DataRow)i.next()).ensureNumberOfColumns(this.columns);
                    }
                }

                return index;
            }
        }
    }

    public static MemoryExampleTable createCompleteCopy(IExampleTable oldTable) {
        MemoryExampleTable table = new MemoryExampleTable(Arrays.asList(oldTable.getAttributes()));
        IDataRowReader reader = oldTable.getDataRowReader();

        while(reader.hasNext()) {
            DataRow dataRow = (DataRow)reader.next();
            double[] newDataRowData = new double[oldTable.getNumberOfAttributes()];

            for(int a = 0; a < oldTable.getNumberOfAttributes(); ++a) {
                IAttribute attribute = oldTable.getAttribute(a);
                if (attribute != null) {
                    newDataRowData[a] = dataRow.get(attribute);
                } else {
                    newDataRowData[a] = Double.NaN;
                }
            }

            table.addDataRow(new DoubleArrayDataRow(newDataRowData));
        }

        return table;
    }
}
