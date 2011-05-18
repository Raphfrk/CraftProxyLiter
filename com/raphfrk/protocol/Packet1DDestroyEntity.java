package com.raphfrk.protocol;

public class Packet1DDestroyEntity extends Packet {

	public Packet1DDestroyEntity(int entityId) {
		super(5);
		super.writeByte((byte)0x1D);
		super.writeInt(entityId);
	}
	
}
