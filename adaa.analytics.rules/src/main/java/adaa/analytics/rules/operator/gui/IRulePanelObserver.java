package adaa.analytics.rules.operator.gui;

public interface IRulePanelObserver {
	public void newConditionClicked();
	public void removeConditionClicked(int row);
	public void attributeChanged(int row);
}
