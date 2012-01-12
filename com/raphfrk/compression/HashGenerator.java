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

	final static int[] startPoint;
	final static int[] step1;

	static {

		startPoint = new int[40];
		step1 = new int[40];

		int cnt;
		for(cnt=0;cnt<16;cnt++) {
			startPoint[cnt] = cnt<<3;
			step1[cnt] = 120;
		}

		int nextStart = 32768;
		for(cnt=16;cnt<40;cnt++) {
			if((cnt & 0x0007) == 0) {
				startPoint[cnt] = nextStart;
				nextStart += 16384;
			} else {
				startPoint[cnt] = startPoint[cnt-1] + 8;
			}
			step1[cnt] = 56;
		}

	}
	
	public byte[] buffer;
	public int blockNum;
	public boolean wipeBuffer;
	
	public Long call() {
		
		int start = startPoint[blockNum];
		int step = step1[blockNum];
		
		int pos = start;
		
		long h = 1;
		int count = 0;
		
		for(int outer=0;outer<256;outer++) {
			for(int inner=0;inner<8;inner++) {
				if(wipeBuffer) {
					buffer[pos] = 1;
				} else {
					h += (h<<5) + (long)buffer[pos];
				}
				count++;
				pos++;
			}
			pos+=step;
		}
		
		return h;
	}
	
	static void copyToBuffer(byte[] buffer, int blockNum, byte[] block) {
		
		int start = startPoint[blockNum];
		int step = step1[blockNum];
		
		int pos = start;
		
		int count = 0;
		int blockPos = 0;
		
		for(int outer=0;outer<256;outer++) {
			for(int inner=0;inner<8;inner++) {
				buffer[pos] = block[blockPos++];
				count++;
				pos++;
			}
			pos+=step;
		}
		
	}
	
	static void copyFromBuffer(byte[] buffer, int blockNum, byte[] block) {
		
		int start = startPoint[blockNum];
		int step = step1[blockNum];
		
		int pos = start;
		
		int count = 0;
		int blockPos = 0;
		
		for(int outer=0;outer<256;outer++) {
			for(int inner=0;inner<8;inner++) {
				block[blockPos++] = buffer[pos];
				count++;
				pos++;
			}
			pos+=step;
		}
		
	}

}
