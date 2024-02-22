package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.tools.Tools;

import java.util.logging.Level;

public class DataRowFactory {
    public static final String[] TYPE_NAMES = new String[]{"double_array", "float_array", "long_array", "int_array", "short_array", "byte_array", "boolean_array", "double_sparse_array", "float_sparse_array", "long_sparse_array", "int_sparse_array", "short_sparse_array", "byte_sparse_array", "boolean_sparse_array", "sparse_map"};
    public static final int FIRST_TYPE_INDEX = 0;
    public static final int TYPE_DOUBLE_ARRAY = 0;
    public static final int TYPE_FLOAT_ARRAY = 1;
    public static final int TYPE_LONG_ARRAY = 2;
    public static final int TYPE_INT_ARRAY = 3;
    public static final int TYPE_SHORT_ARRAY = 4;
    public static final int TYPE_BYTE_ARRAY = 5;
    public static final int TYPE_BOOLEAN_ARRAY = 6;
    public static final int TYPE_DOUBLE_SPARSE_ARRAY = 7;
    public static final int TYPE_FLOAT_SPARSE_ARRAY = 8;
    public static final int TYPE_LONG_SPARSE_ARRAY = 9;
    public static final int TYPE_INT_SPARSE_ARRAY = 10;
    public static final int TYPE_SHORT_SPARSE_ARRAY = 11;
    public static final int TYPE_BYTE_SPARSE_ARRAY = 12;
    public static final int TYPE_BOOLEAN_SPARSE_ARRAY = 13;
    public static final int TYPE_SPARSE_MAP = 14;
    public static final int TYPE_COLUMN_VIEW = 15;
    public static final int LAST_TYPE_INDEX = 15;
    public static final int TYPE_SPECIAL = -1;
    public static final char POINT_AS_DECIMAL_CHARACTER = '.';
    private int type;
    private char decimalPointCharacter;

    public DataRowFactory(int type) {
        this(type, '.');
    }

    public DataRowFactory(int type, char decimalPointCharacter) {
        this.decimalPointCharacter = '.';
        if (type >= 0 && type <= 15) {
            this.type = type;
            this.decimalPointCharacter = decimalPointCharacter;
        } else {
            throw new IllegalArgumentException("Illegal data row type: " + type);
        }
    }

    public DataRow create(int size) {
        DataRow row = null;
        switch (this.type) {
            case 0:
            case 15:
                row = new DoubleArrayDataRow(new double[size]);
                break;
            case 1:
                row = new FloatArrayDataRow(new float[size]);
                break;
            case 2:
                row = new LongArrayDataRow(new long[size]);
                break;
            case 3:
                row = new IntArrayDataRow(new int[size]);
                break;
            case 4:
                row = new ShortArrayDataRow(new short[size]);
                break;
            case 5:
                row = new ByteArrayDataRow(new byte[size]);
                break;
            case 6:
                row = new BooleanArrayDataRow(new boolean[size]);
                break;
            case 7:
                row = new DoubleSparseArrayDataRow(16);
                break;
            case 8:
                row = new FloatSparseArrayDataRow(size >> 2);
                break;
            case 9:
                row = new LongSparseArrayDataRow(size >> 2);
                break;
            case 10:
                row = new IntSparseArrayDataRow(size >> 2);
                break;
            case 11:
                row = new ShortSparseArrayDataRow(size >> 2);
                break;
            case 12:
                row = new ByteSparseArrayDataRow(size >> 2);
                break;
            case 13:
                row = new BooleanSparseArrayDataRow(size >> 2);
                break;
            case 14:
                row = new SparseMapDataRow();
        }

        return (DataRow)row;
    }

    public DataRow create(String[] strings, IAttribute[] attributes) {
        DataRow dataRow = this.create(strings.length);

        for(int i = 0; i < strings.length; ++i) {
            if (strings[i] != null) {
                strings[i] = strings[i].trim();
            }

            if (strings[i] != null && strings[i].length() > 0 && !strings[i].equals("?")) {
                if (attributes[i].isNominal()) {
                    String unescaped = Tools.unescape(strings[i]);
                    dataRow.set(attributes[i], (double)attributes[i].getMapping().mapString(unescaped));
                } else {
                    dataRow.set(attributes[i], string2Double(strings[i], this.decimalPointCharacter));
                }
            } else {
                dataRow.set(attributes[i], Double.NaN);
            }
        }

        dataRow.trim();
        return dataRow;
    }

    public DataRow create(Object[] data, IAttribute[] attributes) {
        DataRow dataRow = this.create(data.length);

        for(int i = 0; i < data.length; ++i) {
            if (data[i] != null) {
                if (attributes[i].isNominal()) {
                    dataRow.set(attributes[i], (double)attributes[i].getMapping().mapString(((String)data[i]).trim()));
                } else {
                    dataRow.set(attributes[i], ((Number)data[i]).doubleValue());
                }
            } else {
                dataRow.set(attributes[i], Double.NaN);
            }
        }

        dataRow.trim();
        return dataRow;
    }

    public DataRow create(Double[] data, IAttribute[] attributes) {
        DataRow dataRow = this.create(data.length);

        for(int i = 0; i < data.length; ++i) {
            if (data[i] != null) {
                if (attributes[i].isNominal()) {
                    dataRow.set(attributes[i], (double)attributes[i].getMapping().mapString(String.valueOf(data[i]).trim()));
                } else {
                    dataRow.set(attributes[i], data[i].doubleValue());
                }
            } else {
                dataRow.set(attributes[i], Double.NaN);
            }
        }

        dataRow.trim();
        return dataRow;
    }

    public int getType() {
        return this.type;
    }

    private static final double string2Double(String str, char decimalPointCharacter) {
        if (str == null) {
            return Double.NaN;
        } else {
            try {
                str = str.replace(decimalPointCharacter, '.');
                return Double.parseDouble(str);
            } catch (NumberFormatException var3) {
//                LogService.getRoot().log(Level.SEVERE, "adaa.analytics.rules.rm.example.table.DataRowFactory.datarowfactory_is_not_a_valid_number", str);
                return Double.NaN;
            }
        }
    }
}
