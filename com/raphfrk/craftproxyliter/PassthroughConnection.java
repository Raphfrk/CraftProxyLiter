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

import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.Packet09Respawn;
import com.raphfrk.protocol.Packet10Holding;
import com.raphfrk.protocol.PacketFFKick;

public class PassthroughConnection extends KillableThread {

	public final ConnectionInfo connectionInfo = new ConnectionInfo();
	final DateFormat shortTime = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	final Socket clientSocket;
	final String defaultHostname;
	final String listenHostname;
	final FairnessManager fairnessManager;
	public final ProxyListener proxyListener;
	public final String IPAddress;
	private final MyPropertiesFile hostnameMap;

	PassthroughConnection(Socket clientSocket, String address, String hostname, String listenHostname, FairnessManager fairnessManager, ProxyListener proxyListener, MyPropertiesFile hostnameMap) {

		this.clientSocket = clientSocket;
		this.listenHostname = listenHostname;
		defaultHostname = hostname;
		connectionInfo.setIP(clientSocket.getInetAddress().getHostAddress());
		connectionInfo.setPort(clientSocket.getPort());
		this.fairnessManager = fairnessManager;
		this.proxyListener = proxyListener;
		setName("Passthrough connection - " + System.currentTimeMillis());
		this.IPAddress = address;
		this.hostnameMap = hostnameMap;

	}

	ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<String>();

