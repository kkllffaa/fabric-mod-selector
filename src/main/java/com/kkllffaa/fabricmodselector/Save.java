package com.kkllffaa.fabricmodselector;

import net.fabricmc.loader.impl.discovery.ModCandidate;

import javax.swing.*;
import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Save {
	public static File getmcjarlocation() {
		try {
			String path = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("net/minecraft/client/main/Main.class")).getPath();
			path = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
			path = URLDecoder.decode(path, "UTF-8");
			return new File(path.substring(1));
		} catch (Exception ignored) {
			return null;
		}
	}
	
	public static class ModListHolder {
		public final List<ModHolder> mods;
		
		public ModListHolder(ListModel<ModJCheckBox> modlist) {
			mods = new ArrayList<>();
			
			
			for (int i = 0; i < modlist.getSize(); i++) {
				
				mods.add(new ModHolder(modlist.getElementAt(i).getSelected(), modlist.getElementAt(i).isSelected()));
			}
		}
		
		public void apply(ListModel<ModJCheckBox> modlist) {
			for (int i = 0; i < modlist.getSize(); i++) {
				for (ModHolder modHolder : mods) {
					if (modHolder.match(modlist.getElementAt(i).getSelected()))
						modlist.getElementAt(i).setSelected(modHolder.enabled);
				}
			}
		}
		
		
		
		public static class ModHolder {
			public final String name, version;
			public final boolean enabled;
			public ModHolder(ModCandidate mod, boolean enabled) {
				this.enabled = enabled;
				this.name = mod.getId();
				this.version = mod.getVersion().getFriendlyString();
			}
			public boolean match(ModCandidate mod) {
				return mod.getId().equals(name) && mod.getVersion().getFriendlyString().equals(version);
			}
		}
	}
}
