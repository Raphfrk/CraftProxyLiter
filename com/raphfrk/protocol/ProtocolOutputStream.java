package com.raphfrk.protocol;

import java.io.IOException;
import java.io.OutputStream;

import com.raphfrk.craftproxyliter.Globals;
import com.raphfrk.netutil.MaxLatencyBufferedOutputStream;

public class ProtocolOutputStream {

	private final OutputStream buffered;

	public ProtocolOutputStream(OutputStream out) {
		buffered = new MaxLatencyBufferedOutputStream(out, 1024, Globals.getBufferLatency());
	}

	public Packet sendPacket(Packet packet) throws IOException {

		int top = packet.mask + 1;
		int start = packet.start & packet.mask;
		int end = packet.end & packet.mask;
		
		if(start > end) {
			buffered.write(packet.buffer, start, top - start);
			buffered.write(packet.buffer, 0, end);
		} else {
			/*StringBuilder sb = new StringBuilder(packet + "Sending: " + start + " to " + end + "[ ");
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
		buffered.close();
	}
	
}
