package com.raphfrk.craftproxyliter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.raphfrk.compression.Compressor;
import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.ProtocolOutputStream;

public class CompressionManager {

	private final FairnessManager fm;
	private final CompressionThread ct;
	private final ProtocolOutputStream out;
	private final PassthroughConnection ptc;
	private final KillableThread t;
	
	private final Object compSync = new Object();
	
	CompressionManager(KillableThread t, PassthroughConnection ptc, FairnessManager fm, ProtocolOutputStream out) {
		this.ptc = ptc;
		this.fm = fm;
		this.out = out;
		this.t = t;
		ct = new CompressionThread();
		ct.start();
	}
	
	public void killTimerAndJoin() {
		ct.interrupt();
		synchronized(compSync) {
			compSync.notifyAll();
		}
		try {
			ct.join();
		} catch (InterruptedException e) {
			System.out.println("Fairness Manager Interrupted when waiting for timer to close");
			Thread.currentThread().interrupt();
		}
	}
	
	public void addToQueue(Packet p) {
		queue.add(p);
		compSync.notifyAll();
	}
	
	ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<Packet>();
	
	private class CompressionThread extends KillableThread {

		public void run() {
			
			ConcurrentHashMap<Long,Boolean> hashes = ptc.connectionInfo.hashesReceived;
			Compressor c = new Compressor(hashes, fm, ptc.proxyListener.hs);
			
			while(!killed()) {
				
				Packet p = queue.poll();

				if(p != null) {
					int packetId = p.getByte(0) & 0xFF;
					if(packetId == 0x33) {
						if(!hashes.isEmpty()) {
							Packet compressed = c.compress(p);
							fm.addPacketToLowQueue(out, compressed, t);
						} else {
							fm.addPacketToLowQueue(out, p, t);
						}
					} else if (packetId == 0x51) {
						Packet decompressed = c.decompress(p, ptc);
						if(decompressed == null) {
							ptc.printLogMessage("Unable to decompress cached packet");
							ptc.interrupt();
						} else {
							ptc.printLogMessage("Decompressed packet successfully");
							fm.addPacketToHighQueue(out, decompressed, t);
						}
					} else {
						fm.addPacketToLowQueue(out, p, t);
					}
				}
				
				synchronized(compSync) {
					if(!queue.isEmpty()) {
						continue;
					}
					try {
						compSync.wait(1000);
					} catch (InterruptedException e) {
						kill();
						continue;
					}
				}
			}
		}
	}
	
}
