package com.raphfrk.craftproxyliter;

import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;

import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.PacketFFKick;

public class PassthroughConnection extends KillableThread {
	
	public final ConnectionInfo connectionInfo = new ConnectionInfo();
	final DateFormat shortTime = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	final Socket clientSocket;
	final String defaultHostname;
	final String listenHostname;
	final FairnessManager fairnessManager;
	final ProxyListener proxyListener;

	PassthroughConnection(Socket clientSocket, String hostname, String listenHostname, FairnessManager fairnessManager, ProxyListener proxyListener) {
		
		this.clientSocket = clientSocket;
		this.listenHostname = listenHostname;
		defaultHostname = hostname;
		connectionInfo.setIP(clientSocket.getInetAddress().getHostAddress());
		connectionInfo.setPort(clientSocket.getPort());
		this.fairnessManager = fairnessManager;
		this.proxyListener = proxyListener;
		setName("Passthrough connection - " + System.currentTimeMillis());
		
	}
	
	
	public void run() {
		
		// Open Packet Streams
		LocalSocket clientLocalSocket = new LocalSocket(clientSocket, this);
		
		if(!clientLocalSocket.success) {
			printLogMessage("Unable to open data streams for client socket");
			return;
		}
		
		// Get username/handshake packet
		String reply;
		
		reply = LoginManager.getUsername(clientLocalSocket, connectionInfo, this);
		
		if(reply != null) {
			sendKickMessageAndClose(clientLocalSocket, reply);
			clientLocalSocket.closeSocket(this);
			return;
		}
		
		// Find if there is any entry in the reconnect cache for player
		
		String cached = ReconnectCache.get(connectionInfo.getUsername());
		
		String hostname = (cached.length() > 0) ? cached : defaultHostname;
		
		connectionInfo.setHostname(hostname);
		
		// Extract connection next connection info from the host name
		
		String nextHostname = RedirectManager.getNextHostname(listenHostname, connectionInfo.getHostname());
		Integer nextPortnum = RedirectManager.getNextPort(listenHostname, connectionInfo.getHostname());
		
		// Connect to server
		
		Socket serverSocket = LocalSocket.openSocket(nextHostname, nextPortnum, this);
		
		if(serverSocket == null) {
			printLogMessage("Unable to open server socket");
			ReconnectCache.remove(connectionInfo.getUsername());
			this.sendKickMessageAndClose(clientLocalSocket, "Unable to open socket to Minecraft server");
			return;
		}
		
		LocalSocket serverLocalSocket = new LocalSocket(serverSocket, this);
		
		if(!serverLocalSocket.success) {
			printLogMessage("Unable to open server socket data streams");
			ReconnectCache.remove(connectionInfo.getUsername());
			this.sendKickMessageAndClose(clientLocalSocket, "Unable to open socket streams to Minecraft server");
			serverLocalSocket.closeSocket(this);
			return;
			
		} else {
			printLogMessage("Connection successful");
		}
		
		// Complete login process
		
		reply = LoginManager.bridgeLogin(clientLocalSocket, serverLocalSocket, connectionInfo, this);
		
		if(reply != null) {
			printLogMessage("Login failed: " + reply);
			sendKickMessageAndClose(clientLocalSocket, reply);
			clientLocalSocket.closeSocket(this);
			serverLocalSocket.closeSocket(this);
			return;
		} else {
			printLogMessage("Server login successful");
		}
		
		KillableThread StCBridge = new DownstreamBridge(serverLocalSocket.pin, clientLocalSocket.pout, this, fairnessManager);
		KillableThread CtSBridge = new UpstreamBridge(clientLocalSocket.pin, serverLocalSocket.pout, this, fairnessManager);
		
		StCBridge.setName("Server to client bridge");
		CtSBridge.setName("Client to server bridge");
		
		StCBridge.start();
		CtSBridge.start();
		
		while(StCBridge.isAlive() || CtSBridge.isAlive()) {
			try {
				StCBridge.join(500);
				CtSBridge.join(500);
			} catch (InterruptedException ie) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException ie2) {
				}
				this.interrupt();
			}
			if(!StCBridge.isAlive()) {
				CtSBridge.interrupt();
			}
			if(!CtSBridge.isAlive()) {
				StCBridge.interrupt();
			}
		}
		
		serverLocalSocket.closeSocket(this);
		printLogMessage("Closed connection to server");
		clientLocalSocket.closeSocket(this);
		printLogMessage("Closed connection to client");		
		
		if(Main.craftGUI != null) {
			Main.craftGUI.safeSetStatus("<html>Disconnected from server<html>");
		}
		
	}
	
	public synchronized void printLogMessage(String message) {
		String username = (connectionInfo==null)?null:(connectionInfo.getUsername());
		if(username == null) {
			Logging.log("[" + shortTime.format(new Date()) + "] " + connectionInfo.getIP() + "/" + connectionInfo.getPort() + ": " + message);
		} else {
			Logging.log("[" + shortTime.format(new Date()) + "] " + connectionInfo.getIP() + "/" + connectionInfo.getPort() + " (" + username + "): " + message);
		}
	}
	
	void sendKickMessageAndClose(LocalSocket socket, String message) {
		PacketFFKick kick = new PacketFFKick(message);
		try {
			socket.pout.sendPacket(kick);
			socket.pout.flush();
		} catch (IOException e) {
			printLogMessage("Unable to send Kick packet: " + message);
		}
		socket.closeSocket(this);
		return;
	}

}
