package com.raphfrk.craftproxyliter;

public class ReconnectCache {

	static MyPropertiesFile pf = null;

	static void init(String filename) {

		if( pf == null ) {
			pf = new MyPropertiesFile(filename);
			pf.load();
		}

	}

	static synchronized void store(String player, String hostname) {
		if(pf==null) return;
		pf.setString(player,hostname);

	}
	
	static synchronized boolean isSet() {
		return pf != null;
	}

	static synchronized String get(String player) {
		if(pf==null) return "";
		return pf.getString(player, "");
	}

	static synchronized void save() {
		if(pf==null) return;
		pf.save();
	}

	static synchronized void remove(String player) {
		if(pf==null) return;
		pf.removeRecord(player);
	}


}
