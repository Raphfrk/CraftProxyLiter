package com.raphfrk.protocol;

public class Packet09Respawn extends Packet {
	
	public Packet09Respawn(Packet packet) {
		super(packet, 9);
	}

	public Packet09Respawn(byte dimension, byte unknown, byte creative, short height, long seed) {
		super(14);
		super.writeByte((byte)0x09);
		super.writeByte(dimension);
		super.writeByte(unknown);
		super.writeByte(creative);
		super.writeShort(height);
		super.writeLong(seed);
	}
	
	public byte getDimension() {
		return getByte(1);
	}
	
	public byte getUnknownField() {
		return getByte(2);
	}
	
	public byte getCreative() {
		return getByte(3);
	}
	
	public short getHeight() {
		return getShort(4);
	}
	
	public long getLong() {
		return getLong(6);
	}
	
}