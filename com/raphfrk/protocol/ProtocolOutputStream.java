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

import java.io.IOException;
import java.io.OutputStream;

import com.raphfrk.craftproxyliter.Globals;
import com.raphfrk.netutil.MaxLatencyBufferedOutputStream;

public class ProtocolOutputStream {

	private final OutputStream buffered;

	public ProtocolOutputStream(OutputStream out) {
		buffered = new MaxLatencyBufferedOutputStream(out, 1024, Globals.getBufferLatency());
		//buffered = out;
	}

	int lastPacketId = 0;

	public Packet sendPacket(Packet packet) throws IOException {

		int top = packet.mask + 1;
		int start = packet.start & packet.mask;
		int end = packet.end & packet.mask;

		int packetId = packet.buffer[start] & 0xFF;
		lastPacketId = packetId;

		if(start > end) {
			buffered.write(packet.buffer, start, top - start);
			buffered.write(packet.buffer, 0, end);
		} else {
			/*StringBuilder sb = new StringBuilder(buffered + ":" + "[ ");
			for(int cnt = start; cnt < end; cnt++) {
				sb.append(Integer.toHexString(packet.buffer[cnt&packet.mask]&0xFF) + " ");
			}
			sb.append("]");
			System.out.println(sb.toString());*/
			buffered.write(packet.buffer, start, end - start);

		}

		return packet;

	}

	public void flush() throws IOException {
		buffered.flush();
	}

	public void close() throws IOException {
		if(lastPacketId != 0xFF) {
			Packet closeKick = new PacketFFKick("[CraftProxyLiter] Protocol stream closed");
			sendPacket(closeKick);
		}
		buffered.flush();
		buffered.close();
	}

}
