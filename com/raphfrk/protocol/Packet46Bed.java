package com.raphfrk.protocol;

public class Packet46Bed extends Packet {

	public Packet46Bed(int state, int aux) {
		super(3);
		super.writeByte((byte)0x46);
		super.writeByte((byte)state);
		super.writeByte((byte)aux);
	}
	
}
