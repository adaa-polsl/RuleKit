/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package utils;

import java.util.ArrayList;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;

public abstract class DataRowFactory2 {

    public abstract DataRow createRow(Iterable<String> inputRow);

    public static DataRowFactory2 withFullStopDecimalSeparator(Iterable<Attribute> attributes) {
        if (attributes == null || attributes.spliterator().getExactSizeIfKnown() == 0)
        	return null;
        DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
        return new DataRowFactoryImpl(attributes, factory);
    }

    public static DataRowFactory2 withCommaDecimalSeparator(Iterable<Attribute> attributes) {
    	if (attributes == null || attributes.spliterator().getExactSizeIfKnown() == 0)
        	return null;
        DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, ',');
        return new DataRowFactoryImpl(attributes, factory);
    }

    private DataRowFactory2() {}

    public static Attribute[] toAttributeArray(Iterable<Attribute> itr) {
        ArrayList<Attribute> ret = new ArrayList<Attribute>();
        for(Attribute t : itr) {
            ret.add(t);
        }
        return (Attribute[]) ret.toArray(new Attribute[0]);
    }
    
    public static String[] toArray(Iterable<String> itr) {
        ArrayList<String> ret = new ArrayList<>();
        for(String t : itr) {
            ret.add(t);
        }
        return (String[]) ret.toArray(new String[0]);
    }
    
    private static final class DataRowFactoryImpl extends DataRowFactory2 {

        private final DataRowFactory factory;
        private final Attribute[] attributes;

        DataRowFactoryImpl(Iterable<Attribute> attributes, DataRowFactory factory){
            assert factory != null;
            assert attributes != null;
            this.attributes = toAttributeArray(attributes);
            this.factory = factory;
    }
        public DataRow createRow(Iterable<String> inputRow) {
        if (inputRow == null || inputRow.spliterator().getExactSizeIfKnown() == 0)
           	return null;
        String[] strings = toArray(inputRow);
        return factory.create(strings, attributes);
        }
    }
}
