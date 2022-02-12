package com.kkllffaa.fabricmodselector.listpanels;

import com.kkllffaa.fabricmodselector.Filter;
import com.kkllffaa.fabricmodselector.listpanels.ModJCheckBox;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JCheckBoxList<T extends ModJCheckBox> extends JList<T> {
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	
	public JCheckBoxList() {
		setCellRenderer(new CellRenderer());
		addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if (index != -1) {
					grabFocus();
					if (e.getButton() == 1) {
						JCheckBox checkbox = getModel().getElementAt(index);
						checkbox.setSelected(!checkbox.isSelected());
						repaint();
					}else if (e.getButton() == 3) {
						setSelectedIndex(index);
						repaint();
					}

				}
			}
		});
		addKeyListener(new KeyAdapter() { @Override public void keyPressed(KeyEvent e) {
				int index = getSelectedIndex();
				if (index != -1 && e.getKeyCode() == KeyEvent.VK_SPACE) {
					boolean newVal = !getModel().getElementAt(index).isSelected();
					for (int i : getSelectedIndices()) {
						JCheckBox checkbox = getModel().getElementAt(i);
						checkbox.setSelected(newVal);
						repaint();
					}
				}
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public JCheckBoxList(ListModel<T> model){
		this();
		setModel(model);
	}
	
	protected class CellRenderer implements ListCellRenderer<T> {
		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
			
			//Drawing checkbox, change the appearance here
			value.setBackground(isSelected ? getSelectionBackground() : value.newmod ? Filter.newmodcolor : getBackground());
			value.setForeground(isSelected ? getSelectionForeground() : getForeground());
			
			value.setEnabled(isEnabled());
			value.setFont(getFont());
			value.setFocusPainted(false);
			value.setBorderPainted(true);
			value.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			return value;
		}
	}
}
