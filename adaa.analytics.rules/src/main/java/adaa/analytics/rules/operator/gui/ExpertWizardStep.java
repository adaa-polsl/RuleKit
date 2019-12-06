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

import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.operator.ExpertRuleGenerator;
import adaa.analytics.rules.operator.gui.ExpertPanel.Category;
import com.rapidminer.gui.tools.dialogs.wizards.AbstractWizard.WizardStepDirection;
import com.rapidminer.gui.tools.dialogs.wizards.WizardStep;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is an element of RapidMiner plugin user interface. It represents a wizard step for defining user's knowledge.
 *
 * @author Adam Gudys
 */
public class ExpertWizardStep extends WizardStep implements IExpertPanelObserver, IRulePanelObserver {

	protected ExpertPanel panel = new ExpertPanel();
	
	protected ExpertRuleGenerator generator;
	
	protected ExampleSetMetaData setMeta;
	
	public ExpertWizardStep(String i18nKey, ExpertRuleGenerator generator) {
		super(i18nKey);
		this.generator = generator;
		
		try {		
			setMeta = (ExampleSetMetaData) generator.getExampleSetInputPort().getMetaData();
			AttributeMetaData labelMeta = setMeta.getLabelMetaData();
				
			Collection<AttributeMetaData> attributes = setMeta.getAllAttributes();
			List<String> attrNames = new ArrayList<String>();
			for (AttributeMetaData a : attributes) {
				if (!a.getName().equals(labelMeta.getName())) {
					attrNames.add(a.getName());
				}
			}
			
			// get rules from operator parameter
			List<String[]> ruleList = generator.getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_RULES);
			for (String[] e: ruleList) {
				Rule r = RuleParser.parseRule(e[1], setMeta);
				panel.addToCategory(r.toString(), 1, ExpertPanel.Category.PREFERRED_RULE);
			}
			
			Pattern pattern = Pattern.compile("(?<count>\\d+):\\s*(?<rule>.*)");
			
			ruleList = generator.getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_PREFERRED_CONDITIONS);
			for (String[] e: ruleList) {
		    	Matcher matcher = pattern.matcher(e[1]);
		    	matcher.find();
		    	String count = matcher.group("count");
		    	String ruleDesc = matcher.group("rule");
		    	
				Rule r = RuleParser.parseRule(ruleDesc, setMeta);
				panel.addToCategory(r.toString(), Integer.parseInt(count), ExpertPanel.Category.PREFERRED_CONDITION);
			}
			
			ruleList = generator.getParameterList(ExpertRuleGenerator.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS);
			for (String[] e: ruleList) {
		    	Rule r = RuleParser.parseRule(e[1], setMeta);
				panel.addToCategory(r.toString(), 1, ExpertPanel.Category.FORBIDDEN_CONDITION);
			}
			
			panel.registerObserver(this);
			panel.getRulePanel().registerObserver(this);
			panel.getRulePanel().setDecisionAttribute(labelMeta.getName());
			
