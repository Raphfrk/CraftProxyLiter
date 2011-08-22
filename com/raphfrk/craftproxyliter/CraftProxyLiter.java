package com.raphfrk.craftproxyliter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftProxyLiter extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");

	File pluginDirectory;
	
	Server server;

	PluginManager pm;
	
	Thread mainThread;
	
	public void onEnable() {

		pm = getServer().getPluginManager();
		server = getServer();

		pluginDirectory = this.getDataFolder();

		String singleOptions[] = new String[] {
			    "auth_off",
			    "log_time_off",
			    "staticlocalhost",
			    "bridge_connection",
			    "quiet",
			    "info",
			    "debug",
			    "disable_flood"
		};
			
		String doubleOptions[] = new String[] {
			    "dimension",
			    "seed",
			    "log",
			    "clientversion",
			    "monitor",
			    "password",
			    "reconnectfile",
			    "banned",
			    "cache_limit",
			    "bandwidth_limit",
			    "whitelist"
		};
		
		if( !pluginDirectory.exists() ) {
			pluginDirectory.mkdirs();
		}

		MyPropertiesFile pf;
		try {
			pf = new MyPropertiesFile(new File( pluginDirectory , "proxylite.txt").getCanonicalPath());
		} catch (IOException e) {
			return;
		}

		
		pf.load();
		
		ArrayList<String> args = new ArrayList<String>();
		
		args.add(pf.getString("listen_hostname_port", "20000"));
		args.add(pf.getString("default_server", "25565"));
		
		for(String current : singleOptions) {
			Boolean temp = current.equals("log_time_off") || current.equals("auth_off");

			temp = pf.getBoolean(current, temp);
			if(temp) {
				args.add(current);
			}
			if(current.equals("staticlocalhost") && !temp) {
				args.add("rotatelocalhost");
			}
		}
		
		for(String current : doubleOptions) {
			String temp = pf.getString(current, "");
			if(!(temp.trim()).equals("")) {
				args.add(current);
				args.add(temp);
			}
		}

		pf.save();
		
		final String[] argsArray = args.toArray(new String[0]);
		
		log.info("CraftProxy-Liter started in plugin mode");
		log.info("Command line args: " + Arrays.toString(argsArray));
		
		mainThread = new Thread(new Runnable() {
			public void run() {
				Main.main(argsArray, false);
			}
		});
		
		mainThread.start();
		
		
	}


	public void onDisable() {
		Main.killServer();
	}
}

