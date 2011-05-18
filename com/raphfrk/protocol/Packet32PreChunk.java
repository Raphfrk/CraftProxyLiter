package com.raphfrk.protocol;

public class Packet32PreChunk extends Packet {

	public Packet32PreChunk(int x, int z, boolean mode) {
		super(10);
		super.writeByte((byte)0x32);
		super.writeInt(x);
		super.writeInt(z);
		super.writeByte((byte)(mode?1:0));
	}
	
}
