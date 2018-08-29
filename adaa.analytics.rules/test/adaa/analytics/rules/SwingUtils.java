/*package adaa.analytics.rules;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExecutionMode;
import com.rapidminer.gui.docking.RapidDockableContainerFactory;
import com.rapidminer.gui.look.RapidLookAndFeel;
import com.rapidminer.gui.look.ui.RapidDockingUISettings;
import com.rapidminer.gui.properties.GenericParameterPanel;
import com.rapidminer.operator.Operator;
import com.vlsolutions.swing.docking.DockableContainerFactory;

public final class SwingUtils {

    private static Thread edt;

    private SwingUtils() {}

    *//**
     * Czeka az glowny watek Swing sie zakonczy.
     * Metoda moze byc przydatna podczas uruchamiania GUI poprzez JUnit,
     * gdyz JUnit zabija watki przy konczeniu metody testujacej.
     *//*
    public static void waitForEventDispatchThread() {

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    edt = Thread.currentThread();
                }
            });

            if (edt != null) {
                edt.join();
            }
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
	public static <T extends Component> List<T> getComponents(Container container, Class<T> clazz) {
        List<T> selectedComponents = new ArrayList<T>();

        Component[] components = container.getComponents();
        for (Component c : components) {
            if (c.getClass().isAssignableFrom(clazz)) {
                selectedComponents.add((T) c);
            }

            if (c instanceof Container) {
                selectedComponents.addAll(getComponents((Container) c, clazz));
            }
        }

        return selectedComponents;
    }

    public static void setRapidMinerLookAndFeel() {
        RapidMiner.setExecutionMode(ExecutionMode.EMBEDDED_WITH_UI);

        DockingUISettings.setInstance(new RapidDockingUISettings());
        DockableContainerFactory.setFactory(new RapidDockableContainerFactory());
        try {
            UIManager.setLookAndFeel(new RapidLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void showOperatorParameterPanel(Operator operator) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));

        frame.getContentPane().add(
            new GenericParameterPanel(operator.getParameters()));

        frame.pack();
        frame.setVisible(true);
    }
}
*/