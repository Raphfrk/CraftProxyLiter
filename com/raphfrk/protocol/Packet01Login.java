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

public class Packet01Login extends Packet {
	
	public Packet01Login(Packet packet) {
		super(packet, 1);
	}
	
	public int getVersion() {
		return getInt(1);
	}
	
	public String getUsername() {
		return getString16(5);
	}
	
	public long getSeed() {
		return getLong(5 + getString16Length(5));
	}
	
	public int getMode() {
		return getInt(13 + getString16Length(5));
	}
	
	public byte getDimension() {
		return getByte(15 + getString16Length(5));
	}
	
	public byte getUnknown() {
		return getByte(16 + getString16Length(5));
	}
	
	public byte getHeight() {
		return getByte(17 + getString16Length(5));
	}
	
	public byte getMaxPlayers() {
		return getByte(18 + getString16Length(5));
	}
	
	

}
