package com.kkllffaa.fabricmodselector.listpanels;

import com.kkllffaa.fabricmodselector.Filter;
import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public abstract class ListPanel extends JPanel {
	public ListPanel() {
		setVisible(false);
		setLayout(null);
	}
	
	@Override public abstract String toString();
	public abstract List<ModCandidate> getToLoadList();
	public abstract void addMods(List<ModCandidate> candidates);
	
	public void apply(Collection<ModCandidate> list) {
		List<ModCandidate> candidateList = getToLoadList();
		list.removeIf(candidate -> Filter.useMod(candidate) && !candidateList.contains(candidate));
	}
}
