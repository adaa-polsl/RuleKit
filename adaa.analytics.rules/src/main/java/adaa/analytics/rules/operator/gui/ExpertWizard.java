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

import adaa.analytics.rules.operator.ExpertRuleGenerator;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.dialogs.wizards.AbstractWizard;
import com.rapidminer.gui.wizards.AbstractConfigurationWizardCreator;
import com.rapidminer.gui.wizards.ConfigurationListener;
import com.rapidminer.parameter.ParameterType;

import java.util.Map;

/**
 * This class is an element of RapidMiner plugin user interface. It represents a wizard for defining user's knowledge.
 *
 * @author Adam Gudys
 */
public class ExpertWizard extends AbstractWizard {

	public ExpertWizard(String key, Object[] arguments) {
		 super(RapidMinerGUI.getMainFrame(), key, arguments);
		 
		 ExpertRuleGenerator generator = (ExpertRuleGenerator)arguments[0];
		 addStep(new ExpertWizardStep("expert_wizard_step", generator));
		 layoutDefault(HUGE);
	}

	public static class ExpertWizardCreator extends AbstractConfigurationWizardCreator {
		

		private static final long serialVersionUID = -7957103859080402742L;

		public ExpertWizardCreator() {
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public String getI18NKey() {
			return "expert_wizard";
		}
	
		@Override
		public void setParameters(Map<String, String> parameters) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public Map<String, String> getParameters() {
			// TODO Auto-generated method stub
			return null;
		}
	
		@Override
		public void createConfigurationWizard(ParameterType type,
				ConfigurationListener listener) {

			ExpertRuleGenerator sourceOperator = (ExpertRuleGenerator) listener;
			
			// create wizard depending on the operator context
			new ExpertWizard(getI18NKey(), new Object[]{sourceOperator}).setVisible(true);
		}
	
	}
	
	
}
