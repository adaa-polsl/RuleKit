package adaa.analytics.rules.gui;

public interface IExpertPanelObserver {
	
	
	public void ruleAddClicked();
	public void ruleRemoveClicked(int id);
	
	public void preferredConditionAddClicked();
	public void preferredConditionRemoveClicked(int id);
	
	public void forbiddenConditionAddClicked();
	public void forbiddenConditionRemoveClicked(int id);
}
