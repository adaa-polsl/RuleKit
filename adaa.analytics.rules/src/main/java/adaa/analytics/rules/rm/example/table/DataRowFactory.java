package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.tools.Tools;

import java.util.logging.Level;

public class DataRowFactory {

    public static final int TYPE_DOUBLE_ARRAY = 0;
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
