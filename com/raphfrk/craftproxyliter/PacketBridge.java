package com.raphfrk.craftproxyliter;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.ProtocolInputStream;
import com.raphfrk.protocol.ProtocolOutputStream;

public class PacketBridge extends KillableThread {

	final ProtocolInputStream in;
	final ProtocolOutputStream out;
	final PassthroughConnection ptc;
	final FairnessManager fm;
	
	final Limiter limiter;

	PacketBridge(ProtocolInputStream in, ProtocolOutputStream out, PassthroughConnection ptc, FairnessManager fm) {

		this.in = in;
		this.out = out;
		this.ptc = ptc;
		this.fm = fm;
		
		this.limiter = new Limiter(Globals.bandwidthLimit());

	}

	LinkedList<Byte> oldPacketIds = new LinkedList<Byte>();

	public void run() {

		Packet packet = new Packet();
		Packet packetBackup = packet;

		while(!killed()) {

			try {
				packet = in.getPacket(packet);
				if(packet == null) {
					ptc.printLogMessage("Timeout");
					kill();
					continue;
				}
			} catch (EOFException e) {
				ptc.printLogMessage("EOF reached");
				kill();
				continue;
			} catch (IOException e) {
				System.out.println("ERROR");
			}
			if(packet == null) {
				if(!killed()) {
					kill();
					ptc.printLogMessage(packetBackup + " Unable to read packet");
					ptc.printLogMessage("Packets: " + oldPacketIds);
				}
				continue;
			}

			if(packet.start < packet.end) {
				oldPacketIds.add(packet.buffer[packet.start & packet.mask]);
				if(this.oldPacketIds.size() > 20) {
					oldPacketIds.remove();
				}
			}
			
			try {
				out.sendPacket(packet);
			} catch (IOException ioe) {
				kill();
				continue;
			}

		}

		try {
			out.flush();
		} catch (IOException e) {
			ptc.printLogMessage("Unable to flush output stream");
		}

	}

}
