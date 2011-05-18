package com.raphfrk.protocol;

public class Packet46Bed extends Packet {

	public Packet46Bed(int state) {
		super(2);
		super.writeByte((byte)0x46);
		super.writeByte((byte)state);
	}
	
}
