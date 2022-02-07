package com.kkllffaa.fabricmodselector;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

public class Update {
	public static final String repo = "kkllffaa.fabric-mod-selector.releases:download:";
	public static final String git = "https://github.com/";
	//public static final String git = "http://localhost/";
	public final File mcjson;
	public final Version installed, latest;
	
	private Update(File mcjson, int numbersup) {
		this.mcjson = mcjson;
		installed = getversion();
		latest = getlatest(installed, numbersup);
	}
	public static Update create(File mcjson, int numbersup) {
		if (mcjson == null || !mcjson.exists() || !mcjson.canRead()) return null;
		Update update = new Update(mcjson, numbersup);
		return update.installed != null && update.latest != null ? update : null;
	}
	
	public Version getlatest(Version startfrom, int numbersup) {
		Version v = new Version(startfrom);
		for (int i = v.minor; i < v.minor + numbersup; i++) {
			Version temp = new Version(v.major, i);
			versionstatus k = versionexist(temp);
			if (k == versionstatus.EXIST) {
				v = temp;
			}else if (k == versionstatus.NOTEXIST) break;
			else return null;
		}
		return v;
	}
	public static versionstatus versionexist(Version version) {
		try {
			HttpURLConnection h = (HttpURLConnection) repotourl(repo, version).openConnection();
			int code = h.getResponseCode();
			if (code == 200) return versionstatus.EXIST;
			else if (code == 404) return versionstatus.NOTEXIST;
			else return versionstatus.ERROR;
		} catch (IOException | ClassCastException e) {
			JOptionPane.showMessageDialog(null, e);
			return versionstatus.ERROR;
		}
	}
	public boolean updatefile(Version version) {
		try {
			//String input = Files.readString(mcjson.toPath());
			String input = new String(Files.readAllBytes(mcjson.toPath()));
			String[] splitted = input.split("\"");
			String output = "";
			
			for (String s : splitted) {
				if (s.startsWith(repo)) {
					output = input.replaceFirst(s, repo+version.toString());
				}
			}
			
			if (!output.isEmpty()) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(mcjson));
				writer.write(output);
				writer.close();
				return true;
			}else {
				return false;
			}
			
		} catch (IOException e) {
			return false;
		}
		
	}
	public Version getversion() {
		try {
			//String input = Files.readString(mcjson.toPath());
			String input = new String(Files.readAllBytes(mcjson.toPath()));
			String[] splitted = input.split("\"");
			
			for (String s : splitted) {
				if (s.startsWith(repo)) {
					return new Version(s.substring(repo.length()));
				}
			}
			return null;
		} catch (IOException | NumberFormatException | IndexOutOfBoundsException e) {
			return null;
		}
	}
	public static URL repotourl(String repo, Version version) throws MalformedURLException {
		return new URL(git + repo.replace('.', '/').replace(':', '/')
				+ version + "/download-" + version + ".jar");
		
	}
	
	private enum versionstatus {
		EXIST,
		NOTEXIST,
		ERROR
	}
	
	
	public static class Version {
		public final int major, minor;
		public Version(int major, int minor) {
			this.major = major;
			this.minor = minor;
		}
		public Version(String version) throws NumberFormatException, IndexOutOfBoundsException {
			String[] v = version.split("\\.");
			major = Integer.parseInt(v[0]);
			minor = Integer.parseInt(v[1]);
		}
		public Version(Version version) {
			major = version.major;
			minor = version.minor;
		}
		
		@Override
		public String toString() {
			return major + "." + minor;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Version version = (Version) o;
			return major == version.major && minor == version.minor;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(major, minor);
		}
	}
}
