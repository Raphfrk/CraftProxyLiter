package com.raphfrk.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;


public class ProtocolInputStream {

	private final InputStream in;

	final private byte[] buffer;
	final private int bufferMask;
	final private int bufferLength;

	private int start;
	private int length;

	final private int timeout = 60000;


	public ProtocolInputStream(InputStream in, int size) {
		this.in = in;
		int powOf2 = size-1;
		powOf2 |= (powOf2>>1);
		powOf2 |= (powOf2>>2);
		powOf2 |= (powOf2>>4);
		powOf2 |= (powOf2>>8);
		powOf2 |= (powOf2>>16);
		bufferMask = powOf2;
		powOf2++;

		bufferLength = powOf2;
		buffer = new byte[powOf2];
		start = 0;
		length = 0;
	}

	public Packet getPacket(Packet packet) throws IOException {

		return getPacket(packet, timeout);

	}

	public Packet getPacket(Packet packet, int timeout) throws IOException{

		if(packet == null) {
			packet = new Packet();
			packet.buffer = buffer;
			packet.mask = bufferMask;
		}

		Packet ret = null;

		long startTime = System.currentTimeMillis();

		long packetScanTime = 0;

		long sleepTime = 0;

		long readTime = 0;

		boolean interrupted = false;

		long currentTime = 0;
		
		int packetId = -1;

		//System.out.println("Starting new packet read " + startTime);

		while(ret == null && length < bufferLength && startTime + timeout > currentTime && !Thread.currentThread().isInterrupted()) {
			//System.out.println("Length: " + length);
			try {
				packetScanTime -= System.currentTimeMillis();
				ret = PacketScan.packetScan(buffer, start, length, bufferMask, packet);
				packetScanTime += System.currentTimeMillis();
			} catch (IllegalStateException ise) {
				throw ise;
			}

			int readSoFar = 0;
			if(ret == null) {
				int startMod = start & bufferMask;
				int endMod = (start + length) & bufferMask;
				int available;

				if(endMod >= startMod) {
					available = bufferLength - endMod;
				} else {
					available = startMod - endMod;
				}
				int actual = 0;
				try {
					readTime -= System.currentTimeMillis();
					actual = in.read(buffer, endMod, available);
					readTime += System.currentTimeMillis();
					
					if(actual > 0 && packetId == -1) {
						packetId = 0xFF & buffer[startMod];
					}

					readSoFar += actual;
					if(readSoFar < 100) {
						sleepTime -= System.currentTimeMillis();
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						sleepTime += System.currentTimeMillis();
					}

				} catch (SocketTimeoutException ste) {
					continue;
				}
				if(actual == -1) {
					throw new EOFException();
				} else {
					length += actual;
					if(length > buffer.length - 1) {
						System.err.println("Buffer mis-calculation for length??");
						System.out.println("Buffer mis-calculation for length??");
						throw new IOException("Buffer mis-calculation for length??");
					}
				}
			}
		}

		if(interrupted) {
			Thread.currentThread().interrupt();
		}

		//System.out.println("Packet scan time: " + packetScanTime);
		//System.out.println("Read time: " + readTime);
		//System.out.println("Sleep time: " + sleepTime);
		//System.out.println("Time to read: " + (System.currentTimeMillis() - startTime));

		if(ret == null) {
			return null;
		}

		int size = ret.end - ret.start;
		start += size;
		length -= size;

		return ret;

	}


}
