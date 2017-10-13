package adaa.analytics.rules.gui;

import java.util.Map;

import adaa.analytics.rules.operator.ExpertRuleGenerator;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.dialogs.wizards.AbstractWizard;
import com.rapidminer.gui.wizards.AbstractConfigurationWizardCreator;
import com.rapidminer.gui.wizards.ConfigurationListener;
import com.rapidminer.parameter.ParameterType;


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
