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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logging {

	static BufferedWriter writer = null;

	static synchronized void setFilename(String filename) {

		if(writer != null) {
			try {
				writer.close();
				writer = null;
			} catch (IOException e) {
				System.out.println("Unable to close log file");
			}
		}

		if(filename == null) {
			writer = null;
			return;
		}

		FileWriter out = null;
		try {
			out = new FileWriter(filename, true);
		} catch (IOException e) {
			System.out.println("Unable to open log file: " + filename);
			writer = null;
			return;
		}
		if(out != null) {
			writer = new BufferedWriter(out);
		} 
	}

	static synchronized void log(String text) {
		System.out.println(text);
		if(writer != null) {
			try {
				writer.write(text);
				writer.newLine();
				writer.flush();
			} catch (IOException e) {
				System.out.println("Unable to write to log file");
				writer = null;
			}
		}
	}
	
	static synchronized void flush() {
		if(writer != null) {
			try {
				writer.flush();
			} catch (IOException e) {
				System.out.println("Unable to flush to log file");
			}
		}
	}

}
