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

import com.raphfrk.protocol.Packet;

public class Packet52ProxyLogin extends Packet {

	public Packet52ProxyLogin(Packet packet) {
		super(packet, 0x52);
	}

	public Packet52ProxyLogin(String code, String hostname, String username) {
		super(username.length()*2 + code.length()*2 + hostname.length()*2 + 8);
		super.writeByte((byte)0x52);
		super.writeString16(code);
		super.writeString16(hostname);
		super.writeString16(username);
	}
	
	public String getCode() {
		return getString16(1);
	}
	
	public String getHostname() {
		int codeLength = getShort(1);
		return getString16(3 + codeLength*2);
	}
	
	public String getUsername() {
		int codeLength = getShort(1);
		int hostnameLength = getShort(3 + codeLength*2);
		return getString16(5 + codeLength*2 + hostnameLength*2);
	}
	
	public String getUsernameSplit() {
		String raw = getUsername();
		String[] split = raw.split(";");
		return split[0];
	}

}
