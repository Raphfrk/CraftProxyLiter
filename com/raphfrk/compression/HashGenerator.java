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

import java.util.concurrent.Callable;

public class HashGenerator implements Callable<Long> {

	public byte[] buffer;
	public int blockNum;
	public boolean wipeBuffer;
	
	public Long call() {
		
		int start = blockNum << 11;
		int end = start + 2048;
		
		long h = 1;
		
		for (int pos = start; pos < end; pos++) {
			if(wipeBuffer) {
				buffer[pos] = 1;
			} else {
				h += (h<<5) + (long)buffer[pos];
			}
		}
		
		return h;
	}
	
	static void copyToBuffer(byte[] buffer, int blockNum, byte[] block) {

		int pos = blockNum << 11;
		
		for (int blockPos = 0; blockPos < 2048; blockPos++) {
			buffer[pos] = block[blockPos];
			pos++;
		}
		
	}
	
	static void copyFromBuffer(byte[] buffer, int blockNum, byte[] block) {
		
		int pos = blockNum << 11;
		
		for (int blockPos = 0; blockPos < 2048; blockPos++) {
			block[blockPos] = buffer[pos];
			pos++;
		}
		
	}

}
