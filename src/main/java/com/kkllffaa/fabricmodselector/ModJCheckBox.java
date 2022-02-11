package com.kkllffaa.fabricmodselector;

import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.util.ArrayList;

public class ModJCheckBox extends JCheckBox {
	public final ArrayList<ModCandidate> candidates;
	private ModCandidate selected;
	public final String id;
	public ModJCheckBox(ModCandidate candidate) {
		super(candidate.getId());
		this.candidates = new ArrayList<>();
		id = candidate.getId();
		candidates.add(candidate);
		selected = candidate;
	}
	public ModJCheckBox(ModCandidate candidate, boolean enabled) {
		this(candidate);
		setSelected(enabled);
	}
	public void add(ModCandidate candidate) {
		if (id.equals(selected.getId()) && !candidates.contains(candidate)) {
			candidates.add(candidate);
		}
	}
	public void select(int i) {
		if (i >= 0 && i < candidates.size()) {
			selected = candidates.get(i);
		}
	}
	public boolean select(ModCandidate mod) {
		if (selected == mod) return true;
		for (int i = 0; i < candidates.size(); i++) {
			if (candidates.get(i) == mod) {
				select(i);
				return true;
			}
		}
		return false;
	}
	public int getIndex() {
		for (int i = 0; i < candidates.size(); i++) {
			if (selected == candidates.get(i)) {
				return i;
			}
		}
		return -1;
	}
	public ModCandidate getSelected() {
		return selected;
	}
}