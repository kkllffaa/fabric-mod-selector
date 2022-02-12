package com.kkllffaa.fabricmodselector.listpanels;

import com.kkllffaa.fabricmodselector.Filter;
import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ListPanelModules extends ListPanel {
	private final JCheckBoxList<CheckBox> list;
	
	public ListPanelModules(List<ModCandidate> candidates) {
		super();
		list = new JCheckBoxList<>(new DefaultListModel<CheckBox>(){{
			for (ModCandidate candidate : candidates) {
				if (Filter.useMod(candidate)) addElement(new CheckBox(candidate, true, false));
			}
		}});
		add(new JScrollPane(list) {{setBounds(50, 25, 400, 200);}});
	}
	
	@Override
	public List<ModCandidate> getToLoadList() {
		List<ModCandidate> toloadlist = new ArrayList<>();
		for (int i = 0; i < list.getModel().getSize(); i++) {
			CheckBox checkBox = list.getModel().getElementAt(i);
			if (checkBox.isSelected()) toloadlist.add(checkBox.candidate);
		}
		
		return toloadlist;
	}
	
	@Override
	public void addMods(List<ModCandidate> candidates) {
		DefaultListModel<CheckBox> model = ((DefaultListModel<CheckBox>) list.getModel());
		for (ModCandidate candidate : candidates) {
			if (Filter.useMod(candidate)) {
				model.addElement(new CheckBox(candidate, true, true));
			}
		}
	}
	
	@Override
	public String toString() { return "select by modules"; }
	
	public static class CheckBox extends ModJCheckBox {
		public final ModCandidate candidate;
		
		public CheckBox(ModCandidate candidate, boolean enabled, boolean newmod) {
			super(candidate.getId() + "  " + candidate.getVersion().getFriendlyString(), enabled, newmod);
			this.candidate = candidate;
		}
	}
}
