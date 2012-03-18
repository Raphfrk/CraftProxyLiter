package com.raphfrk.craftproxyliter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VersionNumbering {
	final static String version = "146";

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
