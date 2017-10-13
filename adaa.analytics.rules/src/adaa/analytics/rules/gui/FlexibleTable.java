package adaa.analytics.rules.gui;

import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class FlexibleTable extends JTable {
	
	/**
	 * 
	 */
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
