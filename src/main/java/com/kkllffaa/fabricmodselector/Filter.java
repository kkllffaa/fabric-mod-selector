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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class Filter {
	
	public static final String VERSION = "1.2";
	
	public static void filter(List<ModCandidate> modCandidates, Map<String, Set<ModCandidate>> disabledmods) {
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) { ex.printStackTrace(); }
		
		try (ClosableJFrame f = new ClosableJFrame("fabric mod selector by kkllffaa v" + VERSION)) {
			
			//region init
			f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			f.setSize(600, 500);
			f.setResizable(false);
			f.setLayout(null);
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			f.setLocation((size.width - f.getWidth()) / 2, (size.height - f.getHeight()) / 2);
			
			f.addButton("exit game", 50, 100, 100, 40).addActionListener(e -> System.exit(0));
			JButton start = f.addButton("start game", 150, 100, 100, 40);
			JButton update = f.addButton("checking for updates", 50, 50, 100, 40, false);
			//JButton save = f.addButton("save", 150, 50, 100, 40);
			//JButton load = f.addButton("load", 150, 5, 100, 40);
			//endregion
			//region modlist
			/*
			ListPanel[] panelarray = {
					new ListPanelTree(),
					new ListPanelModules()
			};
			JComboBox<ListPanel> panel = new JComboBox<>(panelarray);
			*/
			
			
			JPanel panel = new JPanel();
			panel.setLayout(null);
			panel.setBounds(50, 200, 400, 250);
			panel.setBackground(new Color(200, 200, 200));
			
			
			JCheckBoxList list = new JCheckBoxList(new DefaultListModel<ModJCheckBox>(){{
				modCandidates.forEach(candidate -> {
					if (!candidate.isBuiltin() && candidate.isRoot()) {
						for (int i = 0; i < getSize(); i++) {
							if (candidate.getId().equals(getElementAt(i).id)) {
								getElementAt(i).add(candidate);
								return;
							}
						}
						addElement(new ModJCheckBox(candidate, true));
					}
					
				});
			}});
			
			panel.add(new JScrollPane(list) {{setBounds(50, 25, 150, 200);}});
			
			JComboBox<ModComboboxVersionContainer> versionselector = new JComboBox<>();
			versionselector.setBounds(225, 25, 100, 25);
			

			list.addListSelectionListener(e -> {
				DefaultComboBoxModel<ModComboboxVersionContainer> model = new DefaultComboBoxModel<>();
				list.getSelectedValue().candidates.forEach(candidate ->
						model.addElement(new ModComboboxVersionContainer(candidate)));
				versionselector.setModel(model);
				int i = 0;
				
				for (int j = 0; j < versionselector.getModel().getSize(); j++) {
					if (list.getSelectedValue().getSelected() == versionselector.getModel().getElementAt(j).getMod()) {
						i = j;
						break;
					}
				}
				
				
				versionselector.setSelectedIndex(i);
			});
			panel.add(versionselector);
			
			JTextArea childmods = new JTextArea();
			childmods.setEditable(false);
			childmods.setLineWrap(false);
			versionselector.addActionListener(e -> {
				if (list.getSelectedValue().select(versionselector.getModel().getElementAt(versionselector.getSelectedIndex()).getMod())) {
					childmods.setText(lambdasupplier(() -> {
						StringBuilder a = new StringBuilder();
						if (!list.getSelectedValue().getSelected().getNestedMods().isEmpty()) {
							list.getSelectedValue().getSelected().getNestedMods().forEach(candidate ->
									a.append(candidate.getId()).append(" ").append(candidate.getVersion()).append("\n"));
						}
						return a.toString();
					}));
				}

			});
			list.setSelectedIndex(0);
			panel.add(new JScrollPane(childmods) {{setBounds(225, 100, 150, 125);}});
			
			f.add(panel);
			//endregion
			
			start.addActionListener(e -> {
				/*
				JOptionPane.showMessageDialog(null, lambdasupplier(() -> {
					StringBuilder j = new StringBuilder();
					for (ModCandidate modCandidate : modCandidates) {
						j.append(modCandidate.getId()).append(" : ").append(modCandidate.getVersion()).append("\n");
					}
					return j.toString();
				}));
				*/
				
				List<ModCandidate> toloadlist = new ArrayList<>();
				for (int i = 0; i < list.getModel().getSize(); i++) {
					ModJCheckBox checkBox = list.getModel().getElementAt(i);
					if (checkBox.isSelected()) {
						toloadlist.add(checkBox.getSelected());
						new Object() { void addtolist(Collection<ModCandidate> nested) {
							if (!nested.isEmpty()) {
								toloadlist.addAll(nested);
								for (ModCandidate nestedmod : nested) {
									addtolist(nestedmod.getNestedMods());
								}
							}
						}}.addtolist(checkBox.getSelected().getNestedMods());
					}
				}
				
				
				
				modCandidates.removeIf(candidate -> !candidate.isBuiltin() && !toloadlist.contains(candidate));
				
				/*
				//todo
				modCandidates.removeIf(candidate -> {
					if (candidate.isBuiltin()) return false;
					
					for (int i = 0; i < list.getModel().getSize(); i++) {
						ModJCheckBox checkBox = list.getModel().getElementAt(i);
						if (candidate.isRoot() && checkBox.id.equals(candidate.getId())) {
							return checkBox.isSelected() && checkBox.getSelected() == candidate;
						} else if (new Object() { boolean disable(Collection<ModCandidate> candidates) {
							
							for (ModCandidate modCandidate : candidates) {
								if (modCandidate.isRoot() && checkBox.id.equals(modCandidate.getId())) {
									return modCandidate == checkBox.getSelected() && checkBox.isSelected();
								} else if (disable(modCandidate.getParentMods())) return true;
							}
							return false;
							
						}}.disable(candidate.getParentMods())) {
							return true;
						}
					}
					return false;
				});
				
				//todo
				list.foreachmodel(modJCheckBox -> {
					if (!modJCheckBox.isSelected()) {
						modCandidates.removeIf(candidate -> {
							if (!modJCheckBox.id.equals(candidate.getId())) return false;
							if (candidate.isBuiltin()) return false;
							if (candidate.isRoot()) {
								return candidate != modJCheckBox.getSelected();
							} else {
								return new Object() { boolean disable(Collection<ModCandidate> candidates) {
									for (ModCandidate modCandidate : candidates) {
										if (modCandidate.isRoot()) {
											return modCandidate == modJCheckBox.getSelected();
										} else if (disable(modCandidate.getParentMods())) return true;
									}
									return false;
								}}.disable(candidate.getParentMods());
							}
						});
					}
				});
				
				*/
				
				/*
				JOptionPane.showMessageDialog(null, lambdasupplier(() -> {
					StringBuilder j = new StringBuilder();
					for (ModCandidate modCandidate : modCandidates) {
						j.append(modCandidate.getId()).append(" : ").append(modCandidate.getVersion()).append("\n");
					}
					return j.toString();
				}));
				*/
				
				f.closenormal();
			});
			
			
			//todo
			/*
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
			*/
			
			
			
			
			Thread updatethread = new Thread(() -> update(f, update));
			updatethread.start();
			
			
			//region thread lock
			f.addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { f.closenormal(); }});
			f.waittoclose();
			//endregion
			
			if (updatethread.isAlive()) //noinspection deprecation
				updatethread.stop();
			
		}
		
		
	}
	
	public static <T> T lambdasupplier(Supplier<T> supplier) { return supplier.get(); }
	
	private static void update(JFrame f, JButton update) {
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
	}
	
	public static class ClosableJFrame extends JFrame implements AutoCloseable {
		private static final Object lock = new Object();
		public ClosableJFrame(String title) {
			super(title);
		}
		@Override
		public void close() {
			dispose();
		}
		public void waittoclose() {
			setVisible(true);
			synchronized (lock) {
				while (isVisible())
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
		}
		public void closenormal() {
			synchronized (lock) {
				setVisible(false);
				lock.notify();
			}
		}
		
		public JButton addButton(String text, int x, int y, int width, int height) {
			JButton button = new JButton(text);
			button.setBounds(x, y, width, height);
			add(button);
			return button;
		}
		public JButton addButton(String text, int x, int y, int width, int height, boolean enabled) {
			JButton button = addButton(text, x, y, width, height);
			button.setEnabled(enabled);
			return button;
		}
	}
}
