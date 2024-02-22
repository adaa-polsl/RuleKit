package adaa.analytics.rules.rm.example.table;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BinominalMapping implements INominalMapping {
    private static final long serialVersionUID = 6566553739308153153L;
    private static final int FIRST_VALUE_INDEX = 0;
    private static final int SECOND_VALUE_INDEX = 1;
    public static final int POSITIVE_INDEX = 1;
    public static final int NEGATIVE_INDEX = 0;
    private String firstValue = null;
    private String secondValue = null;

    public BinominalMapping() {
    }

    private BinominalMapping(BinominalMapping mapping) {
        this.firstValue = mapping.firstValue;
        this.secondValue = mapping.secondValue;
    }

    BinominalMapping(INominalMapping mapping) {
        if (mapping.size() > 0) {
            this.firstValue = mapping.mapIndex(0);
        }

        if (mapping.size() > 1) {
            this.secondValue = mapping.mapIndex(1);
        }

    }

    public Object clone() {
        return new BinominalMapping(this);
    }

    public boolean equals(INominalMapping mapping) {
        if (mapping.size() != this.size()) {
            return false;
        } else {
            Iterator var2 = mapping.getValues().iterator();

            String value;
            do {
                if (!var2.hasNext()) {
                    return true;
                }

                value = (String)var2.next();
            } while(value.equals(this.firstValue) || value.equals(this.secondValue));

            return false;
        }
    }

    public int mapString(String str) {
        if (str == null) {
            return -1;
        } else {
            int index = this.getIndex(str);
            if (index < 0) {
                if (this.firstValue == null) {
                    this.firstValue = str;
                    return 0;
                } else if (this.secondValue == null) {
                    this.secondValue = str;
                    return 1;
                } else {
                    // @TODO uzupełnić wyjątek
                    throw new NotImplementedException("TODO uzupełnić wyjątek");
                    //throw new AttributeTypeException("Cannot map another string for binary attribute: already mapped two strings (" + this.firstValue + ", " + this.secondValue + "). The third string that was tried to add: '" + str + "'");
                }
            } else {
                return index;
            }
        }
    }

    public int getIndex(String str) {
        if (str.equals(this.firstValue)) {
            return 0;
        } else {
            return str.equals(this.secondValue) ? 1 : -1;
        }
    }

    public String mapIndex(int index) {
        switch (index) {
            case 0:
                return this.firstValue;
            case 1:
                return this.secondValue;
            default:
                // @TODO uzupełnić wyjątek
                throw new NotImplementedException("TODO uzupełnić wyjątek");
                //throw new AttributeTypeException("Cannot map index of binary attribute to nominal value: index " + index + " is out of bounds!");
        }
    }

    public void setMapping(String nominalValue, int index) {
        if (index == 0) {
            this.firstValue = nominalValue;
        } else {
            if (index != 1) {
                // @TODO uzupełnić wyjątek
                //throw new AttributeTypeException("Cannot set mapping of binary attribute to index '" + index + "'.");
            }

            this.secondValue = nominalValue;
        }

    }

    public int getNegativeIndex() {
        return 0;
    }

    public int getPositiveIndex() {
        return 1;
    }

    public String getNegativeString() {
        return this.firstValue;
    }

    public String getPositiveString() {
        return this.secondValue;
    }

    public List<String> getValues() {
        if (this.firstValue == null) {
            return Collections.emptyList();
        } else {
            return this.secondValue == null ? Arrays.asList(this.firstValue) : Arrays.asList(this.firstValue, this.secondValue);
        }
    }

    public int size() {
        if (this.firstValue == null) {
            return 0;
        } else {
            return this.secondValue == null ? 1 : 2;
        }
    }

    public void sortMappings() {
        if (this.size() == 2 && this.firstValue.compareTo(this.secondValue) > 0) {
            String dummy = this.secondValue;
            this.secondValue = this.firstValue;
            this.firstValue = dummy;
        }

    }

    public void clear() {
        this.firstValue = null;
        this.secondValue = null;
    }
}
