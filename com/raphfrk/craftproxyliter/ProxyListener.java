package com.raphfrk.craftproxyliter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import com.raphfrk.compression.HashStore;
import com.raphfrk.protocol.KillableThread;
import com.raphfrk.protocol.PacketFFKick;
import com.raphfrk.protocol.ProtocolOutputStream;
import com.raphfrk.protocol.ProtocolUnitArray;

public class ProxyListener extends KillableThread {

	private final int port;
	private final InetAddress listenAddress;
	private final String listenHostname;
	private final String defaultHostname;
	private final FairnessManager fairnessManager = new FairnessManager();

	LinkedList<PassthroughConnection> connections = new LinkedList<PassthroughConnection>();

	ProxyListener(String listenHostname, String defaultHostname) {
		this.port = RedirectManager.getListenPort(listenHostname);
		this.listenAddress = RedirectManager.getListenHostname(listenHostname);
		this.listenHostname = listenHostname;
		this.defaultHostname = defaultHostname;
		setName("Proxy Listener");
	}

	public ConcurrentHashMap<String,Long> lastLogin = new ConcurrentHashMap<String,Long>();
	public ConcurrentHashMap<String,Long> lastLoginOld = new ConcurrentHashMap<String,Long>();
	public ConcurrentHashMap<String,Long> lastPing = new ConcurrentHashMap<String,Long>();
	
	HashStore hs = new HashStore(new File("CPL_cache"));
	
	@Override
	public void run() {
		
		
		
		// Add new packet types
		
		// Packet to send hash list to server
		ProtocolUnitArray.ops[0x50] = new ProtocolUnitArray.Op[] {ProtocolUnitArray.Op.JUMP_FIXED,  ProtocolUnitArray.Op.SHORT_SIZED};
		ProtocolUnitArray.params[0x50] = new int[] {1, 0};
		
		ProtocolUnitArray.ops[0x51] = new ProtocolUnitArray.Op[] {ProtocolUnitArray.Op.JUMP_FIXED,  ProtocolUnitArray.Op.INT_SIZED};
		ProtocolUnitArray.params[0x51] = new int[] {14, 0};
		
		ProtocolUnitArray.ops[0x52] = new ProtocolUnitArray.Op[] {ProtocolUnitArray.Op.JUMP_FIXED,  ProtocolUnitArray.Op.SHORT_SIZED_DOUBLED, ProtocolUnitArray.Op.SHORT_SIZED_DOUBLED, ProtocolUnitArray.Op.SHORT_SIZED_DOUBLED};
		ProtocolUnitArray.params[0x52] = new int[] {1, 0, 0, 0};

		ProtocolUnitArray.ops[0xC3] = new ProtocolUnitArray.Op[] {ProtocolUnitArray.Op.JUMP_FIXED, ProtocolUnitArray.Op.INT_SIZED};
		ProtocolUnitArray.params[0xC3] = new int[] {5, 0};
		
		ProtocolUnitArray.ops[0xE6] = new ProtocolUnitArray.Op[] {ProtocolUnitArray.Op.JUMP_FIXED, ProtocolUnitArray.Op.INT_SIZED_QUAD, ProtocolUnitArray.Op.INT_SIZED_QUAD, ProtocolUnitArray.Op.INT_SIZED_INT_SIZED_SINGLE};
		ProtocolUnitArray.params[0xE6] = new int[] {8, 0, 0, 0};
		
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(port, 0, listenAddress);
			listener.setSoTimeout(200);
		} catch (BindException be) {
			Logging.log( "ERROR: CraftProxy: Unable to bind to port");
			Logging.log( "ERROR: CraftProxy: Unable to bind to port");
			Logging.log( "ERROR: CraftProxy: Unable to bind to port");
			Logging.log( "ERROR: CraftProxy: Unable to bind to port");
			Logging.log( "ERROR: CraftProxy: Unable to bind to port");
			Logging.log( "ERROR: CraftProxy: Unable to bind to port");
			if(Main.craftGUI != null) {
				Main.craftGUI.safeSetStatus("<html>Unable to start server <br>Port " + port + " not free<html>");
			}
			if( listener != null ) {
				try {
					listener.close();
				} catch (IOException e) {
					Logging.log( "Unable to close connection");
				}
			}
			kill();
			interruptConnections();
			return;
		} catch (IOException ioe) {
			Logging.log("Unknown error");	
			ioe.printStackTrace();
			if( listener != null ) {
				try {
					listener.close();
				} catch (IOException e) {
					Logging.log( "Unable to close connection");
				}
			}
			return;
		} 