	public void run() {

		// Open Packet Streams
		LocalSocket clientLocalSocket = new LocalSocket(clientSocket, this, Globals.getMaxWorldHeight());

		if(!clientLocalSocket.success) {
			printLogMessage("Unable to open data streams for client socket");
			return;
		}

		// Get username/handshake packet
		String reply;

		String hostnameForPing = RedirectManager.getNextHostname(listenHostname, defaultHostname);
		Integer portnumForPing = RedirectManager.getNextPort(listenHostname, defaultHostname);
		
		reply = LoginManager.getUsername(clientLocalSocket, connectionInfo, this, hostnameForPing, portnumForPing);

		if(reply != null) {
			sendKickMessageAndClose(clientLocalSocket, reply);
			clientLocalSocket.closeSocket(this);
			return;
		}

		if(BanList.banned(connectionInfo.getUsername())) {
			String message = BanList.getWhiteList()?
					"You are not on the proxy server's white list":
					"You are on the proxy server's ban list";
			sendKickMessageAndClose(clientLocalSocket, message);
			clientLocalSocket.closeSocket(this);
			return;
		}

		if(!BanList.getWhiteList()) {
			if(BanList.banned(connectionInfo.getIP())) {
				String message = "Your IP is on the proxy server's IP ban list";
				sendKickMessageAndClose(clientLocalSocket, message);
				clientLocalSocket.closeSocket(this);
				return;
			}
		}

		// Find if there is any entry in the reconnect cache for player

		String reconnectUsername = connectionInfo.getUsername();

		String hostname = defaultHostname;
		String[] split = connectionInfo.getUsernameRaw().split(";");
		if (split.length >= 2) {
			split = split[1].split(":");
			reconnectUsername = connectionInfo.getUsername() + ";" + split[0];
			String cached = ReconnectCache.get(reconnectUsername);
			hostname = hostnameMap == null ? null : hostnameMap.getString(split[0], "");
			if (cached.length() > 0) {
				hostname = cached;
				this.printLogMessage("Using cached hostname: " + hostname);
			} else if (hostname == null || hostname.equals("")) {
				hostname = defaultHostname;
			} else {
				this.printLogMessage("Using hostname map: " + split[0] + " -> " + hostname);
			}
		} else {
			this.printLogMessage("Improper username from client no hostname provided");
			hostname = defaultHostname;
		}
		
		if(connectionInfo.getHostname() == null) {
			connectionInfo.setHostname(hostname);
		}
		connectionInfo.redirect = true;

		boolean firstConnection = true;

		while(connectionInfo.redirect) {

			// Extract connection next connection info from the host name

			String nextHostname = RedirectManager.getNextHostname(listenHostname, connectionInfo.getHostname());
			Integer nextPortnum = RedirectManager.getNextPort(listenHostname, connectionInfo.getHostname());

			Boolean isNextProxy = RedirectManager.isNextProxy(listenHostname, connectionInfo.getHostname());
			String fullHostname = (isNextProxy != null && isNextProxy)?connectionInfo.getHostname():null;

			// Connect to server

			printLogMessage("Connecting to : " + nextHostname + " " + nextPortnum);
			Socket serverSocket = LocalSocket.openSocket(nextHostname, nextPortnum, this);

			if(serverSocket == null) {
				printLogMessage("Unable to open server socket");
				ReconnectCache.remove(reconnectUsername);
				this.sendKickMessageAndClose(clientLocalSocket, "Unable to open socket to Minecraft server");
				return;
			}

			LocalSocket serverLocalSocket = new LocalSocket(serverSocket, this, Globals.getMaxWorldHeight());

			if(!serverLocalSocket.success) {
				printLogMessage("Unable to open server socket data streams");
				ReconnectCache.remove(reconnectUsername);
				this.sendKickMessageAndClose(clientLocalSocket, "Unable to open socket streams to Minecraft server");
				serverLocalSocket.closeSocket(this);
				return;

			} else {
				printLogMessage("Connection successful");
			}
			
			for (Packet p : connectionInfo.loginCustomPackets) {
				try {
					serverLocalSocket.pout.sendPacket(p);
				} catch (IOException e) {
					printLogMessage("Unable to send custom packet to server");
					this.sendKickMessageAndClose(clientLocalSocket, "Unable to send custom packet to server");
					serverLocalSocket.closeSocket(this);
				}
			}
			
			connectionInfo.loginCustomPackets.clear();

			// Complete login process

			reply = LoginManager.bridgeLogin(clientLocalSocket, serverLocalSocket, connectionInfo, this, !firstConnection, fullHostname);

			if(reply != null) {
				printLogMessage("Login failed: " + reply);
				ReconnectCache.remove(reconnectUsername);
				sendKickMessageAndClose(clientLocalSocket, reply);
				clientLocalSocket.closeSocket(this);
				serverLocalSocket.closeSocket(this);
				return;
			} else {
				printLogMessage("Server login successful");
			}
			
			if(connectionInfo.holding != 0) {
				Packet10Holding holdingPacket = new Packet10Holding(connectionInfo.holding);
				try {
					serverLocalSocket.pout.sendPacket(holdingPacket);
				} catch (IOException e) {
					printLogMessage("Unable to send holding update packet");
					kill();
				}
			}
			
			if(!firstConnection) {
				byte otherDimension = (byte)((connectionInfo.loginDimension == 0) ? -1 : 0); 
				Packet09Respawn otherDimensionPacket = new Packet09Respawn(otherDimension, (byte)2, (byte)0, (short)Globals.getMaxWorldHeight(), "DEFAULT");
				Packet09Respawn dimensionPacket = new Packet09Respawn(connectionInfo.loginDimension, connectionInfo.loginUnknownRespawn, connectionInfo.loginCreative, connectionInfo.loginHeight, connectionInfo.levelType);
				try {
					clientLocalSocket.pout.sendPacket(otherDimensionPacket);
					clientLocalSocket.pout.sendPacket(dimensionPacket);
				} catch (IOException e) {
					printLogMessage("Unable to send dimension setup packet");
					kill();
				}
			}
			
			firstConnection = false;
			connectionInfo.redirect = false;
			
			ReconnectCache.store(reconnectUsername, connectionInfo.getHostname());

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
				if(killed() || (!StCBridge.isAlive())) {
					CtSBridge.interrupt();
				}
				if(killed() || (!CtSBridge.isAlive())) {
					StCBridge.interrupt();
				}
			}



			serverLocalSocket.closeSocket(this);
			printLogMessage("Closed connection to server");
		}
		clientLocalSocket.closeSocket(this);
		printLogMessage("Closed connection to client");		

		if(Main.craftGUI != null) {
			Main.craftGUI.safeSetStatus("<html>Disconnected from server<html>");
		}

	}

	public synchronized void printLogMessage(String message) {
		String username = (connectionInfo==null)?null:(connectionInfo.getUsername());
		String timeString = (Globals.logTime())?("[" + shortTime.format(new Date()) + "] "):"";
		if(username == null) {
			Logging.log(timeString + connectionInfo.getIP() + "/" + connectionInfo.getPort() + ": " + message);
		} else {
			Logging.log(timeString + connectionInfo.getIP() + "/" + connectionInfo.getPort() + " (" + username + "): " + message);
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
