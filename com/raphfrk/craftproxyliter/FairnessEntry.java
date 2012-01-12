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
package com.raphfrk.craftproxyliter;

import java.io.IOException;

import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.ProtocolOutputStream;

public class FairnessEntry {

	final ProtocolOutputStream pout;
	final Packet packet;
	final long timestamp;
	final KillableThread bridge;

	public FairnessEntry(ProtocolOutputStream pout, Packet packet, KillableThread bridge) {
		this.pout = pout;
		this.packet = packet;
		this.timestamp = System.currentTimeMillis();
		this.bridge = bridge;
	}

	public void send() throws IOException {
		long delay = (System.currentTimeMillis() - timestamp);
		if(delay > 20) {
			//System.out.println("Delay through fairness manager: " + delay);
		}
		pout.sendPacket(packet);
	}

	// Low priority ... check chunks  50, 51, 52, 53 and 130 (update sign)

}
