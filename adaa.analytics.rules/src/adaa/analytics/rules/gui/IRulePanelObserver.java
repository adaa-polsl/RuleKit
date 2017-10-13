package adaa.analytics.rules.gui;

public interface IRulePanelObserver {
	public void newConditionClicked();
	public void removeConditionClicked(int row);
	public void attributeChanged(int row);
}
