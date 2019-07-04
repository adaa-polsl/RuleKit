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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InMemoryDataSet {

	protected ExampleSet dataset;
	protected List<String> inputData;
	
	public InMemoryDataSet(List<Attribute> attributes, List<String> data) {
		inputData = data;
        Map<Attribute, String> roles = createRoles(attributes);

        ExampleTable table = createExampleTable(attributes, inputRows());
        dataset = table.createExampleSet(roles);
	}
	
	public ExampleSet getExampleSet() {
		return dataset;
	}
	
	ExampleTable createExampleTable(List<Attribute> attributes, Iterable<Row> inputRows) {
        MemoryExampleTable table = new MemoryExampleTable(attributes);
        DataRowFactory2 factory = DataRowFactory2.withFullStopDecimalSeparator(attributes);
        for(Iterable<String> row : inputRows){
            DataRow dataRow = factory.createRow(row);
            table.addDataRow(dataRow);
        }
        return table;
    }
	
/*	List<Attribute> createAttributes() {
        return Collections.unmodifiableList(Arrays.asList(
        		AttributeFactory.createAttribute("teamID", Ontology.NOMINAL),
                AttributeFactory.createAttribute("size", Ontology.INTEGER),
                AttributeFactory.createAttribute("leader", Ontology.NOMINAL),
                AttributeFactory.createAttribute("number of qualified employees", Ontology.INTEGER),
                AttributeFactory.createAttribute("leader changed", Ontology.BINOMINAL),
                AttributeFactory.createAttribute("average years of experience", Ontology.INTEGER),
                AttributeFactory.createAttribute("structure", Ontology.BINOMINAL))
        		);
    }*/

	 /* List<String> inputData() {
	        return Collections.unmodifiableList(Arrays.asList(
	                "team_0, 5, Mr. Miller, 4, no, 9, flat",
	                "team_1, 19, Mrs. Green, 3, yes, 8, flat",
	                "team_2, 16, Mrs. Hansc, 2, no, 3, flat",
	                "team_3, 9, Mr. Chang, 6, yes, 3, flat",
	                "team_4, 17, Mr. Chang, 5, yes, 1, hierarchical")
	        		);
	    }*/

	 List<Row> inputRows(){
	        List<Row> data = new ArrayList<Row>(inputData.size());
	        for(String line: inputData){
	            data.add(newRow(line));
	        }
	        return Collections.unmodifiableList(data);
	    }

	 Row newRow(final String line){
	        return new Row() {
	            public Iterator<String> iterator() {
	            	return Arrays.asList(line.split(",")).iterator();
	            }
	        };
	    }

	 Map<Attribute, String> createRoles(List<Attribute> attributes){
		 	Map<Attribute, String> map = new HashMap<Attribute, String>();
		 	map.put(attributes.get(0), Attributes.LABEL_NAME);
	        return Collections.unmodifiableMap(map);
	    }

	
}
