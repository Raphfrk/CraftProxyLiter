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
package com.raphfrk.craftproxyliter;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

public class BanList {

	static HashSet<String> names = null;

	static boolean whiteList = false;

	static void setWhiteList(boolean whiteList) {
		BanList.whiteList = whiteList;
	}

	static boolean getWhiteList() {
		return whiteList;
	}

	static String filename;
	
	static void init(String filename, boolean whiteList) {
		
		synchronized(lastRead) {
			BanList.whiteList = whiteList;

			lastRead.set(System.currentTimeMillis());

			if(filename != null) {
				BanList.filename = filename;
				names = MiscUtils.fileToSet(filename, true);
			}
		}
	}

	final static AtomicLong lastRead = new AtomicLong(0L);

	static boolean banned(String name) {

		synchronized(lastRead) {

			if(names==null) {
				return false;
			}

			if(lastRead.get() + 30000 <  System.currentTimeMillis()) {

				init(filename, whiteList);

			}

			return whiteList ^ names.contains(name.toLowerCase().trim());
		}
	}

}
