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
	
	public int getMode() {
		return getInt(13 + getString16Length(5));
	}
	
	public byte getDimension() {
		return getByte(15 + getString16Length(5));
	}
	
	public byte getUnknown() {
		return getByte(16 + getString16Length(5));
	}
	
	public byte getHeight() {
		return getByte(17 + getString16Length(5));
	}
	
	public byte getMaxPlayers() {
		return getByte(18 + getString16Length(5));
	}
	
	

}
