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

import com.rapidminer.tools.I18N;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an element of RapidMiner plugin user interface. It represents a main panel displayed by {@link ExpertWizardStep}.
 *
 * @author Adam Gudys
 */
public class ExpertPanel extends JPanel {

	public enum Category {
		PREFERRED_RULE,
		PREFERRED_CONDITION,
		FORBIDDEN_CONDITION
	};
	
	private JLabel label_condition;
	private JLabel label_rules;
	private JLabel label_conditions;
	private JLabel label_forbiddenConditions;
	private JComboBox<String> comboBox_discreteValue;
	
	private JTable table_rules;
	private JTable table_conditions;
	private JTable table_forbiddenConditions;
	
	protected List<IExpertPanelObserver> observers = new ArrayList<IExpertPanelObserver>();
	private JButton button_addAsRule;
	private JButton button_addAsPreferredCondition;
	private JButton button_addAsForbiddenCondition;
	private JPanel rulePanel;
	
	public int getCategorySize(Category category) {  
		switch (category) {
			case PREFERRED_RULE: return table_rules.getRowCount();
			case PREFERRED_CONDITION: return table_conditions.getRowCount();
			case FORBIDDEN_CONDITION: return table_forbiddenConditions.getRowCount();
		}	
		return -1;
	}
	
	public String getRuleAt(Category category, int id) {
		switch (category) {
			case PREFERRED_RULE: return (String)table_rules.getModel().getValueAt(id, 0);
			case PREFERRED_CONDITION: return (String)table_conditions.getModel().getValueAt(id, 0);
			case FORBIDDEN_CONDITION: return (String)table_forbiddenConditions.getModel().getValueAt(id, 0);
		}	
		return "";
	}
	
	public int getCountAt(Category category, int id) {
		switch (category) {
			case PREFERRED_RULE: return 1;
			case PREFERRED_CONDITION: return Integer.parseInt((String)table_conditions.getModel().getValueAt(id, 1));
			case FORBIDDEN_CONDITION: return 1;
		}	
		return -1;
	}
	
	public RulePanel getRulePanel() { return (RulePanel)rulePanel; }
	

	public String getDiscreteValue() { return (String)comboBox_discreteValue.getSelectedItem(); }
	public int getDiscreteValueId() { return comboBox_discreteValue.getSelectedIndex(); }
	public void addDiscreteValues(Iterable<String> values) {
		comboBox_discreteValue.removeAllItems();
		for (String v: values) {
			comboBox_discreteValue.addItem(v);
		}
	}

	
	public void addToCategory(String rule, int count, Category category) {

		if (category == Category.PREFERRED_RULE || category == Category.FORBIDDEN_CONDITION) {
			DefaultTableModel m = (category == Category.PREFERRED_RULE)
					? (DefaultTableModel)table_rules.getModel() 
					: (DefaultTableModel)table_forbiddenConditions.getModel();
			Object[] row = {rule};
			m.addRow(row);
		} else {
			DefaultTableModel m = (DefaultTableModel)table_conditions.getModel();
			Object[] row = {rule, "" + count};
			m.addRow(row);
		}
	}
	

	public void removeFromCategory(int id, Category category) {
		DefaultTableModel m = null;
		switch (category) {
		case PREFERRED_RULE: 
				m = (DefaultTableModel)table_rules.getModel(); break;
		case PREFERRED_CONDITION: 
				m = (DefaultTableModel)table_conditions.getModel(); break;
		case FORBIDDEN_CONDITION: 
				m = (DefaultTableModel)table_forbiddenConditions.getModel(); break;
		}
		
		m.removeRow(id);
	}
	
	public void registerObserver(IExpertPanelObserver o) {
		observers.add(o);
	}
	
	/**
	 * Create the panel.
	 */
	public ExpertPanel() {
		setLayout(new MigLayout("", "[214.00,grow][52.00,grow][grow][84.00,grow][grow]", "[][20.00][][66.00][35.00,bottom][][18.00,grow][38.00,bottom][][grow][39.00,bottom][][grow][grow]"));
		
		label_condition = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.label_condition"));
		add(label_condition, "cell 0 0 2 1");
		
		comboBox_discreteValue = new JComboBox<String>();
		
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(20);
		format.setMinimumFractionDigits(1);
		
		rulePanel = new RulePanel();
		add(rulePanel, "cell 0 2 4 1,grow");
		
		label_rules = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.label_rules"));
		add(label_rules, "flowx,cell 0 4");
		
		button_addAsRule = new JButton(I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.button_addAsRule"));
		button_addAsRule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (IExpertPanelObserver o : observers) { o.ruleAddClicked(); }
			}
		});
		add(button_addAsRule, "cell 0 5");
		
		table_rules = new JTable();
		add(table_rules, "cell 0 6 5 1,grow");
		
		DefaultTableModel model = (DefaultTableModel)table_rules.getModel();
		model.addColumn("column_rule");
		model.addColumn("column_remove");
		ColumnButton button = new ColumnButton(table_rules, model.getColumnCount() - 1);
		
		button.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int id = Integer.parseInt(arg0.getActionCommand());
				for (IExpertPanelObserver o: observers) { o.ruleRemoveClicked(id); }
			}
		});	
		
		label_conditions = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.label_conditions"));
		add(label_conditions, "flowx,cell 0 7");
		
		button_addAsPreferredCondition = new JButton(I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.button_addAsPreferredCondition"));
		button_addAsPreferredCondition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (IExpertPanelObserver o: observers) { o.preferredConditionAddClicked(); }
			}
		});
		add(button_addAsPreferredCondition, "cell 0 8");
		
		table_conditions = new JTable();
		add(table_conditions, "cell 0 9 5 1,grow");
		model = (DefaultTableModel)table_conditions.getModel();
		model.addColumn("column_rule");
		model.addColumn("column_count");
		model.addColumn("column_remove");
		button = new ColumnButton(table_conditions, model.getColumnCount() - 1);
		button.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int id = Integer.parseInt(arg0.getActionCommand());
				for (IExpertPanelObserver o: observers) { o.preferredConditionRemoveClicked(id); }
			}
		});	
		
		label_forbiddenConditions = new JLabel(I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.label_forbiddenConditions"));
		add(label_forbiddenConditions, "flowx,cell 0 10");
		
		button_addAsForbiddenCondition = new JButton(I18N.getMessage(I18N.getGUIBundle(), "gui.expert_panel.button_addAsForbiddenCondition"));
		button_addAsForbiddenCondition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (IExpertPanelObserver o: observers) { o.forbiddenConditionAddClicked(); }
			}
		});
		add(button_addAsForbiddenCondition, "cell 0 11");
		
		table_forbiddenConditions = new JTable();
		add(table_forbiddenConditions, "cell 0 12 5 1,grow");
		model = (DefaultTableModel)table_forbiddenConditions.getModel();
		model.addColumn("column_rule");
		model.addColumn("column_remove");
		button = new ColumnButton(table_forbiddenConditions, model.getColumnCount() - 1);
		button.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int id = Integer.parseInt(arg0.getActionCommand());
				for (IExpertPanelObserver o: observers) { o.forbiddenConditionRemoveClicked(id); }
			}
		});
	}

}
