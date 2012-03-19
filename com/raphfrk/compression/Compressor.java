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
package com.raphfrk.compression;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.raphfrk.craftproxyliter.FairnessManager;
import com.raphfrk.craftproxyliter.Globals;
import com.raphfrk.craftproxyliter.Main;
import com.raphfrk.craftproxyliter.PassthroughConnection;
import com.raphfrk.protocol.Packet;

public class Compressor {

	final Deflater d;
	final Inflater i;
	final FairnessManager fm;
	final ConcurrentHashMap<Long,Boolean> hashesReceived;
	final ConcurrentHashMap<Long,Boolean> hashesSent;
	final byte[] compressed;
	final byte[] decompressed;
	final long[] packetHashes;
	final ExecutorService pool = Executors.newFixedThreadPool(4);
	final int maxStripes;
	final int bufferLength;
	final ArrayList<Future<Long>> hashResults = new ArrayList<Future<Long>>(40);
	final ArrayList<HashGenerator> hashGenerators;
	final HashStore hs;

	public Compressor(PassthroughConnection ptc, FairnessManager fm, HashStore hs, int maxHeight) {

		bufferLength = maxHeight << 10;
		
		compressed = new byte[bufferLength];
		decompressed = new byte[bufferLength];
		
		maxStripes = maxHeight >> 1;
		
		packetHashes = new long[maxStripes];
		
		this.d = new Deflater(Globals.getCompressionLevel());
		this.i = new Inflater();
		this.fm = fm;
		this.hs = hs;
		this.hashesReceived = ptc.connectionInfo.hashesReceived;
		this.hashesSent = ptc.connectionInfo.hashesSent;
		
		hashGenerators = new ArrayList<HashGenerator>(maxStripes);
		
		for(int cnt=0;cnt<maxStripes;cnt++) {
			hashGenerators.add(new HashGenerator());
			hashResults.add(null);
		}

	}
	
