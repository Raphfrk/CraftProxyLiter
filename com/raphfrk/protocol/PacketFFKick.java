package com.raphfrk.protocol;

public class PacketFFKick extends Packet {
	
	String message;
	
	public PacketFFKick(Packet packet) {
		super(packet, 0xFF);
	}
	
	public PacketFFKick(String kick) {
		super(kick.length()*2 + 2);
		super.writeByte((byte)0xFF);
		super.writeString16(kick);
	}

}
