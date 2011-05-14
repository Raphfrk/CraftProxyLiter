package com.raphfrk.compression;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.raphfrk.craftproxyliter.FairnessManager;
import com.raphfrk.craftproxyliter.PassthroughConnection;
import com.raphfrk.protocol.Packet;

public class Compressor {

	final Deflater d;
	final Inflater i;
	final ConcurrentHashMap<Long,Boolean> hashes;
	final FairnessManager fm;
	final byte[] compressed = new byte[1024*128];
	final byte[] decompressed = new byte[1024*128];
	final long[] packetHashes = new long[40];
	final int dataSize = 80*1024;
	final ExecutorService pool = Executors.newFixedThreadPool(4);
	final ArrayList<Future<Long>> hashResults = new ArrayList<Future<Long>>(40);
	final ArrayList<HashGenerator> hashGenerators = new ArrayList<HashGenerator>(40);
	final HashStore hs;

	public Compressor(ConcurrentHashMap<Long,Boolean> hashes, FairnessManager fm, HashStore hs) {

		this.d = new Deflater(1);
		this.i = new Inflater();
		this.fm = fm;
		this.hashes = hashes;
		this.hs = hs;

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
		
		int expandedLength;
		try {
			expandedLength = i.inflate(decompressed, 0, decompressed.length);
		} catch (DataFormatException dfe) {
			return null;
		}
		
		if(expandedLength != 81920 + 320) {
			return null;
		}
		
		HashManager.extractHashesList(decompressed, packetHashes);
		
		for(int cnt=0;cnt<40;cnt++) {
			
			byte[] block = hs.getHash(ptc, packetHashes[cnt]);
			if(block != null) {
				HashGenerator.copyToBuffer(decompressed, cnt, block);
			}
			
		}
		
		d.reset();
		d.setInput(decompressed, 0, expandedLength - 320);
		d.finish();
		
		int newSize = d.deflate(compressed);
		
		Packet outPacket = new Packet();
		outPacket.buffer = new byte[newSize + 17];

		outPacket.writeByte((byte)0x51);
		outPacket.writeInt(packet.getInt(1));
		outPacket.writeShort(packet.getShort(5));
		outPacket.writeInt(packet.getInt(7));
		outPacket.writeByte(packet.getByte(11));
		outPacket.writeByte(packet.getByte(12));
		outPacket.writeByte(packet.getByte(13));
		outPacket.writeInt(newSize);
		
		System.arraycopy(compressed, 0, buffer, 18, newSize);
		outPacket.end+=newSize;
		
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
			hashResults.set(cnt, pool.submit(current));
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

		for(int cnt=0;cnt<40;cnt++) {
			if(hashes.containsKey(packetHashes[cnt])) {
				HashGenerator current = hashGenerators.get(cnt);
				current.blockNum = cnt;
				current.buffer = decompressed;
				current.wipeBuffer = true;
				hashResults.set(cnt, pool.submit(current));
			} else {
				hashResults.set(cnt, null);
			}
		}

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
		
		HashManager.setHashesList(buffer, packetHashes);
		
		d.reset();
		d.setInput(decompressed, 0, expandedLength + 320);
		d.finish();
		
		int newSize = d.deflate(compressed);
		
		Packet outPacket = new Packet();
		outPacket.buffer = new byte[newSize + 17];

		outPacket.writeByte((byte)0x33);
		outPacket.writeInt(packet.getInt(1));
		outPacket.writeShort(packet.getShort(5));
		outPacket.writeInt(packet.getInt(7));
		outPacket.writeByte(packet.getByte(11));
		outPacket.writeByte(packet.getByte(12));
		outPacket.writeByte(packet.getByte(13));
		outPacket.writeInt(newSize);
		
		System.arraycopy(compressed, 0, buffer, 18, newSize);
		outPacket.end+=newSize;
		
		return outPacket;
		
	}


}
