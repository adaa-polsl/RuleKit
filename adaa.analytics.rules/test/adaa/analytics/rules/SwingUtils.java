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
