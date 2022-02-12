package com.kkllffaa.fabricmodselector.listpanels;

import com.kkllffaa.fabricmodselector.Filter;
import com.kkllffaa.fabricmodselector.JCheckBoxList;
import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListPanelTree extends ListPanel {
	private final JCheckBoxList<CheckBox> list;
	private final JComboBox<ModComboboxVersionContainer> versionselector;
	private final JTextArea childmods;
	
	public ListPanelTree(Collection<ModCandidate> candidates) {
		super();
		list = new JCheckBoxList<>(new DefaultListModel<CheckBox>(){{
			candidatesloop:
			for (ModCandidate candidate : candidates) {
				if (!candidate.isBuiltin() && candidate.isRoot()) {
					for (int i = 0; i < getSize(); i++) {
						if (candidate.getId().equals(getElementAt(i).id)) {
							getElementAt(i).add(candidate);
							continue candidatesloop;
						}
						
					}
					addElement(new CheckBox(candidate, true));
				}
			}
		}});
		versionselector = new JComboBox<>();
		childmods = new JTextArea();
		childmods.setEditable(false);
		childmods.setLineWrap(false);
		list.addListSelectionListener(e -> {
			DefaultComboBoxModel<ModComboboxVersionContainer> model = new DefaultComboBoxModel<>();
			for (ModCandidate candidate : list.getSelectedValue().candidates)
			{ model.addElement(new ModComboboxVersionContainer(candidate)); }
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
		add(new JScrollPane(childmods) {{setBounds(255, 100, 150, 125);}});
		versionselector.setBounds(255, 25, 100, 25);
		add(versionselector);
	}
	
	@Override
	public List<ModCandidate> getToLoadList() {
		List<ModCandidate> toloadlist = new ArrayList<>();
		for (int i = 0; i < list.getModel().getSize(); i++) {
			CheckBox checkBox = list.getModel().getElementAt(i);
			if (checkBox.isSelected()) {
				toloadlist.add(checkBox.getSelected());
				addallchilds(checkBox.getSelected().getNestedMods(), toloadlist);
			}
		}
		return toloadlist;
	}
	
	private void addallchilds(Collection<ModCandidate> nested, Collection<ModCandidate> listtoadd) {
		for (ModCandidate modCandidate : nested) {
			listtoadd.add(modCandidate);
			addallchilds(modCandidate.getNestedMods(), listtoadd);
		}
	}
	
	@Override
	public String toString() { return "ListPanelTree"; }
	
	public static class CheckBox extends JCheckBox {
		public final ArrayList<ModCandidate> candidates;
		private ModCandidate selected;
		public final String id;
		
		public CheckBox(ModCandidate candidate, boolean enabled) {
			super(candidate.getId());
			this.candidates = new ArrayList<>();
			id = candidate.getId();
			candidates.add(candidate);
			selected = candidate;
			setSelected(enabled);
		}
		public void add(ModCandidate candidate) {
			if (id.equals(selected.getId()) && !candidates.contains(candidate)) {
				candidates.add(candidate);
			}
		}
		public boolean select(ModCandidate mod) {
			if (selected == mod) return true;
			for (ModCandidate candidate : candidates) {
				if (candidate == mod) {
					selected = candidate;
					return true;
				}
			}
			return false;
		}
		public ModCandidate getSelected() {
			return selected;
		}
	}
	
	public static class ModComboboxVersionContainer {
		public final ModCandidate mod;
		public ModComboboxVersionContainer(ModCandidate mod) {
			this.mod = mod;
		}
		
		@Override public String toString() {
			return mod.getVersion().getFriendlyString();
		}
	}
}