		if(listenAddress != null) {
			Logging.log("Server listening on: " + listenAddress + ":" + port);
		} else {
			Logging.log("Server listening on port: " + port);
		}
		if(Main.craftGUI != null) {
			Main.craftGUI.safeSetStatus("<html>Server Started<br>Connect to localhost:" + port + "</html>");
		}

		while(!killed()) {

			if(this.isInterrupted()) {
				System.out.println("Interrupted");
				kill();
			}

			Socket socket = null;

			try {
				socket = listener.accept();
			} catch (SocketTimeoutException ste ) {
				continue;
			} catch (IOException e) {
				Logging.log("Error waiting for connection");
				e.printStackTrace();
				continue;
			}
			if(socket == null) {
				continue;
			}

			try {
				socket.setSoTimeout(200);
			} catch (SocketException e) {
				Logging.log( "Unable to set timeout for socket");
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
						Logging.log("Unable to close connection");
					}
					continue;
				}

			}

			String address = socket.getInetAddress().getHostAddress().toString();
			int port = socket.getPort();
			Logging.log("Connection from " + address + "/" + port);
			long currentTime = System.currentTimeMillis();
			Long lastConnect = lastLogin.get(address);
			boolean floodProtection = !address.equals("127.0.0.1") && Globals.isFlood() && lastConnect != null && lastConnect + 5000 > currentTime;
			if (lastConnect != null) {
				lastLoginOld.put(address, lastConnect);
			} else {
				lastLoginOld.remove(address);
			}
			lastLogin.put(address, currentTime);
			if(floodProtection) {
				Logging.log("Disconnecting due to connect flood protection");
				try {
					DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
					ProtocolOutputStream packetOutputStream = new ProtocolOutputStream(outputStream);
					packetOutputStream.sendPacket(new PacketFFKick("Only one connection is allowed per IP every 5 seconds"));
					outputStream.flush();
					socket.close();
				} catch (IOException e) {
					Logging.log("Exception when closing connection");
				}
			} else {
				try {
					PassthroughConnection ptc = new PassthroughConnection(socket, address, defaultHostname,  listenHostname, fairnessManager, this);
					ptc.start();
					if(Main.craftGUI != null) {
						Main.craftGUI.safeSetStatus("Client connected: " + address + "/" + port);
					}
					addPassthroughConnection(ptc);
				} catch (Exception e) {
					kill();
					e.printStackTrace();
				}
			}


		}

		if(listener!=null) {
			try {
				listener.close();
			} catch (IOException ioe) {
				System.out.println("Unable to close socket");
			}
		}
		
		if(Globals.localCache()) {
			hs.flushPending(true);
			hs.writeFAT();
		}
		
		/*if(fairnessManager != null) {
			System.out.println("Killing fairness manager");
			fairnessManager.killTimerAndJoin();
			System.out.println("Fairness manager killed successfully");
		}*/

		interruptConnections();		
	}

	void addPassthroughConnection(PassthroughConnection ptc) {
		Iterator<PassthroughConnection> itr = connections.iterator();

		while(itr.hasNext()) {
			if(!itr.next().isAlive()) {
				itr.remove();
			}
		}

		connections.add(ptc);
	}

	void interruptConnections() {
		Iterator<PassthroughConnection> itr = connections.iterator();

		while(itr.hasNext()) {
			PassthroughConnection ptc = itr.next();
			ptc.interrupt();
		}

		itr = connections.iterator();

		while(itr.hasNext()) {
			PassthroughConnection ptc = itr.next();
			try {
				ptc.join();
			} catch (InterruptedException e) {
				ptc.printLogMessage("Unable to break connection");
				kill();
			}
		}
	}

}
