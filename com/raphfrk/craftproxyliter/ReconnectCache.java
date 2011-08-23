package com.raphfrk.craftproxyliter;

public class ReconnectCache {

	private static MyPropertiesFile pf = null;
	private static String filename = null;
	private static SaveThread saveThread = null;

	static synchronized void init(String filename) {

		if( pf == null ) {
			ReconnectCache.filename = filename;
			pf = new MyPropertiesFile(filename);
			pf.load();
			if (saveThread != null) {
				saveThread.kill();
				saveThread = null;
			}
			saveThread = new SaveThread();
			saveThread.setPriority(Thread.MIN_PRIORITY);
			saveThread.start();
		}

	}
	
	static void killThread() {
		boolean first = true;
		SaveThread saveThreadLocal = null;
		while (first || saveThreadLocal != null) {
			first = false;
			saveThreadLocal = getAndClearSaveThread();
			if (saveThreadLocal != null) {
				saveThreadLocal.kill();
			}
		}
	}
	
	static private synchronized SaveThread getAndClearSaveThread() {
		SaveThread saveThreadLocal = saveThread;
		saveThread = null;
		return saveThreadLocal;
	}
	
	static synchronized void reload() {
		pf = null;
		init(filename);
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
	
	private static class SaveThread extends Thread {
		
		public void kill() {
			while (isAlive()) {
				interrupt();
				try {
					join(100);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}
		
		public void run() {
			while (!isInterrupted()) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				save();
			}
		}
		
	}


}