			if (labelMeta.isNominal()) {
				panel.getRulePanel().addDecisionClasses(labelMeta.getValueSet());
			} else {
				panel.getRulePanel().hideDecisionClasses();
			}
	
		} catch (OperatorException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected JComponent getComponent() {
		return panel;
	}

	@Override
	protected boolean canProceed() {
		return true;
	}

	@Override
	protected boolean canGoBack() {
		return false;
	}
	
	@Override
	protected boolean performLeavingAction(WizardStepDirection direction) {
		// iterate over rules and set operator parameter
		
		List<String[]> ruleList = new ArrayList<String[]>();
		for (int id = 0; id < panel.getCategorySize(Category.PREFERRED_RULE); ++id) {
			String desc =  panel.getRuleAt(Category.PREFERRED_RULE, id);
			String[] entry = new String[]{"rule-" + id, desc };
			ruleList.add(entry);
		}
		generator.setListParameter(ExpertRuleGenerator.PARAMETER_EXPERT_RULES, ruleList);
		
		ruleList.clear();
		for (int id = 0; id < panel.getCategorySize(Category.PREFERRED_CONDITION); ++id) {
			String desc =  panel.getCountAt(Category.PREFERRED_CONDITION, id) + ": " +  panel.getRuleAt(Category.PREFERRED_CONDITION, id);
			String[] entry = new String[]{"preferred-condition-" + id, desc };
			ruleList.add(entry);
		}
		generator.setListParameter(ExpertRuleGenerator.PARAMETER_EXPERT_PREFERRED_CONDITIONS, ruleList);
		
		ruleList.clear();
		for (int id = 0; id < panel.getCategorySize(Category.FORBIDDEN_CONDITION); ++id) {
			String desc =  panel.getRuleAt(Category.FORBIDDEN_CONDITION, id);
			String[] entry = new String[]{"forbidden-condition-" + id, desc };
			ruleList.add(entry);
		}
		generator.setListParameter(ExpertRuleGenerator.PARAMETER_EXPERT_FORBIDDEN_CONDITIONS, ruleList);
		
		return true;
	};

	
	@Override
	public void ruleAddClicked() {
		Rule r = parseRuleFromPanel(ConditionBase.Type.FORCED);
		if (r.getPremise().getSubconditions().size() > 0) {
			panel.addToCategory(r.toString(), 1, ExpertPanel.Category.PREFERRED_RULE);
		//	panel.getRulePanel().removeAllConditions();
		}
	}

	@Override
	public void ruleRemoveClicked(int id) {
		panel.removeFromCategory(id, ExpertPanel.Category.PREFERRED_RULE);	
	}
	
	@Override
	public void preferredConditionAddClicked() {
		Rule r = parseRuleFromPanel(ConditionBase.Type.PREFERRED);
		if (r.getPremise().getSubconditions().size() > 0) {
			panel.addToCategory(r.toString(), 1, ExpertPanel.Category.PREFERRED_CONDITION);
		//	panel.getRulePanel().removeAllConditions();
		}
	}

	@Override
	public void preferredConditionRemoveClicked(int id) {
		panel.removeFromCategory(id, ExpertPanel.Category.PREFERRED_CONDITION);	
	}
	
	@Override
	public void forbiddenConditionAddClicked() {
		Rule r = parseRuleFromPanel(ConditionBase.Type.NORMAL);
		if (r.getPremise().getSubconditions().size() > 0) {
			panel.addToCategory(r.toString(), 1, ExpertPanel.Category.FORBIDDEN_CONDITION);
		//	panel.getRulePanel().removeAllConditions();
		}
	}

	@Override
	public void forbiddenConditionRemoveClicked(int id) {
		panel.removeFromCategory(id, ExpertPanel.Category.FORBIDDEN_CONDITION);	
	}

	
	protected Rule parseRuleFromPanel(ConditionBase.Type type) {
		RulePanel rp = panel.getRulePanel(); 	
		Rule rule = null;	
		AttributeMetaData labelMeta = setMeta.getLabelMetaData();
		
		if (labelMeta.isNominal()) {
			// create classification rule
			List<String> mapping = new ArrayList<String>();
			mapping.addAll(labelMeta.getValueSet());	
			
			rule = new ClassificationRule(
				new CompoundCondition(), 
				new ElementaryCondition(labelMeta.getName(), new SingletonSet(rp.getDecisionClassId(), mapping)));	
		} else {
			// create regression rule
			rule = new RegressionRule(
				new CompoundCondition(),
				new ElementaryCondition(labelMeta.getName(), new SingletonSet(Double.NaN, null)));
		}
		
		rule.getPremise().setType(type);
			
		for (int cid = 0; cid < rp.getConditionsCount(); ++cid) {
			RulePanel.ConditionRow row = rp.getConditionRow(cid);
			
			if (row.isComplete()) {
				AttributeMetaData a = setMeta.getAttributeByName(row.attribute);
				IValueSet set = null;
	
				if (row.value.equals("")) {
					set = new Universum();
				} else if (a.isNominal()) {
					List<String> mapping = new ArrayList<String>();
					mapping.addAll(a.getValueSet());
					double value = (double)mapping.indexOf(row.value);
					set = new SingletonSet(value, mapping);
				} else {
					double value = Double.parseDouble(row.value);
					set = new Interval(value, row.relation);
				}
				ElementaryCondition cnd = new ElementaryCondition(a.getName(), set);
				rule.getPremise().addSubcondition(cnd);
			}
		}
		
		return rule;
	}
	
	@Override
	public void newConditionClicked() {
		panel.getRulePanel().addCondition(setMeta);	
	}

	@Override
	public void attributeChanged(int row) {
		panel.getRulePanel().updateCondition(setMeta, row);
		
	}

	@Override
	public void removeConditionClicked(int row) {
		panel.getRulePanel().removeCondition(row);
	}


}
