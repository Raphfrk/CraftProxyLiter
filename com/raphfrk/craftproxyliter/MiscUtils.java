package com.raphfrk.craftproxyliter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;


public class MiscUtils {
	
	static public SecureRandom random = new SecureRandom();
	
	static final String slash = System.getProperty("file.separator");
	
	protected static final Logger log = Logger.getLogger("Minecraft");
	static Object logSync = new Object();
	
	static void safeLogging( String message ) {
		safeLogging( log , message );
	}
	
	static void safeLogging( Logger log , String message ) {
		
		synchronized( logSync ) {
			log.info( message );
		}
	
	}
	

	

	static HashSet<String> fileToSet( String filename , boolean forceLowerCase ) {
		
		String filePath = filename;
		
		try {
			(new File( filePath )).createNewFile();
		} catch (IOException e) {}
		
		String[] list = fileToString( filePath );
		
		HashSet<String> set = new HashSet<String>();
		
		for( String current : list ) {
			if( forceLowerCase ) {
				set.add(current.toLowerCase().trim());
			} else {
				set.add(current.trim());
			}
		}
		
		return set;
		
	}
	
	static void stringToFile( ArrayList<String> string , String filename ) {
		
		File portalFile = new File( filename );

		BufferedWriter bw;
		
		try {
			bw = new BufferedWriter(new FileWriter(portalFile));
		} catch (FileNotFoundException fnfe ) {
			MiscUtils.safeLogging(log, "[CraftProxy-Lite] Unable to write to file: " + filename );
			return;
		} catch (IOException ioe) {
			MiscUtils.safeLogging(log, "[CraftProxy-Lite] Unable to write to file: " + filename );
			return;
		}
		
		try {
			for( Object line : string.toArray() ) {
				bw.write((String)line);
				bw.newLine();
			}
			bw.close();
		} catch (IOException ioe) {
			MiscUtils.safeLogging(log, "[CraftProxy-Lite] Unable to write to file: " + filename );
			return;
		}
		
	}
	
	static String[] fileToString( String filename ) {
		
		File portalFile = new File( filename );
		
		BufferedReader br;
		
		try {
			br = new BufferedReader(new FileReader(portalFile));
		} catch (FileNotFoundException fnfe ) {
			MiscUtils.safeLogging(log, "[CraftProxy-Lite] Unable to open file: " + filename );
			return null;
		} 
		
		StringBuffer sb = new StringBuffer();
		
		String line;
		
		try {
		while( (line=br.readLine()) != null ) {
			sb.append( line );
			sb.append( "\n" );
			
		}
		br.close();
		} catch (IOException ioe) {
			MiscUtils.safeLogging( log , "[CraftProxy-Lite] Error reading file: " + filename );
			return null;
		}
		
		return( sb.toString().split("\n") );
	}
	
	static String[] splitParam( String line ) {
		
		String[] split = line.split("=",-1);
		
		if( split.length < 2 ) {
			MiscUtils.safeLogging( log , "[CraftProxy-Lite] Unable to parse parameter from: " + line );
			return null;
		}
		
		String[] ret = new String[2];
		
		ret[0] = split[0];
		
		ret[1] = line.substring(ret[0].length() + 1 );
		
		return ret;
		
		
	}

	
	static boolean getBoolean( String var ) {
		
		if( 
				var.equalsIgnoreCase("true") || 
				var.equals("1") || 
				var.matches("^[tT].*$") ) {
			return true;
		}
		return false;
		
	}
	
	static boolean isBoolean( String var ) {

		if( 
				var.equalsIgnoreCase("true") || 
				var.equalsIgnoreCase("false") || 
				var.equals("1") || 
				var.equals("0") ||
				var.matches("^[tT].*$") ||
				var.matches("^[fF].*$") ) {
			return true;
		}
		return false;
		
	}
	
	static int getInt( String var ) {

		try {
			var = var.trim();
			int x = Integer.parseInt(var.trim());
			return x;
		} catch (NumberFormatException nfe ) {
			MiscUtils.safeLogging( log , "[CraftProxy-Lite] Unable to parse " + var + " as integer" );
			return 0;
		}
		
	}
	
	static boolean isInt (String string) {
		
		try {
			Integer.parseInt(string.trim());
		} catch (NumberFormatException nfe ) {
			return false;
		}
		return true;
	}
	
	static long getLong( String var ) {

		try {
			var = var.trim();
			long x = Long.parseLong(var.trim());
			return x;
		} catch (NumberFormatException nfe ) {
			MiscUtils.safeLogging( log , "[CraftProxy-Lite] Unable to parse " + var + " as Long" );
			return 0;
		}
		
	}
	
