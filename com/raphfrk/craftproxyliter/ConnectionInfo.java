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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.raphfrk.protocol.Packet;

public class ConnectionInfo {
	
	public ConcurrentLinkedQueue<Long> hashesToSend = new ConcurrentLinkedQueue<Long>();
	public AtomicInteger saved = new AtomicInteger();
	public AtomicInteger uploaded = new AtomicInteger();
	public ConcurrentHashMap<Long,Boolean> hashesSent = new ConcurrentHashMap<Long,Boolean>();
	public ConcurrentHashMap<Long,Boolean> hashesReceived = new ConcurrentHashMap<Long,Boolean>();
	public Set<Long> activeChunks = Collections.synchronizedSet(new LinkedHashSet<Long>());
	public Set<Integer> activeEntities = Collections.synchronizedSet(new LinkedHashSet<Integer>());
	
	public List<Packet> loginCustomPackets = new ArrayList<Packet>();
	
	public int clientPlayerId = 0;
	public int serverPlayerId = 0;
	
	public int holding = 0;
	
	public AtomicBoolean cacheInUse = new AtomicBoolean(false);
	
	public boolean redirect = false;
	
	public int clientVersion = 0;
	public boolean craftProxyLogin = false;
	
	public boolean forwardConnection = false;
	
	public int loginDimension = 0;
	public byte loginUnknownRespawn = 0;
	public byte loginCreative = 0;
	public short loginHeight = 128;
	public long loginSeed = 0;
	public String levelType = "DEFAULT";
	
	private String username;
	private String usernameRaw;
	
	public String toString() {
		return "clientplayerId=" + clientPlayerId + ", serverPlayerId=" + serverPlayerId + ", holding=" + holding + ", cacheInUse=" + 
				cacheInUse.get() + ", clientVersion=" + clientVersion + ", craftProxyLogin=" + craftProxyLogin + ", forwardConnection=" + 
				forwardConnection + ", loginDimension=" + loginDimension + ", loginCreative=" + loginCreative + ", loginHeight=" + 
				loginHeight + ", loginSeed=" + loginSeed + ", levelType=" + levelType;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	
	public void setUsernameRaw(String usernameRaw) {
		this.usernameRaw = usernameRaw;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getUsernameRaw() {
		return usernameRaw;
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
	
	private String hostname = null;
	
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
