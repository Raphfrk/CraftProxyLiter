package com.raphfrk.protocol;

public class Packet09Respawn extends Packet {
	
	public Packet09Respawn(Packet packet) {
		super(packet, 9);
	}

	public Packet09Respawn(byte dimension) {
		super(2);
		super.writeByte((byte)0x09);
		super.writeByte(dimension);
	}
	
	public byte getDimension() {
		return getByte(1);
	}
	
}