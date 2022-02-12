package com.kkllffaa.fabricmodselector.listpanels;

import javax.swing.*;

public abstract class ModJCheckBox extends JCheckBox {
	public final boolean newmod;
	
	public ModJCheckBox(String name, boolean startenabled, boolean newmod) {
		super(name);
		this.newmod = newmod;
		setSelected(startenabled);
	}
}
