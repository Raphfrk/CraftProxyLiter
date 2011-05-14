package com.raphfrk.craftproxyliter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionInfo {
	
	public ConcurrentLinkedQueue<Long> hashesToSend = new ConcurrentLinkedQueue<Long>();
	public AtomicInteger saved = new AtomicInteger();
	public ConcurrentHashMap<Long,Boolean> hashesSent = new ConcurrentHashMap<Long,Boolean>();
	public ConcurrentHashMap<Long,Boolean> hashesReceived = new ConcurrentHashMap<Long,Boolean>();
	
	public AtomicBoolean cacheInUse = new AtomicBoolean(false);
	
	private String username;
	
	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
	
	String IP;
	
	public String getIP() {
		return IP;
	}
	
	public void setIP(String IP) {
		this.IP = IP;
	}
	
	int port;
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	private String hostname;
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}
	
}
