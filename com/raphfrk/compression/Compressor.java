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
	final byte[] compressed = new byte[1024*128];
	final byte[] decompressed = new byte[1024*128];
	final long[] packetHashes = new long[40];
	final int dataSize = 80*1024;
	final ExecutorService pool = Executors.newFixedThreadPool(4);
	final ArrayList<Future<Long>> hashResults = new ArrayList<Future<Long>>(40);
	final ArrayList<HashGenerator> hashGenerators;
	final HashStore hs;

	public Compressor(PassthroughConnection ptc, FairnessManager fm, HashStore hs) {

		this.d = new Deflater(Globals.getCompressionLevel());
		this.i = new Inflater();
		this.fm = fm;
		this.hs = hs;
		this.hashesReceived = ptc.connectionInfo.hashesReceived;
		this.hashesSent = ptc.connectionInfo.hashesSent;
		
		hashGenerators = new ArrayList<HashGenerator>(40);
		
		for(int cnt=0;cnt<40;cnt++) {
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
		
		int length = packet.getInt(14);
		Packet newPacket = packet.clone(fm);
		byte[] buffer = newPacket.buffer;
		int start = 18;
		
		if(length > 131072) {
			return null;
		}
		
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
		
		if(expandedLength != 81920 + 320) {
			ptc.printLogMessage("Wrong length");
			return null;
		}
		
		HashManager.extractHashesList(decompressed, packetHashes);
		
		for(int cnt=0;cnt<40;cnt++) {
			
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
		d.setInput(decompressed, 0, expandedLength - 320);
		d.finish();
		
		int newSize = d.deflate(compressed);

		Packet outPacket = new Packet();
		outPacket.buffer = new byte[newSize + 18];

		outPacket.writeByte((byte)0x33);
		outPacket.writeInt(packet.getInt(1));
		outPacket.writeShort(packet.getShort(5));
		outPacket.writeInt(packet.getInt(7));
		outPacket.writeByte(packet.getByte(11));
		outPacket.writeByte(packet.getByte(12));
		outPacket.writeByte(packet.getByte(13));
		outPacket.writeInt(newSize);
		
		System.arraycopy(compressed, 0, outPacket.buffer, 18, newSize);
		outPacket.end+=newSize;
		ptc.connectionInfo.saved.addAndGet(newSize - length);
		
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
		int start = 18;
		int length = packet.getInt(14);
		
		if(length > 131072) {
			return null;
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

		if(expandedLength != (81920)) {
			return packet;
		}

		for(int cnt=0;cnt<40;cnt++) {
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

		for(int cnt=0;cnt<40;cnt++) {
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

		for(int cnt=0;cnt<40;cnt++) {
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
		
		for(int cnt=0;cnt<40;cnt++) {
			hashesReceived.put(packetHashes[cnt], true);
		}
		
		//System.out.println("Hit: " + hit + "-" + (40-hit));

		for(int cnt=0;cnt<40;cnt++) {
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
		
		HashManager.setHashesList(decompressed, packetHashes);
		
		d.reset();
		d.setInput(decompressed, 0, expandedLength + 320);
		d.finish();
		
		int newSize = d.deflate(compressed);
		
		Packet outPacket = new Packet();
		outPacket.buffer = new byte[newSize + 18];

		outPacket.writeByte((byte)0x51);
		outPacket.writeInt(packet.getInt(1));
		outPacket.writeShort(packet.getShort(5));
		outPacket.writeInt(packet.getInt(7));
		outPacket.writeByte(packet.getByte(11));
		outPacket.writeByte(packet.getByte(12));
		outPacket.writeByte(packet.getByte(13));
		outPacket.writeInt(newSize);
		
		System.arraycopy(compressed, 0, outPacket.buffer, 18, newSize);
		outPacket.end+=newSize;
		
		//System.out.println("Saved: (" + length + " -> " + newSize + ") " + (length - newSize));
		
		return outPacket;
		
	}


}
