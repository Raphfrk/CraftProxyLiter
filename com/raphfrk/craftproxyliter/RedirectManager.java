/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.raphfrk.craftproxyliter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RedirectManager {
	
	public static Integer getListenPort(String fullHostname) {
		
		int pos = fullHostname.lastIndexOf(":");
		int pos2 = fullHostname.lastIndexOf(",");
		
		int lastMarker = Math.max(pos, pos2);
		
		if(lastMarker == -1) {
			try {
				return Integer.parseInt(fullHostname);
			} catch (NumberFormatException nfe) {
				return 25565;
			}
		} else {
			try {
				return Integer.parseInt(fullHostname.substring(lastMarker+1));
			} catch (NumberFormatException nfe) {
				return 25565;
			}
		}
		
	}
	
	public static InetAddress getListenHostname(String fullHostname) {
		
		int pos = fullHostname.lastIndexOf(":");
		int pos2 = fullHostname.lastIndexOf(",");
		
		int lastMarker = Math.max(pos, pos2);
		
		if(lastMarker == -1) {
				return null;
		} else {
			pos2++;
			if(pos == -1) {
				return null;
			} else {
				if(pos2 < pos) {
					String hostname = fullHostname.substring(pos2, pos);
					InetAddress address;
					try {
						address = InetAddress.getByName(hostname);
					} catch (UnknownHostException e) {
						Logging.log("Unknown hostname for listen port: " + hostname);
						return null;
					}
					if(MiscUtils.isThisMyIpAddress(address)) {
						return address;
					} else {
						return null;
					}
				} else {
					return null;
				}
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
			try {
				Integer.parseInt(split2[0]);
				return "localhost";
			} catch (NumberFormatException nfe) {
				return split2[0];
			}
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
			try {
				return Integer.parseInt(split2[0]);
			} catch (NumberFormatException nfe) {
				return 25565;
			}
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
