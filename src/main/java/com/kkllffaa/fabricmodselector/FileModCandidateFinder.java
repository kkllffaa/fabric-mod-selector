package com.kkllffaa.fabricmodselector;

import net.fabricmc.loader.impl.discovery.ModCandidateFinder;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class FileModCandidateFinder implements ModCandidateFinder {
	private final Collection<File> files;
	private final boolean requiresRemap;
	
	public FileModCandidateFinder(Collection<File> files, boolean requiresRemap) {
		this.files = files;
		this.requiresRemap = requiresRemap;
	}
	
	@Override
	public void findCandidates(ModCandidateConsumer out) {
		for (File file : files) {
			if (file.exists() && isValidFile(file.toPath())) {
				out.accept(file.toPath(), requiresRemap);
			}
		}
	}
	
	static boolean isValidFile(Path path) {
		/*
		 * We only propose a file as a possible mod in the following scenarios:
		 * General: Must be a jar file
		 *
		 * Some OSes Generate metadata so consider the following because of OSes:
		 * UNIX: Exclude if file is hidden; this occurs when starting a file name with `.`
		 * MacOS: Exclude hidden + startsWith "." since Mac OS names their metadata files in the form of `.mod.jar`
		 */
		
		if (!Files.isRegularFile(path)) return false;
		
		try {
			if (Files.isHidden(path)) return false;
		} catch (IOException e) {
			Log.warn(LogCategory.DISCOVERY, "Error checking if file %s is hidden", path, e);
			return false;
		}
		
		String fileName = path.getFileName().toString();
		
		return fileName.endsWith(".jar") && !fileName.startsWith(".");
	}
}