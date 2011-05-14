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
