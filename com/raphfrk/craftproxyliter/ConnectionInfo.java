package com.raphfrk.craftproxyliter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionInfo {
	
	public ConcurrentLinkedQueue<Long> hashesToSend = new ConcurrentLinkedQueue<Long>();
	public AtomicInteger saved = new AtomicInteger();
	public AtomicInteger uploaded = new AtomicInteger();
	public ConcurrentHashMap<Long,Boolean> hashesSent = new ConcurrentHashMap<Long,Boolean>();
	public ConcurrentHashMap<Long,Boolean> hashesReceived = new ConcurrentHashMap<Long,Boolean>();
	public Set<Long> activeChunks = Collections.synchronizedSet(new LinkedHashSet<Long>());
	public Set<Integer> activeEntities = Collections.synchronizedSet(new LinkedHashSet<Integer>());
	
	public int clientPlayerId = 0;
	public int serverPlayerId = 0;
	
	public int holding = 0;
	
	public AtomicBoolean cacheInUse = new AtomicBoolean(false);
	
	public boolean redirect = false;
	
	public int clientVersion = 0;
	
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
	
	public boolean addChunk(int x, int z) {
		
		long temp1 = z & 0x00000000FFFFFFFFL;
		long temp2 = ((long)x) << 32L;
		long temp = temp1 | temp2;
		
		return activeChunks.add(temp);
	}
	
	public boolean removeChunk(int x, int z) {
		
		long temp1 = z & 0x00000000FFFFFFFFL;
		long temp2 = ((long)x) << 32L;
		long temp = temp1 | temp2;
		
		return activeChunks.remove(temp);
	}
	
	public boolean containsChunk(int x, int z) {
		long temp1 = z & 0x00000000FFFFFFFFL;
		long temp2 = ((long)x) << 32L;
		long temp = temp1 | temp2;
		
		return activeChunks.contains(temp);
	}
	
	public List<Long> clearChunks() {
		
		List<Long> temp = new ArrayList<Long>(activeChunks.size());
		synchronized(activeChunks) {
			for(Long current : activeChunks) {
				temp.add(current);
			}
		}
		activeChunks.clear();
		return temp;	
	}
	
	public List<Integer> clearEntities() {
		List<Integer> temp = new ArrayList<Integer>(activeEntities.size());
		synchronized(activeEntities) {
			for(Integer current : activeEntities) {
				temp.add(current);
			}
		}
		activeEntities.clear();
		return temp;	
	}
	
	static public int getX(long key) {
		return (int)(key >> 32L); 
	}
	
	static public int getZ(long key) {
		return (int)key;
	}
	
}
