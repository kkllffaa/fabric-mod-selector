package com.kkllffaa.fabricmodselector.listpanels;

import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

public abstract class ListPanel extends JPanel {
	public ListPanel() {
		setVisible(false);
		setLayout(null);
	}
	
	@Override public abstract String toString();
	
	public abstract List<ModCandidate> getToLoadList();
	
	public void apply(Collection<ModCandidate> list) {
		List<ModCandidate> candidateList = getToLoadList();
		list.removeIf(candidate -> !candidate.isBuiltin() && !candidateList.contains(candidate));
	}
}
