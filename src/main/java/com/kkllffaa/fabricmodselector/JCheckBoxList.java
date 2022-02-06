package com.kkllffaa.fabricmodselector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JCheckBoxList extends JList<ModJCheckBox> {
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	
	public JCheckBoxList() {
		setCellRenderer(new CellRenderer());
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if (index != -1) {
					JCheckBox checkbox = getModel().getElementAt(index);
					checkbox.setSelected(!checkbox.isSelected());
					repaint();
				}
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int index = getSelectedIndex();
				if (index != -1 && e.getKeyCode() == KeyEvent.VK_SPACE) {
					boolean newVal = !getModel()
							.getElementAt(index).isSelected();
					for (int i : getSelectedIndices()) {
						JCheckBox checkbox = getModel()
								.getElementAt(i);
						checkbox.setSelected(newVal);
						repaint();
					}
				}
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public JCheckBoxList(ListModel<ModJCheckBox> model){
		this();
		setModel(model);
	}
	
	protected class CellRenderer implements ListCellRenderer<ModJCheckBox> {
		public Component getListCellRendererComponent(
				JList<? extends ModJCheckBox> list, ModJCheckBox value, int index,
				boolean isSelected, boolean cellHasFocus) {
			
			//Drawing checkbox, change the appearance here
			value.setBackground(isSelected ? getSelectionBackground()
					: getBackground());
			value.setForeground(isSelected ? getSelectionForeground()
					: getForeground());
			value.setEnabled(isEnabled());
			value.setFont(getFont());
			value.setFocusPainted(false);
			value.setBorderPainted(true);
			value.setBorder(isSelected ? UIManager
					.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			return value;
		}
	}
}
