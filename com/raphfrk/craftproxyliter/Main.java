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
			Logging.log("    adminname <name>      The user <name> can use the /memdump command to perform a heap dump");
			Logging.log("    command <command-name>Sets the command name for proxy commands /command-name");
			Logging.log("    reconnectfile <file>  Sets the reconnect file");
			Logging.log("    banned <file>         Sets the banned list file (one player per line)");
			Logging.log("    whitelist <file>      Sets the white list file (one player per line)");
			Logging.log("    log <file>            Redirects output to a log file");
			Logging.log("    dimension <num>       Sets the dimension (-1 = hell, 0=normal)");
			Logging.log("    seed <num>            Sets the world seed");
			Logging.log("    monitor <period ms>   Enables bandwidth use logging (not implemented)");
			Logging.log("    compress_info         Outputs info related to the compression/cache system");
			Logging.log("    local_cache           Puts the proxy in local cache mode");
			Logging.log("    bridge_connection     Authentication is handled by backend server (depreciated)");
			Logging.log("    cache_limit           Sets the max size of the cache");
			Logging.log("    quiet:                Reduces logging");
			Logging.log("    disable_flood:        Disables flood protection");
			Logging.log("    show_unallocated_warn:Shows a warning when chunks for unallocated columns are sent to the client");
			Logging.log("    info:                 Gives more information");
			Logging.log("    debug:                Gives debug info");
			Logging.log("    bufferlatency:        Sets buffers max latency");
			Logging.log("    timeout:              Sets the network timeout in seconds (default is 60 seconds)");
			Logging.log("    log_time_off:         Turns off time for logging");
			Logging.log("    max_height <height>:  Sizes internal buffers for a given max world height");

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
					else if( args[pos].equals("adminname"))     { Globals.setAdminName(args[pos+1]); pos++;}
					else if( args[pos].equals("command"))     { Globals.setCommand(args[pos+1]); pos++;}
					else if( args[pos].equals("show_unallocated_warn")) Globals.setShowUnallocatedChunkWarning(true);
					else if( args[pos].equals("quiet"))          Globals.setQuiet(true);
					else if( args[pos].equals("disable_flood")) Globals.setFlood(false);
					else if( args[pos].equals("cache_limit"))   { Globals.setCacheLimit(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("reconnectfile")){ ReconnectCache.init(args[pos+1]); pos++;}
					else if( args[pos].equals("banned"))       { BanList.init(args[pos+1], false); pos++;}
					else if( args[pos].equals("whitelist"))       { BanList.init(args[pos+1], true); pos++;}
					else if( args[pos].equals("local_cache"))  { Globals.setlocalCache(true); }
					else if( args[pos].equals("compress_info")){ Globals.setCompressInfo(true);}
					else if( args[pos].equals("dimension"))       { Globals.setDimension(Byte.parseByte(args[pos+1])); pos++;}
					else if( args[pos].equals("monitor"))       { Globals.setMonitor(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("bufferlatency"))       { Globals.setBufferLatency(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("compression_level"))       { Globals.setCompressionLevel(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("seed"))       { Globals.setSeed(Long.parseLong(args[pos+1])); pos++;}
					else if( args[pos].equals("blockredirects"))       { Globals.setAllowRedirect(false);}
					else if( args[pos].equals("log_time_off"))       { Globals.setLogTime(false);}
					else if( args[pos].equals("timeout"))       { Globals.setNetTimeout(Integer.parseInt(args[pos+1])); pos++;}
					else if( args[pos].equals("max_height"))       { Globals.setMaxWorldHeight(Integer.parseInt(args[pos+1])); pos++;}
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
			
			ReconnectCache.killThread();
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

