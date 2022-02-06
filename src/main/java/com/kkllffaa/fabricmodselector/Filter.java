package com.kkllffaa.fabricmodselector;

import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Filter {
	
	public static final String VERSION = "1.0";
	
	
	public static void filter(List<ModCandidate> modCandidates, Map<String, Set<ModCandidate>> disabledmods) {
		
		//region init
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		Object lock = new Object();
		JFrame f = new JFrame("fabric mod selector by kkllffaa");
		f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		f.setSize(400, 500);
		f.setLayout(null);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation((size.width-f.getWidth()) / 2, (size.height-f.getHeight()) / 2);
		//endregion
		//region start exit
		JButton exit = new JButton("exit game");
		JButton start = new JButton("start game");
		exit.setBounds(50, 100, 100, 40);
		start.setBounds(150, 100, 100, 40);
		exit.addActionListener(e -> System.exit(0));
		f.add(exit);
		f.add(start);
		//endregion
		//region modlist
		final DefaultListModel<ModJCheckBox> listModel = new DefaultListModel<>();
		
		for (ModCandidate modCandidate : modCandidates) {
			if (!modCandidate.isBuiltin())
				listModel.addElement(new ModJCheckBox(modCandidate));
		}
		
		JCheckBoxList list = new JCheckBoxList(listModel);
		
		for (int i = 0; i < list.getModel().getSize(); i++) {
			list.getModel().getElementAt(i).setSelected(true);
		}
		
		f.add(new JScrollPane(list){{
			setBounds(130, 200, 200, 200);
		}});
		//endregion
		
		start.addActionListener(e -> {
			modCandidates.removeIf(candidate -> {
				if (candidate.isBuiltin()) return false;
				for (int i = 0; i < list.getModel().getSize(); i++) {
					if (candidate == list.getModel().getElementAt(i).getCandidate() && list.getModel().getElementAt(i).isSelected()) return false;
				}
				return true;
			});
			f.setVisible(false);
			synchronized (lock) {
				lock.notify();
			}
		});
		
		//region thread lock
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				synchronized (lock) {
					f.setVisible(false);
					lock.notify();
				}
			}
		});
		f.setVisible(true);
		synchronized(lock) {
			while (f.isVisible())
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		
		f.dispose();
		//endregion
		
		
	}
	
	
}