	static boolean isLong (String string) {
		
		try {
			Long.parseLong(string.trim());
		} catch (NumberFormatException nfe ) {
			return false;
		}
		return true;
	}
	
	static Double getDouble( String var ) {

		try {
			var = var.trim();
			double x = Double.parseDouble(var.trim());
			return x;
		} catch (NumberFormatException nfe ) {
			MiscUtils.safeLogging( log , "[CraftProxy-Lite] Unable to parse " + var + " as Double" );
			return 0.0;
		}
		
	}
	
	static boolean isDouble (String string) {
		
		try {
			Double.parseDouble(string.trim());
		} catch (NumberFormatException nfe ) {
			return false;
		}
		return true;
	}
	
	static boolean checkText( String text ) {
		
		if( text.length() > 15 ) {
			return false;
		}
		
		return text.matches("^[a-zA-Z0-9\\.\\-]+$");
	}

	

	
	static String base36( int x ) {
		
		return Integer.toString( x , 36 );
		
	}
	
	/*static int mod( int x , int r ) {
		
		int m = x % r;
		if( m < 0 ) m+=r;
		
		return m;
		
	}*/
		
	
	static boolean isThisMyIpAddress(InetAddress addr) {
	    // Check if the address is a valid special local or loop back
	    if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
	        return true;

	    // Check if the address is defined on any interface
	    try {
	        return NetworkInterface.getByInetAddress(addr) != null;
	    } catch (SocketException e) {
	        return false;
	    }
	}

	static boolean isAddressLocal(String address) {

		try {
			InetAddress addr = InetAddress.getByName( address );

			if( isThisMyIpAddress( addr ) ||
					addr.isLinkLocalAddress() ) {
				return true;
			} 

		} catch (Exception e) {};
		return false;

	}

	static int[] getLocalIP() {
		
		try {
			InetAddress addr = InetAddress.getLocalHost(); 
			
			byte[] ip = addr.getAddress(); 
			
			if( ip == null || ip.length != 4 ) {
				return null;
			} 
			
			int[] ret = new int[4];
			
			for( int cnt = 0;cnt<4;cnt++) {
				ret[cnt] = (int)ip[cnt];
				if( ret[cnt] < 0 ) {
					ret[cnt] += 256;
				}
			}
			
			return ret;
			
		} catch (UnknownHostException uhe ) {
			return null;
		}
		
	}
	
	static int[] getGlobalIP() {
		
		String globalServer = "checkip.dyndns.org";
		//String globalServer = "checkip.dyndns.org"
		
		//safeLogging(log , "Attempting to determine global IP using " + globalServer);
		
		try {
			URL ipCheck = new URL("http://checkip.dyndns.org");
			
			URLConnection ipCheckConnection = ipCheck.openConnection();
						
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							ipCheckConnection.getInputStream()));
	
			String line;
			
			line = in.readLine();
			
			String[] split = line.split("\\.");
			
			if( split.length != 4 ) {
				safeLogging(log , "Incorrect number of bytes: " + split.length );
				safeLogging(log , "Unable to parse IP address from " + globalServer);
				return null;
			}
			
			int[] ret = new int[4];
			
			int cnt;
			
			for( cnt=0;cnt<4;cnt++) {
				String singleByte = split[cnt].replaceAll("[^0-9]","");
				ret[cnt] = Integer.parseInt(singleByte);
			}
			
			return ret;
			
		} catch (Exception e) {
			safeLogging(log , "Unable to parse IP address from " + globalServer);
			e.printStackTrace();
			return null;
		}
		
	}
	
	static String genRandomCode() {
		synchronized( random ) {
			return new BigInteger(130, random).toString(32);
		}
	}
	
	static String sha1Hash( String inputString ) {

		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.reset();

			md.update(inputString.getBytes("utf-8"));
			
			BigInteger bigInt = new BigInteger( md.digest() );

			return bigInt.toString( 16 ) ;

		} catch (Exception ioe) {
			safeLogging( "Hashing error, this will probably prevent connections working");
			return "hash error";
		}

	}
	
	static String errorCheck( String line ) {
		
		if( line.matches("^Error: .*$")) {
			return line.substring(7);
		} else {
			return null;
		}

	}
	
	static File dirScan( String dir , String fileName ) {
		
		File dirFile = new File( dir );
		
		if( !dirFile.isDirectory() ) {
			return null;
		} else {
			
			File[] files = dirFile.listFiles();
			
			for( File file : files ) {
				
				if( file.getName().equalsIgnoreCase(fileName)) {
					return file;
				}
				
			}
			
		}
		
		return null;
		
	}

}
