package com.kkllffaa.fabricmodselector;

import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Filter {
	
	public static final String VERSION = "1.0";
	
	
	public static void filter(List<ModCandidate> modCandidates, Map<String, Set<ModCandidate>> disabledmods) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		try (ClosableJFrame f = new ClosableJFrame("fabric mod selector by kkllffaa")) {
			
			
			//region init
			Object lock = new Object();
			f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			f.setSize(400, 500);
			f.setLayout(null);
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			f.setLocation((size.width - f.getWidth()) / 2, (size.height - f.getHeight()) / 2);
			//endregion
			
			//region start exit
			JButton exit = new JButton("exit game");
			exit.setBounds(50, 100, 100, 40);
			exit.addActionListener(e -> System.exit(0));
			f.add(exit);
			JButton start = new JButton("start game");
			start.setBounds(150, 100, 100, 40);
			f.add(start);
			JButton update = new JButton("checking for updates");
			update.setBounds(50, 50, 100, 40);
			update.setEnabled(false);
			f.add(update);
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
			
			f.add(new JScrollPane(list) {{
				setBounds(130, 200, 200, 200);
			}});
			//endregion
			
			start.addActionListener(e -> {
				modCandidates.removeIf(candidate -> {
					if (candidate.isBuiltin()) return false;
					for (int i = 0; i < list.getModel().getSize(); i++) {
						if (candidate == list.getModel().getElementAt(i).getCandidate() && list.getModel().getElementAt(i).isSelected())
							return false;
					}
					return true;
				});
				f.setVisible(false);
				synchronized (lock) {
					lock.notify();
				}
			});
			
			
			Thread updatethread = new Thread(() -> {
				String path;
				
				try {
					path = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("net/minecraft/client/main/Main.class")).getPath();
					path = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
					path = URLDecoder.decode(path, "UTF-8");
					path = path.substring(1, path.length() - 4) + ".json";
				} catch (Exception ignored) {
					path = "";
				}
				
				
				Update u = Update.create(new File(path), 10);
				if (u == null) {
					update.setText("cannot check for updates");
				} else if (u.installed.equals(u.latest)) {
					update.setText("you are up to date");
				} else {
					update.setText("update to " + u.latest);
					update.setEnabled(true);
					update.addActionListener(e -> {
						int option = JOptionPane.showOptionDialog(f, "to apply " + u.latest + " update restart game and launcher", "update",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
								new String[]{"cancel update", "update and restart later", "update and exit"}, null);
						switch (option) {
							case 1:
								if (u.updatefile(u.latest)) {
									update.setText("restart launcher to see effect");
									update.setEnabled(false);
									for (ActionListener actionListener : update.getActionListeners()) {
										update.removeActionListener(actionListener);
									}
								} else JOptionPane.showMessageDialog(f, "error while updating");
								break;
							case 2:
								if (u.updatefile(u.latest)) System.exit(0);
								else JOptionPane.showMessageDialog(f, "error while updating");
								break;
						}
					});
				}
			});
			updatethread.start();
			
			
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
			synchronized (lock) {
				while (f.isVisible())
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
			//endregion
			
			if (updatethread.isAlive()) //noinspection deprecation
				updatethread.stop();
			
		}
		
		
	}
	
	
	
	public static class ClosableJFrame extends JFrame implements AutoCloseable {
		public ClosableJFrame(String title) {
			super(title);
		}
		@Override
		public void close() {
			dispose();
		}
	}
}
