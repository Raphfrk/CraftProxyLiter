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

public class Packet09Respawn extends Packet {
	
	public Packet09Respawn(Packet packet) {
		super(packet, 9);
	}

	public Packet09Respawn(int dimension, byte difficulty, byte creative, short height, String levelType) {
		super(14 + levelType.length() + 2);
		super.writeByte((byte)0x09);
		super.writeInt(dimension);
		super.writeByte(difficulty);
		super.writeByte(creative);
		super.writeShort(height);
		super.writeString16(levelType);
	}
	
	public int getDimension() {
		return getInt(1);
	}
	
	public byte getDifficulty() {
		return getByte(5);
	}
	
	public byte getCreative() {
		return getByte(6);
	}
	
	public short getHeight() {
		return getShort(7);
	}
	
	public String getType() {
		return getString16(9);
	}
	
}
