package com.kkllffaa.fabricmodselector;

import com.google.gson.Gson;
import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Filter {
	
	public static final String VERSION = "1.1";
	
	
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
			//region buttons
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
			JButton save = new JButton("save");
			save.setBounds(150, 50, 100, 40);
			f.add(save);
			JButton load = new JButton("load");
			load.setBounds(150, 5, 100, 40);
			f.add(load);
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
			
			//region save load
			File savefile = null;
			try {
				savefile = new File(Objects.requireNonNull(Save.getmcjarlocation()).getParent() + "/h.json");
			} catch (Exception ignored) {}
			
			if (savefile != null) {
				File finalSavefile = savefile;
				AtomicBoolean loadadded = new AtomicBoolean(false);
				ActionListener loadaction = e -> {
					try {
						String json = new String(Files.readAllBytes(finalSavefile.toPath()));
						
						Save.ModListHolder modListHolder = new Gson().fromJson(json, Save.ModListHolder.class);
						
						
						modListHolder.apply(list.getModel());
						list.repaint();
					} catch (Exception exception) { JOptionPane.showMessageDialog(f, exception); }
				};
				save.addActionListener(e -> {
					
					
					Save.ModListHolder modListHolder = new Save.ModListHolder(list.getModel());
					
					
					String json = new Gson().toJson(modListHolder);
					
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(finalSavefile))) {
						writer.write(json);
						if (!loadadded.get()) {
							load.addActionListener(loadaction);
							load.setText("load");
							load.setEnabled(true);
							loadadded.set(true);
						}
					} catch (Exception exception) { JOptionPane.showMessageDialog(f, exception); }
					
				});
				
				
				if (savefile.exists()) {
					load.addActionListener(loadaction);
					loadadded.set(true);
				}
				else {
					load.setText("no save file found");
					load.setEnabled(false);
				}
			} else {
				String a = "filed to get save location";
				save.setText(a);
				load.setText(a);
				save.setEnabled(false);
				load.setEnabled(false);
			}
			//endregion
			
			
			
			
			
			Thread updatethread = new Thread(() -> {
				File mcjar = Save.getmcjarlocation();
				File a = null;
				if (mcjar != null) {
					a = new File(mcjar.toString().substring(0, mcjar.toString().length() - 4) + ".json");
				}
				Update u = Update.create(a, 10);
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
