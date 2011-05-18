package com.raphfrk.protocol;

public class Packet10Holding extends Packet {
	
	public Packet10Holding(int slot) {
		super(3);
		super.writeByte((byte)0x10);
		super.writeShort((short)slot);
	}

}
