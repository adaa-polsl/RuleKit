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

import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;

public class DataFrameRowReader implements DataRowReader {


    public DataFrameRowReader(ExampleTableAdapter adaptor) {
        this.adaptor = adaptor;
        this.index = 0;
        this.size = adaptor.size();
    }

    private int index;
    private int size;
    private ExampleTableAdapter adaptor;

    @Override
    public boolean hasNext() {
        if (index < size) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DataRow next() {
        index++;
        return adaptor.getDataRow(index -1);
    }
}
