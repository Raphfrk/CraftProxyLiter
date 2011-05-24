package com.raphfrk.craftproxyliter;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

public class BanList {

	static HashSet<String> names = null;

	static boolean whiteList = false;

	static void setWhiteList(boolean whiteList) {
		BanList.whiteList = whiteList;
	}

	static boolean getWhiteList() {
		return whiteList;
	}

	static String filename;
	
	static void init(String filename, boolean whiteList) {
		
		synchronized(lastRead) {
			BanList.whiteList = whiteList;

			lastRead.set(System.currentTimeMillis());

			if(filename != null) {
				BanList.filename = filename;
				names = MiscUtils.fileToSet(filename, true);
			}
		}
	}

	final static AtomicLong lastRead = new AtomicLong(0L);

	static boolean banned(String name) {

		synchronized(lastRead) {

			if(names==null) {
				return false;
			}

			if(lastRead.get() + 30000 <  System.currentTimeMillis()) {

				init(filename, whiteList);

			}

			return whiteList ^ names.contains(name.toLowerCase().trim());
		}
	}

}
