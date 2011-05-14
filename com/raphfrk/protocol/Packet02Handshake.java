package com.raphfrk.protocol;

public class Packet02Handshake extends Packet{

	public Packet02Handshake(Packet packet) {
		super(packet, 2);
	}
	
	public Packet02Handshake(String username) {
		super(username.length()*2 + 3, (byte)2);
		super.writeByte((byte)0x2);
		super.writeString16(username);
	}
	
	public String getUsername() {
		return getString16(1);
	}
	
}
