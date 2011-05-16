package com.raphfrk.craftproxyliter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

	public static Object sleeper = new Object();

	public static boolean consoleInput = true;

	public static void main(String[] args, boolean consoleInput) {
		Main.consoleInput = consoleInput;
		main(args);
	}
	
	public static CraftProxyGUI craftGUI = null;
	
	public static File cacheDir = new File("CPL_cache");
	
	static ProxyListener server = null;

	public static void main(String [] args) {

		if(args.length==0 && craftGUI == null) {
			
			craftGUI = new CraftProxyGUI();
			
			craftGUI.setVisible(true);
			return;
			
		}
		
		Logging.log( "Starting Craftproxy-lite version " +  VersionNumbering.version );

		String listenHostname = null;
		String defaultHostname = null;

		String usageString = "craftproxy <listen hostname>:<listen port> <default hostname>:<default-port> .... parameters";

		if( args.length < 2 ) {
			Logging.log( "Usage: " + usageString );
			Logging.log("    auth                  Switches on authentication (not needed)");
			Logging.log("    auth_off              Proxy leaves authentication to Minecraft server");
			Logging.log("    staticlocalhost       Forces use of 127.0.0.1 for localhost");
			Logging.log("    rotatelocalhost       Uses a different 127.0.x.y for each connection to localhost");
			Logging.log("    clientversion  <num>  Allows manually setting of client version");
			Logging.log("    password <password>   Sets password for multi-LAN/global mode");
			Logging.log("    reconnectfile <file>  Sets the reconnect file");
			Logging.log("    banned <file>         Sets the banned list file");
			Logging.log("    log <file>            Redirects output to a log file");
			Logging.log("    dimension <num>       Sets the dimension (-1 = hell, 0=normal)");
			Logging.log("    seed <num>            Sets the world seed");
			Logging.log("    monitor <period ms>   Enables bandwidth use logging");
			Logging.log("    compress_info         Outputs info related to the compression/cache system");
			Logging.log("    local_cache           Puts the proxy in local cache mode");
			Logging.log("    bridge_connection     Authentication is handled by backend server");
			Logging.log("    cache_limit           Sets the max size of the cache");
			Logging.log("    quiet:                Reduces logging");
			Logging.log("    disable_flood:        Disables flood protection");
			Logging.log("    info:                 Gives more information");
			Logging.log("    debug:                Gives debug info");
			Logging.log("    bufferlatency:        Gives more information");
			
					
					
			if(consoleInput) {
				System.exit(0);
			}
			return;

		} else {
			try {
				listenHostname = args[0];
				defaultHostname = args[1];
				for( int pos=2;pos<args.length;pos++) {

					if( args[pos].equals("verbose"))        Globals.setVerbose(true);
					else if( args[pos].equals("info"))           Globals.setInfo(true);
					else if( args[pos].equals("auth"))           Globals.setAuth(true);
					else if( args[pos].equals("auth_off"))       Globals.setAuth(false);
					else if( args[pos].equals("staticlocalhost"))  Globals.setVaryLocalhost(false);
					else if( args[pos].equals("rotatelocalhost"))  Globals.setVaryLocalhost(true);
					else if( args[pos].equals("debug"))          Globals.setDebug(true);
					else if( args[pos].equals("bridge_connection")) Globals.setBridgingConnection(true);
					else if( args[pos].equals("clientversion")){ Globals.setClientVersion(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("password"))     { Globals.setPassword(args[pos+1]); pos++;}
					else if( args[pos].equals("quiet"))          Globals.setQuiet(true);
					else if( args[pos].equals("disable_flood")) Globals.setFlood(false);
					else if( args[pos].equals("cache_limit"))   { Globals.setCacheLimit(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("reconnectfile")){ ReconnectCache.init(args[pos+1]); pos++;}
					else if( args[pos].equals("banned"))       { BanList.init(args[pos+1]); pos++;}
					else if( args[pos].equals("local_cache"))  { Globals.setlocalCache(true); }
					else if( args[pos].equals("compress_info")){ Globals.setCompressInfo(true);}
					else if( args[pos].equals("dimension"))       { Globals.setDimension(Byte.parseByte(args[pos+1])); pos++;}
					else if( args[pos].equals("monitor"))       { Globals.setMonitor(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("bufferlatency"))       { Globals.setBufferLatency(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("seed"))       { Globals.setSeed(Long.parseLong(args[pos+1])); pos++;}
					else if( args[pos].equals("log"))              { Logging.setFilename(args[pos+1]) ; pos++;}
					else                                        {System.out.println("Unknown field: " + args[pos]); System.exit(0);}

				}

			} catch (NumberFormatException nfe) {
				Logging.log( "Unable to parse numbers");
				Logging.log( "Usage: " + usageString );
				System.exit(0);
				return;
			}
		}

		if( !Globals.isAuth() ) {
			Logging.log( "" );
			Logging.log( "WARNING: You have not enabled player name authentication");
			Logging.log( "WARNING: This means that player logins are not checked with the minecraft server");
			Logging.log( "" );
			Logging.log( "To enable name authentication, add auth to the command line" );
			Logging.log( "" );
		} else {
			Logging.log( "Name authentication enabled");
		}

		if( !ReconnectCache.isSet() ) {
			Logging.log( "WARNING: reconnectfile parameter not set");
			Logging.log( "WARNING: players will be connected to the default server regardless of last server connected to");
		}
		
		cacheDir.mkdirs();

		Logging.log( "Use \"end\" to stop the server");

		server = new ProxyListener( listenHostname, defaultHostname );

		server.start();

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		if(consoleInput) {
			try {
				while( !in.readLine().equals("end") ) {
				}
			} catch (IOException e) {
			}

			try {
				in.close();
			} catch (IOException e) {
			}
			
			ReconnectCache.save();
			server.interrupt();
		} else {
			Logging.log("[CraftProxy-Lite] Server console disabled");
			boolean enabled = true;
			while(enabled && server.isAlive()) {
				try {
					server.join();
				} catch (InterruptedException ie) {
					server.interrupt();
					enabled = false;
				}
				ReconnectCache.save();
			}
		}
		
		Logging.log("Waiting for server to close");
		server.interrupt();
		try {
			server.join();
		} catch (InterruptedException e) {
			Logging.log("Server interrupted while closing");
		}
		ReconnectCache.save();
		Logging.flush();
		
		if(Main.craftGUI != null) {
			craftGUI.safeSetStatus("Server Stopped");
			craftGUI.safeSetButton("Start");
		}

	}

	public static void killServer() {

		Logging.log("Killing server from Bukkit");

		if(server != null) {
			server.interrupt();
		}
		
		try {
			server.join();
		} catch (InterruptedException e) {
			Logging.log("Server did not correctly shut down");
		}

	}


}

