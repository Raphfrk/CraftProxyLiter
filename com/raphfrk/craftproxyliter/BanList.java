package com.raphfrk.craftproxyliter;

import java.util.HashSet;

public class BanList {
	
	static HashSet<String> names = null;
	
	static void init(String filename) {

		names = MiscUtils.fileToSet(filename, true);

	}
	
	static boolean banned(String name) {
		if(names==null) {
			return false;
		}
		return names.contains(name.toLowerCase().trim());
	}

}
