package com.raphfrk.craftproxyliter;

import java.io.IOException;

import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.ProtocolOutputStream;

public class FairnessEntry {

	final ProtocolOutputStream pout;
	final Packet packet;
	final long timestamp;
	final KillableThread bridge;

	public FairnessEntry(ProtocolOutputStream pout, Packet packet, KillableThread bridge) {
		this.pout = pout;
		this.packet = packet;
		this.timestamp = System.currentTimeMillis();
		this.bridge = bridge;
	}

	public void send() throws IOException {
		long delay = (System.currentTimeMillis() - timestamp);
		if(delay > 20) {
			//System.out.println("Delay through fairness manager: " + delay);
		}
		pout.sendPacket(packet);
	}

	// Low priority ... check chunks  50, 51, 52, 53 and 130 (update sign)

}
