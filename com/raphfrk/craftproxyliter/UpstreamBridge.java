package com.raphfrk.craftproxyliter;

import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.raphfrk.protocol.EntityMap;
import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.ProtocolInputStream;
import com.raphfrk.protocol.ProtocolOutputStream;

public class UpstreamBridge extends KillableThread {

	final ProtocolInputStream in;
	final ProtocolOutputStream out;
	final PassthroughConnection ptc;
	final FairnessManager fm;


	UpstreamBridge(ProtocolInputStream in, ProtocolOutputStream out, PassthroughConnection ptc, FairnessManager fm) {

		this.in = in;
		this.out = out;
		this.ptc = ptc;
		this.fm = fm;

		this.setName("Upstream Bridge");
	}

	LinkedList<Byte> oldPacketIds = new LinkedList<Byte>();

	public void run() {

		Packet hashPacket = new Packet();
		hashPacket.buffer = new byte[2049*8];
		hashPacket.mask = 0xFFFFFFFF;
		hashPacket.start = 0;

		Packet packet = new Packet();
		Packet packetBackup = packet;
		long[] hashStore = new long[2048];

		boolean blankSent = !Globals.localCache();

		int timeoutCounter = 0;

		while(!killed()) {
			
			if((!blankSent) || (!ptc.connectionInfo.hashesToSend.isEmpty())) {

				Long hash;

				ConcurrentLinkedQueue<Long> queue = ptc.connectionInfo.hashesToSend;
				ConcurrentHashMap<Long,Boolean> sent = ptc.connectionInfo.hashesSent;

				while(!killed() && (!queue.isEmpty() || !blankSent)) {
					int size = 0;
					hashPacket.end = 0;
					hashPacket.writeByte((byte)0x50);
					while(size < 2047 && (hash = queue.poll()) != null) {
						if(!sent.containsKey(hash)) {
							hashStore[size++] = hash;
							sent.put(hash, true);
						}
					}
					//System.out.println("Sending " + size + " hashes");
					hashPacket.writeShort((short)(size * 8));
					for(int cnt=0;cnt<size;cnt++) {
						//System.out.println("Sending " + Long.toHexString(hashStore[cnt]));
						hashPacket.writeLong(hashStore[cnt]);
					}
					ptc.connectionInfo.saved.addAndGet(-(size*8 + 3));
					fm.addPacketToHighQueue(out, hashPacket, this);

					if(!blankSent) {
						//ptc.printLogMessage("Sent blank hash packet");
						blankSent = true;
					}

				}

			}

			if(killed()) {
				continue;
			}

			try {
				packet = in.getPacket(packet, 100);
				if(packet == null) {
					if((timeoutCounter++) > 600) {
						ptc.printLogMessage("Timeout");
						kill();
						continue;
					} else {
						continue;
					}
				}
			} catch (EOFException e) {
				ptc.printLogMessage("EOF reached");
				kill();
				continue;
			} catch (IOException e) {
				System.out.println("IO ERROR");
				kill();
				continue;
			}catch (IllegalStateException ise) {
				kill();
				ptc.printLogMessage(packetBackup + " Unable to read packet");
				ptc.printLogMessage("Packets: " + oldPacketIds);
				continue;
			}

			timeoutCounter = 0;
			
			if(packet.start < packet.end) {

				boolean dontSend = false;
				if(packet.getByte(0) == 0x50) {

					if(!ptc.connectionInfo.cacheInUse.getAndSet(true)) {
						ptc.printLogMessage("Client requested caching mode");
					}

					int pos = 1;
					long hash;
					ConcurrentHashMap<Long,Boolean> hashes = ptc.connectionInfo.hashesReceived;

					short size = packet.getShort(pos);
					pos += 2;

					for(int cnt=0;cnt<(size/8);cnt++) {
						hash = packet.getLong(pos);
						//System.out.println("received hash: " + Long.toHexString(hash));
						hashes.put(hash,true);
						pos+=8;
					}
					dontSend = true;
				}

				oldPacketIds.add(packet.buffer[packet.start & packet.mask]);
				if(this.oldPacketIds.size() > 20) {
					oldPacketIds.remove();
				}

				if(!dontSend) {
					int packetId = packet.getByte(0) & 0xFF;
					
					if(packetId == 0x10) {
						ptc.connectionInfo.holding = packet.getShort(1);
					}
					
					// Map entity Ids
					int clientPlayerId = ptc.connectionInfo.clientPlayerId;
					int serverPlayerId = ptc.connectionInfo.serverPlayerId;

					if(clientPlayerId != serverPlayerId) {
						int[] entityIdArray = EntityMap.entityIds[packetId];
						if(entityIdArray != null) {
							for(int pos : entityIdArray) {
								int id = packet.getInt(pos);
								if(id == clientPlayerId) {
									packet.setInt(pos, serverPlayerId);
								} else if(id == serverPlayerId) {
									packet.setInt(pos, clientPlayerId);
								}
							}
						}
					}
					fm.addPacketToHighQueue(out, packet, this);
				} 

			}
		}

		try {
			out.flush();
		} catch (IOException e) {
			ptc.printLogMessage("Unable to flush output stream");
		}

	}

}
