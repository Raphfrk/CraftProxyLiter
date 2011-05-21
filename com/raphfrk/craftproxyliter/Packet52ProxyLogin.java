package com.raphfrk.craftproxyliter;

import com.raphfrk.protocol.Packet;

public class Packet52ProxyLogin extends Packet {

	public Packet52ProxyLogin(Packet packet) {
		super(packet, 0x52);
	}

	public Packet52ProxyLogin(String code, String hostname, String username) {
		super(username.length()*2 + code.length()*2 + hostname.length()*2 + 8);
		super.writeByte((byte)0x52);
		super.writeString16(code);
		super.writeString16(hostname);
		super.writeString16(username);
	}
	
	public String getCode() {
		return getString16(1);
	}
	
	public String getHostname() {
		int codeLength = getShort(1);
		return getString16(3 + codeLength*2);
	}
	
	public String getUsername() {
		int codeLength = getShort(1);
		int hostnameLength = getShort(3 + codeLength*2);
		return getString16(5 + codeLength*2 + hostnameLength*2);
	}

}