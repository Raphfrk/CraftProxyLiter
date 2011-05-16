package com.raphfrk.craftproxyliter;

import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;

import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.ProtocolInputStream;
import com.raphfrk.protocol.ProtocolOutputStream;

public class DownstreamBridge extends KillableThread {

	final ProtocolInputStream in;
	final ProtocolOutputStream out;
	final PassthroughConnection ptc;
	final FairnessManager fm;

	DownstreamBridge(ProtocolInputStream in, ProtocolOutputStream out, PassthroughConnection ptc, FairnessManager fm) {

		this.in = in;
		this.out = out;
		this.ptc = ptc;
		this.fm = fm;
		this.setName("Downstream Bridge");

	}

	LinkedList<Byte> oldPacketIds = new LinkedList<Byte>();

	public void run() {

		CompressionManager cm = new CompressionManager(this, ptc, fm, out);

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
				System.out.println("IO ERROR");
				kill();
				continue;
			} catch (IllegalStateException ise) {
				kill();
				ptc.printLogMessage(packetBackup + " Unable to read packet");
				ptc.printLogMessage("Packets: " + oldPacketIds);
				continue;
			}

			if(packet.start < packet.end) {

				boolean dontSend = false;

				int packetId = packet.getByte(0) & 0xFF;
				if(((packetId >= 0x32 && packetId < 0x36) || packetId == 0x82) || (packetId == 0x51)) {

					cm.addToQueue(packet);

					dontSend = true;
				}

				oldPacketIds.add(packet.buffer[packet.start & packet.mask]);
				if(this.oldPacketIds.size() > 20) {
					oldPacketIds.remove();
				}

				if(!dontSend) {
					ptc.connectionInfo.uploaded.addAndGet(packet.end - packet.start);
					fm.addPacketToHighQueue(out, packet, this);
					/*try {
						out.sendPacket(packet);
					} catch (IOException e) {
						kill();
						continue;
					}*/
				}

			}

		}

		try {
			out.flush();
		} catch (IOException e) {
			ptc.printLogMessage("Unable to flush output stream");
		}

		System.out.println("About to kill compression manager");
		cm.killTimerAndJoin();

	}

}
