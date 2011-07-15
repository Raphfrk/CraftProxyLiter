package com.raphfrk.protocol;

public class Packet03Chat extends Packet {
	String message;
	
	public Packet03Chat(Packet packet) {
		super(packet, 0x03);
	}
	
	public Packet03Chat(String message) {
		super(message.length()*2 + 2);
		super.writeByte((byte)0x03);
		super.writeString16(message);
	}

}
