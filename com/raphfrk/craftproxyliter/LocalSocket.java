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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.raphfrk.craftproxyliter.Globals;
import com.raphfrk.craftproxyliter.LocalhostIPFactory;
import com.raphfrk.protocol.ProtocolInputStream;
import com.raphfrk.protocol.ProtocolOutputStream;

public class LocalSocket {

	public final boolean success;

	private final DataInputStream in;
	public final ProtocolInputStream pin;
	private final DataOutputStream out;
	public final ProtocolOutputStream pout;
	public final Socket socket;
	public final PassthroughConnection ptc;

	public static Socket openSocket(String hostname, int port, PassthroughConnection ptc) {

		ptc.printLogMessage("Attempting to connect to: " + hostname + ":" + port);
		
		Socket socket = null;

		try {
			if(hostname.trim().startsWith("localhost") && Globals.varyLocalhost()) {
				String fakeLocalIP = LocalhostIPFactory.getNextIP();
				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Connecting to: " + hostname + ":" + port + " from " + fakeLocalIP );
				}
				socket = new Socket(hostname, port, InetAddress.getByName(fakeLocalIP), 0);
			} else {
				socket = new Socket(hostname, port);
			}			
		} catch (UnknownHostException e) {
			ptc.printLogMessage("Unknown hostname: " + hostname);
			return null;
		} catch (IOException e) {
			if(hostname.trim().startsWith("localhost")) {
				ptc.printLogMessage("Trying alternative IPs on localhost, this is slow");
				List<String> hostnames = getLocalIPs();
				for(String h : hostnames) {
					ptc.printLogMessage("Attempting to connect to: " + h + ":" + port);
					try {
						socket = new Socket(h, port);
					} catch (IOException ioe) {
						continue;
					}
					ptc.printLogMessage("WARNING: Used alternative IP to connect: " + h);
					ptc.printLogMessage("WARNING: Used alternative IP to connect: " + h);
					ptc.printLogMessage("WARNING: Used alternative IP to connect: " + h);
					ptc.printLogMessage("WARNING: Used alternative IP to connect: " + h);
					ptc.printLogMessage("WARNING: Used alternative IP to connect: " + h);
					ptc.printLogMessage("WARNING: Used alternative IP to connect: " + h);
					ptc.printLogMessage("You should change your default server parameter to include the IP address: " + h);
					break;
				}
			}
			if(socket == null) {
				ptc.printLogMessage("Unable to open socket to " + hostname + ":" + port);
				return null;
			}
		}
		try {
			socket.setSoTimeout(Globals.getSOTimeout());
		} catch (SocketException e) {
			ptc.printLogMessage("Unable to set socket timeout");
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException ioe){
					return null;
				}
			}
			return null;
		}

		return socket;

	}

	public static List<String> getLocalIPs() {

		Enumeration<NetworkInterface> interfaces;

		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return null;
		}

		List<String> ips = new ArrayList<String>();

		while(interfaces.hasMoreElements()) {
			NetworkInterface current = interfaces.nextElement();

			if(current != null) {
				Enumeration<InetAddress> addresses = current.getInetAddresses();

				while(addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if(addr != null) {
						ips.add(addr.getHostAddress());
					}
				}
			}
		}

		return ips;

	}

	public boolean closeSocket(PassthroughConnection ptc) {

		try {
			pout.flush();
			pout.close();
			pin.close();
			socket.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	LocalSocket(Socket socket, PassthroughConnection ptc, int worldHeight) {
		this.ptc = ptc;
		this.socket = socket;
		DataInputStream inLocal = null;
		DataOutputStream outLocal = null;
		try {
			inLocal = new DataInputStream( socket.getInputStream() );
		} catch (IOException e) {
			ptc.printLogMessage("Unable to open data stream to client");
			if( inLocal != null ) {
				try {
					inLocal.close();
					socket.close();
				} catch (IOException e1) {
					ptc.printLogMessage("Unable to close data stream to client");
				}
			}
			in = null;
			pin = null;
			out = null;
			pout = null;
			success = false;
			return;
		}

		try {
			outLocal = new DataOutputStream( socket.getOutputStream() );
		} catch (IOException e) {
			ptc.printLogMessage("Unable to open data stream from client");
			if( outLocal != null ) {
				try {
					outLocal.close();
					socket.close();
				} catch (IOException e1) {
					ptc.printLogMessage("Unable to close data stream from client");
				}
			}
			in = null;
			pin = null;
			out = null;
			pout = null;
			success = false;
			return;
		}
		in = inLocal;
		pin = new ProtocolInputStream(in, worldHeight*1024);
		out = outLocal;
		pout = new ProtocolOutputStream(out);
		success = true;
	}

}
