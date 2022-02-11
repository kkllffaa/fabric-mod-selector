package com.kkllffaa.fabricmodselector;

import javax.swing.*;
import java.awt.*;

public class JPanelComboBox extends JComboBox<ListPanel> {
	JPanelComboBox(Container parent, ListPanel[] panels) {
		super(panels);
		for (ListPanel panel : panels) {
			panel.setVisible(false);
			parent.add(panel);
		}
		addActionListener(e -> {
			for (ListPanel panel : panels) {
				panel.setVisible(false);
			}
			getItemAt(getSelectedIndex()).setVisible(true);
		});
	}
	public ListPanel getSelected() {
		return getItemAt(getSelectedIndex());
	}
}
