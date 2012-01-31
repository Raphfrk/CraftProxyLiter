/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.raphfrk.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import com.raphfrk.craftproxyliter.Globals;


public class ProtocolInputStream {

	private final InputStream in;

	final private byte[] buffer;
	final private int bufferMask;
	final private int bufferLength;

	private int start;
	private int length;

	final private int timeout = Globals.getNetTimeout() * 1000;


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

		boolean interrupted = false;

		int packetId = -1;

		//System.out.println("Starting new packet read " + startTime);

		while(ret == null && length < bufferLength && startTime + timeout > System.currentTimeMillis() && !Thread.currentThread().isInterrupted()) {
			//System.out.println("Length: " + length);
			try {
				ret = PacketScan.packetScan(buffer, start, length, bufferMask, packet);
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
				available -= 64;
				int actual = 0;
				try {
					actual = in.read(buffer, endMod, available);
					
					if(actual > 0 && packetId == -1) {
						packetId = 0xFF & buffer[startMod];
					}

					readSoFar += actual;
					if(readSoFar < 100) {
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
						System.err.println("Buffer full and unable to parse packet");
						System.out.println("Buffer full and unable to parse packet");
						StringBuilder sb = new StringBuilder();
						boolean first = true;
						for (int i = -32; i < 256; i++) {
							if (!first) {
								sb.append(", ");
							} else {
								first = false;
							}
							int pos = (start + i) & bufferMask;
							if (i == 0) {
								sb.append("_");
							}
							sb.append(Integer.toHexString(buffer[pos] & 0xFF));
							if (i == 0) {
								sb.append("_");
							}
						}
						throw new IOException("Buffer full and unable to parse packet");
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