	public void destroyPool() {
		pool.shutdown();
		while(!pool.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public Packet decompress(Packet packet, PassthroughConnection ptc) {

		int stripes = packet.getInt(14) >> 1;
		
		int length = packet.getInt(18 + (stripes << 3));
		Packet newPacket = packet.clone(fm);
		byte[] buffer = newPacket.buffer;
		
		if(length > bufferLength - 50) {
			return null;
		}
		
		for(int cnt = 0; cnt < stripes; cnt++) {
			packetHashes[cnt] = packet.getLong(18 + (cnt << 3));
		}
	
		int start = 26 + (stripes << 3);
		
		i.reset();
		i.setInput(buffer, start, length);
		i.finished();
		
		int expandedLength;
		try {
			expandedLength = i.inflate(decompressed, 0, decompressed.length);
		} catch (DataFormatException dfe) {
			ptc.printLogMessage("Data format exception");
			return null;
		}
		
		
		for(int cnt=0;cnt<stripes;cnt++) {
			
			//System.out.println("Header hash: 0x" + Long.toHexString(packetHashes[cnt]));
			
			byte[] block = hs.getHash(ptc, packetHashes[cnt]);
			if(block != null) {
				HashGenerator.copyToBuffer(decompressed, cnt, block);
			} else {
				block = new byte[2048];
				HashGenerator.copyFromBuffer(decompressed, cnt, block);
				hs.addHash(packetHashes[cnt], block);
			}	
		}
		
		d.reset();
		d.setInput(decompressed, 0, expandedLength);
		d.finish();
		
		int newSize = d.deflate(compressed);

		Packet outPacket = new Packet();
		outPacket.buffer = new byte[newSize + 30];

		outPacket.writeByte((byte)0x33);
		outPacket.writeInt(packet.getInt(1));
		outPacket.writeInt(packet.getInt(5));
		outPacket.writeByte(packet.getByte(9));
		outPacket.writeShort(packet.getShort(10));
		outPacket.writeShort(packet.getShort(12));
		outPacket.writeInt(newSize);
		outPacket.writeInt(packet.getByte(22 + (stripes << 3)));
		
		System.arraycopy(compressed, 0, outPacket.buffer, 22, newSize);
		outPacket.end+=newSize;
		ptc.connectionInfo.saved.addAndGet(newSize - length - (stripes << 3) - 4);
		
		int percent = (int)(((100.0)*ptc.connectionInfo.saved.get())/ptc.connectionInfo.uploaded.get());
		if(Main.craftGUI != null) {
			Main.craftGUI.safeSetStatus("<html>Saved " + (ptc.connectionInfo.saved.get()/1024) + " kB<br>Compression of " + percent + "</html>");
		} else {
			//ptc.printLogMessage("Saved: " + percent);
		}
		
		//System.out.println("Saved % = " + ((100.0)*ptc.connectionInfo.saved.get())/ptc.connectionInfo.uploaded.get());
		//System.out.println("New Size: " + newSize + " initial size: " + length);
		//System.out.println("Saved: " + ptc.connectionInfo.saved.get());
		//System.out.println("Uploaded: " + ptc.connectionInfo.uploaded.get());
	
		return outPacket;
		
	}

	public Packet compress(Packet packet) {

		Packet newPacket = packet.clone(fm);
		byte[] buffer = newPacket.buffer;
		int start = 22; // new int added
		int length = packet.getInt(14);
		
		if(length > bufferLength) {
			return packet;
		}

		i.reset();
		i.setInput(buffer, start, length);
		i.finished();

		int expandedLength;
		try {
			expandedLength = i.inflate(decompressed, 0, decompressed.length);
		} catch (DataFormatException dfe) {
			return packet;
		}

		if(expandedLength < 16384) {
			return packet;
		}

		for (int p = expandedLength; p < bufferLength && p < expandedLength + 2048; p++) {
			decompressed[p] = 0;
		}
		
		int stripes = (expandedLength + 2047) >> 11;
		
		for(int cnt=0;cnt<stripes;cnt++) {
			HashGenerator current = hashGenerators.get(cnt);
			current.blockNum = cnt;
			current.buffer = decompressed;
			current.wipeBuffer = false;
			try {
				hashResults.set(cnt, pool.submit(current));
			} catch (RejectedExecutionException ree) {
				return packet;
			}
		}

		for(int cnt=0;cnt<stripes;cnt++) {
			Future<Long> result = hashResults.get(cnt);
			try {
				packetHashes[cnt] = result.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return packet;
			} catch (ExecutionException e) {
				e.printStackTrace();
				return packet;
			}
		}
		
		int hit = 0;

		for(int cnt=0;cnt<stripes;cnt++) {
			//System.out.print("Checking for hash: 0x" + Long.toHexString(packetHashes[cnt]));
			if(hashesReceived.containsKey(packetHashes[cnt])) {
				//System.out.println(" - Hit");
				hit++;
				HashGenerator current = hashGenerators.get(cnt);
				current.blockNum = cnt;
				current.buffer = decompressed;
				current.wipeBuffer = true;
				try {
					hashResults.set(cnt, pool.submit(current));
				} catch (RejectedExecutionException ree) {
					return packet;
				}
			} else {
				//System.out.println(" - Miss");
				hashResults.set(cnt, null);
			}
		}
		
		for(int cnt=0;cnt<stripes;cnt++) {
			hashesReceived.put(packetHashes[cnt], true);
		}
		
		//System.out.println("Hit: " + hit + "-" + (40-hit));

		for(int cnt=0;cnt<stripes;cnt++) {
			Future<Long> result = hashResults.get(cnt);
			if(result != null) {
				try {
					result.get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return packet;
				} catch (ExecutionException e) {
					e.printStackTrace();
					return packet;
				}
			}
		}
		
		d.reset();
		d.setInput(decompressed, 0, expandedLength);
		d.finish();
		
		int newSize = d.deflate(compressed);
		
		Packet outPacket = new Packet();
		outPacket.buffer = new byte[newSize + 50 + stripes * 8];

		outPacket.writeByte((byte)0x51);
		outPacket.writeInt(packet.getInt(1));
		outPacket.writeInt(packet.getInt(5));
		outPacket.writeByte(packet.getByte(9));
		outPacket.writeShort(packet.getShort(10));
		outPacket.writeShort(packet.getShort(12));
		
		outPacket.writeInt(stripes * 2); // since it counts in ints
		for(int cnt = 0; cnt < stripes; cnt++) {
			outPacket.writeLong(packetHashes[cnt]);
		}
		
		outPacket.writeInt(newSize);
		outPacket.writeInt(packet.getInt(18));
		
		System.arraycopy(compressed, 0, outPacket.buffer, outPacket.end, newSize);
		outPacket.end+=newSize;
		
		//System.out.println("Saved: (" + length + " -> " + newSize + ") " + (length - newSize));
		
		return outPacket;
		
	}


}
