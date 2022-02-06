package com.kkllffaa.fabricmodselector;

import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;

public class ModJCheckBox extends JCheckBox {
	private final ModCandidate candidate;
	ModJCheckBox(ModCandidate candidate) {
		super(candidate.getId());
		this.candidate = candidate;
	}
	public ModCandidate getCandidate() {
		return candidate;
	}
}