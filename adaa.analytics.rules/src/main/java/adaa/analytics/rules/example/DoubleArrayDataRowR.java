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
package adaa.analytics.rules.example;

import com.rapidminer.example.table.DoubleArrayDataRow;

public class DoubleArrayDataRowR extends DoubleArrayDataRow {

    private ExampleTableAdapter adapter;

    public DoubleArrayDataRowR(double[] data, ExampleTableAdapter adapter) {
        super(data);
        this.adapter=adapter;
    }

    @Override
    protected synchronized void set(int index, double value, double defaultValue) {
        adapter.setDataRowValue(index, value, defaultValue);
    }
}
