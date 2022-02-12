package com.kkllffaa.fabricmodselector.listpanels;

import com.kkllffaa.fabricmodselector.JCheckBoxList;
import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListPanelModules extends ListPanel {
	private final JCheckBoxList<CheckBox> list;
	
	public ListPanelModules(Collection<ModCandidate> candidates) {
		super();
		list = new JCheckBoxList<>(new DefaultListModel<CheckBox>(){{
			for (ModCandidate candidate : candidates) {
				if (!candidate.isBuiltin()) addElement(new CheckBox(candidate, true));
			}
		}});
		add(new JScrollPane(list) {{setBounds(50, 25, 350, 200);}});
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
	public String toString() { return "ListPanelModules"; }
	
	public static class CheckBox extends JCheckBox {
		public final ModCandidate candidate;
		
		public CheckBox(ModCandidate candidate, boolean enabled) {
			super(candidate.getId() + "  " + candidate.getVersion().getFriendlyString());
			this.candidate = candidate;
			setSelected(enabled);
		}
	}
}
