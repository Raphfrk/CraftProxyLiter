package com.raphfrk.protocol;

import java.util.Arrays;

import com.raphfrk.craftproxyliter.FairnessManager;

public class Packet {

	public boolean isValid;

	public byte[] buffer;
	public int mask;
	public int start = 0;
	public int end = 0;
	public long timeStamp;

	public Packet clone(FairnessManager fm) {

		Packet p = new Packet();

		int length = end - start;

		p.buffer = new byte[length];

		p.start = 0;
		p.end = length;
		p.mask = mask;
		p.timeStamp = timeStamp;

		int startMod = start & mask;
		int endMod = end & mask;
			if( startMod > endMod ) {
				int l1 = mask + 1 - startMod;
				System.arraycopy(buffer, startMod, p.buffer, 0, l1);
				System.arraycopy(buffer, 0, p.buffer, l1, length - l1);
			} else {
				System.arraycopy(buffer, startMod, p.buffer, 0, length);
			}

		return p;
	}

	public Packet() {	
		timeStamp = System.currentTimeMillis();
		start = 0;
		end = 0;
		mask = 0xFFFFFFFF;
	}

	public Packet(Packet packet, int expectedId) {
		buffer = packet.buffer;
		mask = packet.mask;
		start = packet.start;
		end = packet.end;
		isValid = (packet.buffer.length == 0 || (start == end)) ? false : 
			(buffer[0] & 0xFF) == expectedId;
	}

	public Packet(int size) {
		this(size, (byte)0);
	}

	public Packet(int size, byte packetId) {
		int powOf2 = size;
		powOf2 |= (powOf2>>1);
		powOf2 |= (powOf2>>2);
		powOf2 |= (powOf2>>4);
		powOf2 |= (powOf2>>8);
		powOf2 |= (powOf2>>16);
		mask = powOf2;
		powOf2++;

		if(powOf2 == 0) {
			powOf2++;
		}

		buffer = new byte[powOf2];
		buffer[0] = packetId;
		start = 0;
		end = 0;
	}
	
	public void setByte(int pos, byte b) {
		buffer[(start + pos++) & mask] = b;
	}

	public void setShort(int pos, short s) {
		buffer[(start + pos++) & mask] = (byte)(s >> 8);
		buffer[(start + pos++) & mask] = (byte)(s >> 0);
	}

	public void setInt(int pos, int i) {
		buffer[(start + pos++) & mask] = (byte)(i >> 24);
		buffer[(start + pos++) & mask] = (byte)(i >> 16);
		buffer[(start + pos++) & mask] = (byte)(i >> 8);
		buffer[(start + pos++) & mask] = (byte)(i >> 0);
	}	
	
	public void setLong(int pos, long i) {
		buffer[(start + pos++) & mask] = (byte)(i >> 56);
		buffer[(start + pos++) & mask] = (byte)(i >> 48);
		buffer[(start + pos++) & mask] = (byte)(i >> 40);
		buffer[(start + pos++) & mask] = (byte)(i >> 32);
		buffer[(start + pos++) & mask] = (byte)(i >> 24);
		buffer[(start + pos++) & mask] = (byte)(i >> 16);
		buffer[(start + pos++) & mask] = (byte)(i >> 8);
		buffer[(start + pos++) & mask] = (byte)(i >> 0);
	}	

	public void writeByte(byte b) {
		buffer[(end++) & mask] = b;
	}

	public void writeShort(short s) {
		buffer[(end++) & mask] = (byte)(s >> 8);
		buffer[(end++) & mask] = (byte)(s >> 0);
	}

	public void writeInt(int i) {
		buffer[(end++) & mask] = (byte)(i >> 24);
		buffer[(end++) & mask] = (byte)(i >> 16);
		buffer[(end++) & mask] = (byte)(i >> 8);
		buffer[(end++) & mask] = (byte)(i >> 0);
	}	
	
	public void writeLong(long i) {
		buffer[(end++) & mask] = (byte)(i >> 56);
		buffer[(end++) & mask] = (byte)(i >> 48);
		buffer[(end++) & mask] = (byte)(i >> 40);
		buffer[(end++) & mask] = (byte)(i >> 32);
		buffer[(end++) & mask] = (byte)(i >> 24);
		buffer[(end++) & mask] = (byte)(i >> 16);
		buffer[(end++) & mask] = (byte)(i >> 8);
		buffer[(end++) & mask] = (byte)(i >> 0);
	}	

	public void writeString16(String s) {
		int length = s.length();
		writeShort((short)length);
		for(int cnt=0;cnt<length;cnt++) {
			writeShort((short)s.charAt(cnt));
		}
	}

	public byte getByte(int pos) {
		return buffer[(pos + start)&mask];
	}

	public short getShort(int pos) {
		int a = buffer[((pos++) + start)&mask] & 0xFF;
		int b = buffer[((pos++) + start)&mask] & 0xFF;
		return (short)((a << 8) | b);
	}

	public int getInt(int pos) {
		int a = buffer[((pos++) + start)&mask] & 0xFF;
		int b = buffer[((pos++) + start)&mask] & 0xFF;
		int c = buffer[((pos++) + start)&mask] & 0xFF;
		int d = buffer[((pos++) + start)&mask] & 0xFF;
		return ((a << 24) | (b << 16) | (c << 8) | d);
	}

	public long getLong(int pos) {
		long a = buffer[((pos++) + start)&mask] & 0xFF;
		long b = buffer[((pos++) + start)&mask] & 0xFF;
		long c = buffer[((pos++) + start)&mask] & 0xFF;
		long d = buffer[((pos++) + start)&mask] & 0xFF;
		long e = buffer[((pos++) + start)&mask] & 0xFF;
		long f = buffer[((pos++) + start)&mask] & 0xFF;
		long g = buffer[((pos++) + start)&mask] & 0xFF;
		long h = buffer[((pos++) + start)&mask] & 0xFF;
		return (long)((a << 56) | (b << 48) | (c << 40) | (d << 32) | (e << 24) | (f << 16) | (g << 8) | h);
	}

	public String getString16(int pos) {
		int length = getShort(pos);
		pos+=2;

		StringBuilder sb = new StringBuilder(length);

		for(int cnt=0; cnt<length;cnt++) {
			sb.append((char)getShort(pos));
			pos+=2;
		}
		return sb.toString();
	}

	public int getString16Length(int pos) {
		return 2 + getShort(pos);
	}

	boolean isValid() {
		return (end - start) <= buffer.length;
	}
	
	public boolean equals(Packet p) {
		
		if((end-start) != (p.end - p.start)) {
			return false;
		}
		
		if(p.mask != mask || p.timeStamp != timeStamp) {
			return false;
		}
		
		int length = end-start;
		for(int cnt=0;cnt<length;cnt++) {
			if(buffer[(start+cnt)&mask] != p.buffer[(p.start+cnt)&mask]) {
				System.out.println("Mismatch at position " + cnt);
				return false;
			}
		}
		
		return true;
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(int cnt = 0; cnt<end-start;cnt++) {
			if(!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(Integer.toHexString(getByte(cnt) & 0xFF));
		}
		return sb.toString();
	}
}
