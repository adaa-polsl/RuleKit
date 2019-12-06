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

import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.tools.I18N;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is an element of RapidMiner plugin user interface. It represents a subpanel of {@link ExpertPanel} for defining user's knowledge.
 *
 * @author Adam Gudys
 */
public class RulePanel extends JPanel {
	
	public class ConditionRow {
		public String attribute;
		public String relation;
		public String value;
		
		boolean isComplete() {
			return attribute != null && relation != null && value != null &&
					attribute.length() > 0 && relation.length() > 0;
		}
	}
	
	
	private FlexibleTable table;
	private JComboBox<String> combo_class;
	private JLabel lbl_consequence;
	
	private DefaultTableModel tableModel;
		
	protected List<IRulePanelObserver> observers = new ArrayList<IRulePanelObserver>();
	private JButton btn_removeCondition;
	public void registerObserver(IRulePanelObserver o) {
		observers.add(o);
	}
	
	public void addCondition(ExampleSetMetaData setMeta) {
		AttributeMetaData labelMeta = setMeta.getLabelMetaData();
		Collection<AttributeMetaData> attributes = setMeta.getAllAttributes();
		List<String> attrNames = new ArrayList<String>();
		for (AttributeMetaData a : attributes) {
			if (!a.getName().equals(labelMeta.getName())) {
				attrNames.add(a.getName());
			}
		}
		String[] labels = attrNames.toArray(new String[attrNames.size()]);
		Object[] row = new Object[table.getColumnCount()];
		row[0] = new JComboBox(labels); 
		table.addRow(row);
	}
	
	public void updateCondition(ExampleSetMetaData setMeta, int row) {
		String name = (String)table.getValueAt(row, 0);
		AttributeMetaData attr = setMeta.getAttributeByName(name);
		if (attr != null && attr.isNominal()) {
			ArrayList<String> values = new ArrayList<String>();
			values.add("");
			values.addAll(attr.getValueSet());
			JComboBox cb = new JComboBox(values.toArray(new String[values.size()]));
			cb.setSelectedItem(0);
			table.setCell("=", row, 1);
			table.setCell(cb, row, 2);
			
		} else {
			table.setCell(new JComboBox(new String[]{"<", "<=", ">", ">="}), row, 1);
			table.setCell("", row, 2);
		}
	}
	
	public void removeCondition(int row) {
		table.removeRow(row);
	}
	
	public void removeAllConditions() {
		while (table.getRowCount() > 0) {
			table.removeRow(0);
		}
	}
	
	public int getConditionsCount() {
		return table.getRowCount();
	}
	
	public ConditionRow getConditionRow(int row) {
		ConditionRow out = new ConditionRow();
		out.attribute = (String)table.getValueAt(row, 0);
		out.relation = (String)table.getValueAt(row, 1);
		out.value = (String)table.getValueAt(row, 2);
		return out;
	}
	
	public void setDecisionAttribute(String attribute) { lbl_consequence.setText(
		I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.label_then") +
		" " + attribute + " = " ); 
	} 
	
	public void hideDecisionClasses() { 
		combo_class.removeAllItems();
		combo_class.setEnabled(false);
	}
	
	public String getDecisionClass() { return (String)combo_class.getSelectedItem(); }
	public int getDecisionClassId() { return combo_class.getSelectedIndex(); }
	public void addDecisionClasses(Iterable<String> classes) {
		combo_class.setEnabled(true);
		combo_class.removeAllItems();
		for (String c: classes) {
			combo_class.addItem(c);
		}
	}
	
	/**
	 * Create the panel.
	 */
	public RulePanel() {
		setLayout(new MigLayout("", "[][434.00,grow][50px:50px:50px,fill][50:50:50,grow,fill][][grow]", "[][grow][]"));
		
		JLabel lbl_if = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.label_if") );
		add(lbl_if, "cell 0 1");
		
		Object[] columns = {"attribute", "relation", "value"};
		tableModel = new DefaultTableModel(columns, 0);
		table = new FlexibleTable(tableModel);
		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE) {
					if (e.getColumn() == 0) {
						for (IRulePanelObserver o: observers) {
							o.attributeChanged(e.getFirstRow());
						}
					}
				}	
			}
		});
			
		add(table, "cell 1 1 3 1,grow");
		
		lbl_consequence = new JLabel("THEN Y =");
		add(lbl_consequence, "cell 4 1,alignx trailing");
		
		combo_class = new JComboBox();
		add(combo_class, "cell 5 1,growx");
		
		btn_removeCondition = new JButton("-");
		btn_removeCondition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (IRulePanelObserver o: observers) {	
					o.removeConditionClicked(table.getSelectedRow());
				}
			}
		});
		
		JButton btn_addCondition = new JButton("+");
		btn_addCondition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (IRulePanelObserver o: observers) {
					o.newConditionClicked();
				}
			}
		});
		add(btn_addCondition, "cell 2 2,alignx right");
		add(btn_removeCondition, "cell 3 2,alignx right");

	}

}
