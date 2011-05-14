package com.raphfrk.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;


public class ProtocolInputStream {

	private final InputStream in;
	
	final private byte[] buffer;
	final private int bufferMask;
	final private int bufferLength;
	
	private int start;
	private int length;
	
	private int timeout = 60000;
	
	
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
		
		if(packet == null) {
			packet = new Packet();
			packet.buffer = buffer;
			packet.mask = bufferMask;
		}
		
		Packet ret = null;
		
		long startTime = System.currentTimeMillis();
		
		boolean interrupted = false;
		
		long currentTime = 0;
		
		while(ret == null && length < bufferLength && startTime + timeout > currentTime && !Thread.currentThread().isInterrupted()) {
			ret = PacketScan.packetScan(buffer, start, length, bufferMask, packet);
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
					actual = in.read(buffer, endMod, available);
					if(actual < 100) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
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
					}
				}
			}
		}
		
		if(interrupted) {
			Thread.currentThread().interrupt();
		}
		
		if(ret == null) {
			return null;
		}
		
		int size = ret.end - ret.start;
		start += size;
		length -= size;
	
		return ret;
		
	}


}
