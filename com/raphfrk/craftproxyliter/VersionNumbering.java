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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VersionNumbering {
	final static String version = "128";

	static String name = "default";
	static final String slash = System.getProperty("file.separator");
	static String prefix = "default";
	
	static void updateUpdatr() {
		
		if( !(new File( "Updatr")).exists() ) {
			(new File( "Updatr")).mkdir();
		}

		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter("Updatr" + slash + name + ".updatr"));
			
			bw.write( "name=" + name );
			bw.newLine();
			
			bw.write( "version=" + version );
			bw.newLine();
			
			bw.write( "url=www.prydwen.net/minec/latest" + prefix + "/" + name + ".updatr");
			bw.newLine();
			
			bw.write( "file=www.prydwen.net/minec/latest" + prefix + "/" + name + ".updatr");
			bw.newLine();
			
			bw.write( "notes=");
			bw.newLine();
			
			bw.close();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		
		
	}
}
