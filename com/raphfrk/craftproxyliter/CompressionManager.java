package com.raphfrk.craftproxyliter;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
	private final Limiter limiter;

	private final Object compSync = new Object();

	CompressionManager(KillableThread t, PassthroughConnection ptc, FairnessManager fm, ProtocolOutputStream out, Limiter limiter) {
		this.ptc = ptc;
		this.fm = fm;
		this.out = out;
		this.t = t;
		this.limiter = limiter;
		ct = new CompressionThread();
		ct.setName("CompressionThread");
		ct.start();
	}

	public void killTimerAndJoin() {
		ct.c.destroyPool();
		ct.interrupt();
		synchronized(compSync) {
			compSync.notifyAll();
		}
		while(ct.isAlive()) {
			try {
				ct.join();
			} catch (InterruptedException e) {
				System.out.println("Compression Manager Interrupted when waiting for timer to close");
				Thread.currentThread().interrupt();
				ct.interrupt();
			}
		}
	}

	public void addToQueue(Packet p) {
		queue.add(p.clone(fm));
		synchronized(compSync) {
			compSync.notifyAll();
		}
	}

	ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<Packet>();

	private class CompressionThread extends KillableThread {

		Compressor c;

		public void run() {

			c = new Compressor(ptc, fm, ptc.proxyListener.hs);
			AtomicBoolean compressing = ptc.connectionInfo.cacheInUse;

			while((!queue.isEmpty()) || (!killed())) {

				Packet p = queue.poll();

				if(p != null && !killed()) {
					int packetId = p.getByte(0) & 0xFF;
					if(packetId == 0x33) {
						if(compressing.get()) {
							Packet compressed = c.compress(p);
							try {
								limiter.pass(compressed.end - compressed.start);
								synchronized(out) {
									out.sendPacket(compressed);
								}
							} catch (IOException ioe) {
								kill();
								continue;
							}
							ptc.connectionInfo.uploaded.addAndGet(compressed.end - compressed.start);
						} else {
							try {
								limiter.pass(p.end - p.start);
								synchronized(out) {
									out.sendPacket(p);
								}
							} catch (IOException ioe) {
								kill();
								continue;
							}
							ptc.connectionInfo.uploaded.addAndGet(p.end - p.start);
						}
					} else if (packetId == 0x51) {
						Packet decompressed = c.decompress(p, ptc);
						if(decompressed == null) {
							ptc.printLogMessage("Unable to decompress cached packet");
							ptc.interrupt();
						} else {
							try {
								limiter.pass(decompressed.end - decompressed.start);
								synchronized(out) {
									out.sendPacket(decompressed);
								}
								ptc.connectionInfo.uploaded.addAndGet(decompressed.end - decompressed.start);
							} catch (IOException ioe) {
								kill();
								continue;
							}
						}
					} else {
						try {
							limiter.pass(p.end - p.start);
							synchronized(out) {
								out.sendPacket(p);
							}
							ptc.connectionInfo.uploaded.addAndGet(p.end - p.start);
						} catch (IOException ioe) {
							kill();
							continue;
						}
					}
				}

				if(!killed()) {
					synchronized(compSync) {
						try {
							if(!queue.isEmpty()) {
								continue;
							}
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

}
