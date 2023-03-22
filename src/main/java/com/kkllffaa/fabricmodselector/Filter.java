package com.kkllffaa.fabricmodselector;

import com.kkllffaa.fabricmodselector.listpanels.ListPanel;
import com.kkllffaa.fabricmodselector.listpanels.ListPanelModules;
import com.kkllffaa.fabricmodselector.listpanels.ListPanelTree;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.discovery.ModCandidate;
import net.fabricmc.loader.impl.discovery.ModDiscoverer;
import net.fabricmc.loader.impl.discovery.ModResolutionException;

import net.fabricmc.loader.impl.metadata.VersionOverrides;
import net.fabricmc.loader.impl.metadata.DependencyOverrides;



import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;

public class Filter {
	
	public static final String VERSION = "1.5";
	
	public static final Color newmodcolor = new Color(0, 200, 100);
	
	public static void filter(
		List<ModCandidate> modCandidates,
		FabricLoaderImpl loader,
		Map<String, Set<ModCandidate>> disabledmods,
		boolean isdevelopment,
		VersionOverrides versionOverrides,
		DependencyOverrides depOverrides
		) {


		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { JOptionPane.showMessageDialog(null, e, "error",JOptionPane.ERROR_MESSAGE); }
		
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
			JButton update = f.addButton("checking for updates", 50, 50, 200, 40, false);
			//JButton save = f.addButton("save", 150, 50, 100, 40);
			//JButton load = f.addButton("load", 150, 5, 100, 40);
			//endregion
			//region modlist
			
			ListPanel[] panelarray = {
					new ListPanelTree(modCandidates),
					new ListPanelModules(modCandidates)
			};
			JComboBox<ListPanel> panel = new JComboBox<>(panelarray);
			panel.setBounds(50, 150, 250, 25);
			panel.addActionListener(e -> {
				for (int i = 0; i < panel.getModel().getSize(); i++) {
					panel.getModel().getElementAt(i).setVisible(false);
				}
				listfromcombobox(panel).setVisible(true);
			});
			panel.setSelectedIndex(0);
			
			f.add(panel);
			for (ListPanel listPanel : panelarray) {
				listPanel.setBounds(50, 200, 500, 250);
				listPanel.setBackground(new Color(200, 200, 200));
				f.add(listPanel);
			}
			//endregion
			//region dragdropmods
			JLabel dragdropmods = new JLabel("drop mod jar here to load", SwingConstants.CENTER);
			dragdropmods.setDropTarget(new DropTarget() {public synchronized void drop(DropTargetDropEvent event) {
				event.acceptDrop(DnDConstants.ACTION_COPY);
				
				Transferable transferable = event.getTransferable();
				DataFlavor[] flavors = transferable.getTransferDataFlavors();
				
				List<File> modfiles = new ArrayList<>();
				
				for (DataFlavor flavor : flavors) { try {
						if (flavor.isFlavorJavaFileListType()) {
							for (Object fileobj : (List<?>) transferable.getTransferData(flavor)) {
								modfiles.add(((File) fileobj));
							}
						}
					} catch (Exception e) { JOptionPane.showMessageDialog(null, e); }
				}
				if (!modfiles.isEmpty()) { try {
						ModDiscoverer discoverer = new ModDiscoverer(versionOverrides, depOverrides);
						discoverer.addCandidateFinder(new FileModCandidateFinder(modfiles, isdevelopment));
						List<ModCandidate> newcandidates = discoverer.discoverMods(loader, disabledmods);
						newcandidates.removeIf(candidate -> !useMod(candidate));
						modCandidates.addAll(newcandidates);
						for (int i = 0; i < panel.getModel().getSize(); i++) {
							ListPanel p = panel.getItemAt(i);
							p.addMods(newcandidates);
						}
					} catch (ModResolutionException e) { JOptionPane.showMessageDialog(f, e); }
				}
			}});
			dragdropmods.setBounds(350, 20, 200, 100);
			dragdropmods.setOpaque(true);
			dragdropmods.setBackground(new Color(200, 100, 100));
			f.add(dragdropmods);
			//endregion
			
			start.addActionListener(e -> {
				//JOptionPane.showMessageDialog(null, listmods(modCandidates));
				listfromcombobox(panel).apply(modCandidates);
				//JOptionPane.showMessageDialog(null, listmods(modCandidates));
				
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
			
			
			
			
			Thread updatethread = new Thread(() -> update(update));
			updatethread.start();
			
			
			f.addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { f.closenormal(); }});
			f.waittoclose();
			
			if (updatethread.isAlive()) //noinspection deprecation
				updatethread.stop();
			
		}
	}
	
	public static boolean useMod(ModCandidate candidate) { return !candidate.isBuiltin() && !candidate.getId().equals("fabricloader"); }
	
	public static String listmods(Collection<ModCandidate> mods) {
		StringBuilder j = new StringBuilder();
		for (ModCandidate modCandidate : mods) {
			j.append(modCandidate.getId()).append(" : ").append(modCandidate.getVersion()).append("\n");
		}
		return j.toString();
	}
	
	public static ListPanel listfromcombobox(JComboBox<ListPanel> panelComboBox) {
		return panelComboBox.getModel().getElementAt(panelComboBox.getSelectedIndex());
	}
	
	public static <T> T lambdasupplier(Supplier<T> supplier) { return supplier.get(); }
	
	private static void update(JButton update) {
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
				
				int option = JOptionPane.showOptionDialog(null, "to apply " + u.latest + " update restart game and launcher", "update",
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
						} else JOptionPane.showMessageDialog(null, "error while updating");
						break;
					case 2:
						if (u.updatefile(u.latest)) System.exit(0);
						else JOptionPane.showMessageDialog(null, "error while updating");
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
