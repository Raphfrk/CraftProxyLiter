package com.raphfrk.compression;

public class HashManager {

	public static void extractHashesList(byte[] buffer, long[] hashes) {

		int pos = 80*1024;

		for(int a=0;a<40;a++) {
			long hash = 0;
			for(int b=0;b<8;b++) {
				byte value = buffer[pos++];
				hash = (hash << 8) & 0xFFFFFFFFFFFFFF00L;
				hash |= ((long)value) & 0xFF;
			}
			hashes[a] = hash;
		}
	}

	public static void setHashesList(byte[] buffer, long[] hashes) {

		int pos = 80*1024;

		for(int a=0;a<40;a++) {
			long hash = hashes[a];
			for(int b=0;b<8;b++) {
				buffer[pos++] = (byte)(hash>>56);
				hash = hash << 8;
			}
		}
	}

}