package com.raphfrk.protocol;

public class Packet01Login extends Packet {
	
	public Packet01Login(Packet packet) {
		super(packet, 1);
	}
	
	public int getVersion() {
		return getInt(1);
	}
	
	public String getUsername() {
		return getString16(5);
	}
	
	public long getSeed() {
		return getLong(5 + getString16Length(5));
	}
	
	public byte getDimension() {
		return getByte(13 + getString16Length(5));
	}

}
