package com.kkllffaa.fabricmodselector;

import net.fabricmc.loader.impl.discovery.ModCandidate;

public class ModComboboxVersionContainer {
	private final ModCandidate mod;
	ModComboboxVersionContainer(ModCandidate mod) {
		this.mod = mod;
	}
	ModCandidate getMod() {
		return mod;
	}
	
	@Override
	public String toString() {
		return mod.getVersion().getFriendlyString();
	}
}
