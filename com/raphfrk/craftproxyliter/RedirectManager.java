package com.raphfrk.craftproxyliter;

public class RedirectManager {
	
	public static Integer getPort(String fullHostname) {
		
		int pos = fullHostname.lastIndexOf(":");
		int pos2 = fullHostname.lastIndexOf(",");
		
		int lastMarker = Math.max(pos, pos2);
		
		if(lastMarker == -1) {
			try {
				return Integer.parseInt(fullHostname);
			} catch (NumberFormatException nfe) {
				return null;
			}
		} else {
			try {
				return Integer.parseInt(fullHostname.substring(lastMarker+1));
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
		
	}

	
	public static String removePrefix(String listenHostname, String hostname) {
		
		if(hostname == null || listenHostname == null) {
			return null;
		}
		
		listenHostname = listenHostname.trim();
		hostname = hostname.trim();

		if(hostname.startsWith(listenHostname)) {
			hostname = hostname.substring(listenHostname.length());
			hostname = hostname.trim();
		} else {
			return hostname;
		}
		
		if(hostname.startsWith(",")) {
			hostname = hostname.substring(1);
			hostname = hostname.trim();
		}  else {
			return null;
		}
		
		return hostname;
	}
	
	public static Boolean isNextProxy(String listenHostname, String hostname) {

		hostname = removePrefix(listenHostname, hostname);
		
		if(hostname == null) {
			return null;
		}
		
		return hostname.indexOf(",") >=0;
	}
	
	public static String[] pare(String listenHostname, String hostname) {
		
		if(hostname == null || listenHostname == null) {
			return null;
		}
		hostname = removePrefix(listenHostname, hostname);
		
		if(hostname == null) {
			return null;
		}
	
		String[] split = hostname.split(",");
		
		return split;
	}
	
	public static String getNextHostname(String listenHostname, String hostname) {
		
		String[] split = pare(listenHostname, hostname);
		
		if(split == null) {
			return null;
		}
		
		String[] split2 = split[0].split(":");
		
		if(split2.length<=1) {
			return "localhost";
		} else if(split2.length == 2) {
			return split2[0];
		} else {
			return null;
		}
	
	}
	
	public static Integer getNextPort(String listenHostname, String hostname) {
		
		String[] split = pare(listenHostname, hostname);
		
		if(split == null) {
			return null;
		}
		
		String[] split2 = split[0].split(":");
		
		String portnum;
		
		if(split2.length<=1) {
			portnum = split2[0];
		} else if(split2.length == 2) {
			portnum = split2[1];
		} else {
			return null;
		}
		
		try {
			return Integer.parseInt(portnum);
		} catch (NumberFormatException nfe) {
			return null;
		}
	
	}
	
	

}
