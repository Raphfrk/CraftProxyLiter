package com.raphfrk.craftproxyliter;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.ProtocolOutputStream;

public class FairnessManager {

	final static int largeSize = 16384;
	final static int smallSize = 128;

	ConcurrentLinkedQueue<Reference<byte[]>> largeBufferPool = new ConcurrentLinkedQueue<Reference<byte[]>>();
	ConcurrentLinkedQueue<Reference<byte[]>> smallBufferPool = new ConcurrentLinkedQueue<Reference<byte[]>>();

	AtomicInteger bufCalls = new AtomicInteger(0);

	public byte[] getBuffer(int size) {
		//System.out.println("Buffer call: (" + size + ") " + bufCalls.incrementAndGet());
		/*if(size <= smallSize) {
			return getSmallBuffer();
		} else if(size <= largeSize) {
			return getLargeBuffer();
		} else {*/
		return new byte[size];
		//}
	}

	private byte[] getLargeBuffer() {
		Reference<byte[]> ref = largeBufferPool.poll();
		if(ref == null) {
			return new byte[largeSize];
		} else {
			byte[] b = ref.get();
			if(b==null) {
				return new byte[largeSize];
			}
			return b;
		}
	}

	public void returnLargeBuffer(byte[] buf) {
		if(buf.length == largeSize) {
			largeBufferPool.add(new SoftReference<byte[]>(buf));
		} else {
			throw new IllegalStateException("returned large buffer is wrong size");
		}
	}

	private byte[] getSmallBuffer() {
		Reference<byte[]> ref = smallBufferPool.poll();
		if(ref == null) {
			return new byte[smallSize];
		} else {
			byte[] b = ref.get();
			if(b==null) {
				return new byte[smallSize];
			}
			return b;
		}
	}

	public void returnSmallBuffer(byte[] buf) {
		if(buf.length == smallSize) {
			largeBufferPool.add(new SoftReference<byte[]>(buf));
		} else {
			throw new IllegalStateException("returned small buffer is wrong size");
		}
	}

	private ConcurrentLinkedQueue<FairnessEntry> lowQueue = new ConcurrentLinkedQueue<FairnessEntry>();
	private ConcurrentLinkedQueue<FairnessEntry> highQueue = new ConcurrentLinkedQueue<FairnessEntry>();

	final private Object outSync = new Object();

	boolean addPacketToLowQueue(ProtocolOutputStream pout, Packet p, KillableThread t) {
		boolean r;
		synchronized(outSync) {
			r = lowQueue.add(new FairnessEntry(pout, p.clone(this), t));
			outSync.notifyAll();
		}
		return r;
	}

	boolean addPacketToHighQueue(ProtocolOutputStream pout, Packet p, KillableThread t) {
		boolean r;
		synchronized(outSync) {
			r = highQueue.add(new FairnessEntry(pout, p.clone(this), t));
			outSync.notifyAll();
		}
		return r;
	}

	private final OutputManager outputManager;

	FairnessManager() {
		outputManager = new OutputManager();
		outputManager.start();
		outputManager.setName("Output Manager");
	}

	public void killTimerAndJoin() {
		while(outputManager.isAlive()) {
			synchronized(outSync) {
				System.out.println("Interrupting output manager");
				outputManager.interrupt();
				outSync.notifyAll();
			}
			System.out.println("About to join against output manager");
			try {
				outputManager.join();
			} catch (InterruptedException e) {
				System.out.println("Fairness Manager Interrupted when waiting for timer to close");
				Thread.currentThread().interrupt();
			}
		}
	}

	private class OutputManager extends KillableThread {

		int offset = 0;;

		public void run() {

			while(!killed()) {

				synchronized(outSync) {
					if(highQueue.isEmpty() && lowQueue.isEmpty()) {
						offset = 0;
						try {
							outSync.wait(100);
						} catch (InterruptedException e) {
							kill();
							continue;
						}
					}
				}

				if(killed()) {
					continue;
				}

				FairnessEntry lowEntry = lowQueue.peek();
				FairnessEntry highEntry = highQueue.peek();

				boolean highQueueHasPriority = true;

				highQueueHasPriority = 
					(lowEntry == null) || 
					(offset >= 0) ||
					(highEntry != null && highEntry.timestamp <= (lowEntry.timestamp + 5));

				//System.out.println("High Queue : " + highEntry);
				//System.out.println("Low  Queue : " + lowEntry);
				//System.out.println("High priority: " + highQueueHasPriority);


				if(highQueueHasPriority) {
					highEntry = highQueue.poll();
					if(highEntry != null) {
						try {
							highEntry.send();
							offset -= highEntry.packet.end - highEntry.packet.start;
						} catch (IOException e) {
							highEntry.bridge.interrupt();
						}
						continue;
					}
					lowEntry = lowQueue.poll();
					if(lowEntry != null) {
						try {
							lowEntry.send();
							offset += lowEntry.packet.end - lowEntry.packet.start;
						} catch (IOException e) {
							lowEntry.bridge.interrupt();
						}
						continue;
					}
				} else {
					lowEntry = lowQueue.poll();
					if(lowEntry != null) {
						try {
							lowEntry.send();
							offset += lowEntry.packet.end - lowEntry.packet.start;
						} catch (IOException e) {
							lowEntry.bridge.interrupt();
						}
						continue;
					}
					highEntry = highQueue.poll();
					if(highEntry != null) {
						try {
							highEntry.send();
							offset -= highEntry.packet.end - highEntry.packet.start;
						} catch (IOException e) {
							highEntry.bridge.interrupt();
						}
						continue;
					}
				}
			}
		}
	}
}
