package com.kkllffaa.fabricmodselector.listpanels;

import com.kkllffaa.fabricmodselector.Filter;
import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListPanelTree extends ListPanel {
	private final JCheckBoxList<CheckBox> list;
	private final JComboBox<ModComboboxVersionContainer> versionselector;
	private final JTextArea childmods;
	
	public ListPanelTree(List<ModCandidate> candidates) {
		super();
		list = new JCheckBoxList<>(new DefaultListModel<CheckBox>(){{
			addWithChildsToList(candidates, this, false);
		}});
		versionselector = new JComboBox<>();
		versionselector.setRenderer(new CellComboBoxRenderer(versionselector.getRenderer()));
		childmods = new JTextArea();
		childmods.setEditable(false);
		childmods.setLineWrap(false);
		list.addListSelectionListener(e -> {
			DefaultComboBoxModel<ModComboboxVersionContainer> model = new DefaultComboBoxModel<>();
			for (CheckBox.ModPair candidate : list.getSelectedValue().candidates) {
				model.addElement(new ModComboboxVersionContainer(candidate));
			}
			versionselector.setModel(model);
			int selected = 0;
			for (int i = 0; i < versionselector.getModel().getSize(); i++) {
				if (list.getSelectedValue().getSelected() == versionselector.getModel().getElementAt(i).mod) {
					selected = i;
					break;
				}
			}
			versionselector.setSelectedIndex(selected);
		});
		versionselector.addActionListener(e -> {
			if (list.getSelectedValue().select(versionselector.getModel().getElementAt(versionselector.getSelectedIndex()).mod)) {
				childmods.setText(Filter.lambdasupplier(() -> {
					StringBuilder builder = new StringBuilder();
					for (ModCandidate nestedMod : list.getSelectedValue().getSelected().getNestedMods()) {
						builder.append(nestedMod.getId()).append(" ").append(nestedMod.getVersion().getFriendlyString()).append("\n");
					}
					return builder.toString();
				}));
			}
		});
		list.setSelectedIndex(0);
		add(new JScrollPane(list) {{setBounds(50, 25, 150, 200);}});
		add(new JScrollPane(childmods) {{setBounds(250, 100, 200, 125);}});
		versionselector.setBounds(250, 25, 200, 25);
		add(versionselector);
	}
	
	@Override
	public List<ModCandidate> getToLoadList() {
		List<ModCandidate> toloadlist = new ArrayList<>();
		for (int i = 0; i < list.getModel().getSize(); i++) {
			CheckBox checkBox = list.getModel().getElementAt(i);
			if (checkBox.isSelected()) {
				toloadlist.add(checkBox.getSelected());
				addAllChilds(checkBox.getSelected().getNestedMods(), toloadlist);
			}
		}
		return toloadlist;
	}
	
	@Override
	public void addMods(List<ModCandidate> candidates) {
		addWithChildsToList(candidates, ((DefaultListModel<CheckBox>) list.getModel()), true);
		list.setSelectedIndex(list.getSelectedIndex());
	}
	
	private void addWithChildsToList(List<ModCandidate> mods, DefaultListModel<CheckBox> listModel, boolean newmod) {
		candidatesloop:
		for (ModCandidate candidate : mods) {
			if (Filter.useMod(candidate) && candidate.isRoot()) {
				for (int i = 0; i < listModel.getSize(); i++) {
					if (candidate.getId().equals(listModel.getElementAt(i).id)) {
						listModel.getElementAt(i).add(new CheckBox.ModPair(candidate, newmod));
						continue candidatesloop;
					}
					
				}
				listModel.addElement(new CheckBox(candidate, true, newmod));
			}
		}
	}
	
	private void addAllChilds(Collection<ModCandidate> nested, Collection<ModCandidate> listtoadd) {
		for (ModCandidate modCandidate : nested) {
			listtoadd.add(modCandidate);
			addAllChilds(modCandidate.getNestedMods(), listtoadd);
		}
	}
	
	@Override
	public String toString() { return "select by mod versions and submodules"; }
	
	public static class CheckBox extends ModJCheckBox {
		public final ArrayList<ModPair> candidates;
		private ModCandidate selected;
		public final String id;
		
		public CheckBox(ModCandidate candidate, boolean enabled, boolean newmod) {
			super(candidate.getId(), enabled, newmod);
			this.candidates = new ArrayList<>();
			id = candidate.getId();
			candidates.add(new ModPair(candidate, newmod));
			selected = candidate;
		}
		public void add(ModPair candidate) {
			if (id.equals(selected.getId()) && !candidates.contains(candidate)) {
				candidates.add(candidate);
			}
		}
		public boolean select(ModCandidate mod) {
			if (selected == mod) return true;
			for (ModPair candidate : candidates) {
				if (candidate.mod == mod) {
					selected = candidate.mod;
					return true;
				}
			}
			return false;
		}
		public ModCandidate getSelected() {
			return selected;
		}
		
		public static class ModPair {
			public final ModCandidate mod;
			public final boolean newmod;
			
			public ModPair(ModCandidate mod, boolean newmod) {
				this.mod = mod;
				this.newmod = newmod;
			}
		}
	}
	
	public static class ModComboboxVersionContainer {
		public final ModCandidate mod;
		public final boolean newmod;
		
		public ModComboboxVersionContainer(CheckBox.ModPair modPair) {
			this.mod = modPair.mod;
			this.newmod = modPair.newmod;
		}
		
		@Override public String toString() {
			return mod.getVersion().getFriendlyString();
		}
	}
	
	public static class CellComboBoxRenderer implements ListCellRenderer<ModComboboxVersionContainer> {
		private final ListCellRenderer<? super ModComboboxVersionContainer> parent;
		
		public CellComboBoxRenderer(ListCellRenderer<? super ModComboboxVersionContainer> parent) {
			this.parent = parent;
		}
		@Override
		public Component getListCellRendererComponent(JList<? extends ModComboboxVersionContainer> list, ModComboboxVersionContainer value, int index, boolean isSelected, boolean cellHasFocus) {
			Component cp = parent.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (value.newmod)
				cp.setBackground(Filter.newmodcolor);
			
			return cp;
		}
	}
}
