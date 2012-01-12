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
