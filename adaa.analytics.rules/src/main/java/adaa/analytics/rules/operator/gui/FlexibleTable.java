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
package adaa.analytics.rules.operator.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.util.ArrayList;

/**
 * A class representing table with custom editor objects. Allows other GUI elements to be contained in a table.
 *
 * @author Adam Gudys
 */
public class FlexibleTable extends JTable {

	private static final long serialVersionUID = -4153947883789813784L;

	protected ArrayList<TableCellEditor[]> editors = new ArrayList<TableCellEditor[]>();
	
	public void setCell(Object aObject, int row, int column) {
		DefaultTableModel dtm = (DefaultTableModel)this.getModel();
		if (aObject instanceof JComboBox) {
			editors.get(row)[column] = new DefaultCellEditor((JComboBox)aObject);
			super.setValueAt("", row, column);
		} else if (aObject instanceof String) {
			editors.get(row)[column] = super.getCellEditor(row, column);
			super.setValueAt(aObject, row, column);
		} 
	}
	
	public void addRow(Object[] objects) {
		DefaultTableModel dtm = (DefaultTableModel)this.getModel();
		
		// add empty row
		dtm.addRow(new Object[objects.length]);
		editors.add(new TableCellEditor[getColumnCount()]);

		// set cell values
		for (int c = 0; c < objects.length; ++c) {
			setCell(objects[c], dtm.getRowCount() - 1, c);
		}
	}
	
	public void removeRow(int row) {
		DefaultTableModel dtm = (DefaultTableModel)this.getModel();
		if (row >= 0) {
			dtm.removeRow(row);
			editors.remove(row);
		}
	}
	
	
	@Override
	public TableCellEditor getCellEditor(int row, int column)
    {
		return editors.get(row)[column];	
    }
	
	public FlexibleTable(DefaultTableModel model) {
		super(model);
		
/*		model.addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				
				if (e.getType() == TableModelEvent.UPDATE) {
					
				} 
			}
		});*/
	}
	
	
}
