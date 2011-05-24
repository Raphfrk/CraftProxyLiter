package com.raphfrk.craftproxyliter;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;

import com.raphfrk.protocol.Packet;
import com.raphfrk.protocol.Packet01Login;
import com.raphfrk.protocol.Packet02Handshake;

public class LoginManager {

	public static String getUsername(LocalSocket clientSocket, ConnectionInfo info, PassthroughConnection ptc) {
		Packet packet = new Packet();

		try {
			packet = clientSocket.pin.getPacket(packet);
			if(packet == null) {
				return "Client didn't send handshake packet";
			}
		} catch (EOFException eof) {
			return "Client closed connection before sending handshake";
		} catch (IOException ioe) {
			return "IO Error reading client handshake";
		}

		if(packet.getByte(0) == 0x02) {
			Packet02Handshake CtSHandshake = new Packet02Handshake(packet);
			info.setUsername(CtSHandshake.getUsername());
		} else if (packet.getByte(0) == 0x52){
			Packet52ProxyLogin proxyLogin = new Packet52ProxyLogin(packet);
			info.setUsername(proxyLogin.getUsername());
			info.setHostname(proxyLogin.getHostname());
			info.forwardConnection = true;
			ptc.printLogMessage("Proxy to proxy connection received, forwarding to " + ptc.connectionInfo.getHostname());
		} else {
			return "Unknown login packet id " + packet.getByte(0);
		}

		return null;

	}

	public static String bridgeLogin(LocalSocket clientSocket, LocalSocket serverSocket, ConnectionInfo info, PassthroughConnection ptc, boolean reconnect, String fullHostname) {

		Packet packet = new Packet();

		Packet CtSHandshake;
		
		String password = Globals.getPassword();
		
		if(fullHostname == null || password == null) {
			if(fullHostname != null) {
				ptc.printLogMessage("WARNING: attempting to log into another proxy which has authentication enabled but password has not been set");
			}
			ptc.printLogMessage("Connecting using proxy to server connection format");
			CtSHandshake = new Packet02Handshake(info.getUsername());
		} else {
			ptc.printLogMessage("Connecting using proxy to proxy connection format");
			CtSHandshake = new Packet52ProxyLogin("", fullHostname, info.getUsername());
		}

		try {
			if(serverSocket.pout.sendPacket(CtSHandshake) == null) {
				return "Server didn't accept handshake packet";
			}
		} catch (EOFException eof) {
			return "Server closed connection before accepting handshake";
		} catch (IOException ioe) {
			return "IO Error sending client handshake to server";
		}

		try {
			packet = serverSocket.pin.getPacket(packet);
			if(packet == null) {
				return "Server didn't send handshake packet";
			}
		} catch (EOFException eof) {
			return "Server closed connection before sending handshake";
		} catch (IOException ioe) {
			return "IO Error reading server handshake";
		}

		Packet02Handshake StCHandshake = new Packet02Handshake(packet);

		String hash = StCHandshake.getUsername();

		if(fullHostname != null) {
			if(password == null) {
				ptc.printLogMessage("WARNING: attempting to log into another proxy which has authentication enabled but password has not been set");
			} else {
				String confirmCode = sha1Hash(password + hash);
				System.out.println("Sending code: " + confirmCode);
				Packet code = new Packet52ProxyLogin(confirmCode, info.getHostname(), info.getUsername());
				System.out.println("Sent 0x52 packet");
				try {
					if(serverSocket.pout.sendPacket(code) == null) {
						return "Server refused password packet";
					} 
				} catch (EOFException eof) {
					return "Server closed connection before accepting password packet";
				} catch (IOException ioe) {
					return "IO Error sending password packet";
				}
			}
		}

		String expectedCode = null;
		if(Globals.isAuth()) {
			hash = getHashString();
			StCHandshake = new Packet02Handshake(hash);
			expectedCode = sha1Hash(password + hash);
		}
		
		boolean passwordAccepted = false;

		if(!reconnect) {
			try {
				if(clientSocket.pout.sendPacket(StCHandshake) == null) {
					return "Client didn't accept handshake packet";
				}
			} catch (EOFException eof) {
				return "Client closed connection before accepting handshake";
			} catch (IOException ioe) {
				return "IO Error sending server handshake";
			}
			
			try {
				packet = clientSocket.pin.getPacket(packet);
				if(packet == null) {
					return "Client didn't send login packet";
				}
				info.clientVersion = packet.getInt(1);
			} catch (EOFException eof) {
				return "Client closed connection before sending login";
			} catch (IOException ioe) {
				return "IO Error reading client login";
			}
			
			if(packet.getByte(0) == 0x52) {
				Packet52ProxyLogin proxyLogin = new Packet52ProxyLogin(packet);
				if(proxyLogin.getCode().equals(expectedCode)) {
					passwordAccepted = true;
					try {
						packet = clientSocket.pin.getPacket(packet);
						if(packet == null) {
							return "Client didn't send login packet";
						}
						info.clientVersion = packet.getInt(1);
					} catch (EOFException eof) {
						return "Client closed connection before sending login";
					} catch (IOException ioe) {
						return "IO Error reading client login";
					}
				} else {
					ptc.printLogMessage("Expected: " + expectedCode);
					ptc.printLogMessage("Received: " + proxyLogin.getCode());
					return "Attemped password login failed";
				}
			}
			
		} else {
			String username = info.getUsername();
			packet = new Packet(100);
			packet.writeByte((byte)0x01);
			packet.writeInt(info.clientVersion);
			packet.writeString16(username.substring(0,Math.min(16, username.length())));
			packet.writeLong(0);
			packet.writeByte((byte)0);	
		}

		Packet01Login CtSLogin = new Packet01Login(packet);

		try {
			if(serverSocket.pout.sendPacket(CtSLogin) == null) {
				return "Server didn't accept login packet";
			}
		} catch (EOFException eof) {
			return "Server closed connection before accepting login";
		} catch (IOException ioe) {
			return "IO Error sending client login to server";
		}

		try {
			packet = serverSocket.pin.getPacket(packet);
			if(packet == null) {
				return "Server didn't send login packet";
			}
		} catch (EOFException eof) {
			return "Server closed connection before sending login";
		} catch (IOException ioe) {
			return "IO Error reading server login";
		}

		if(!passwordAccepted && !reconnect && Globals.isAuth()) {
			if(!authenticate(ptc.connectionInfo.getUsername(), hash, ptc)) {
				return "Authentication failed";
			}
		}

		Packet01Login StCLogin = new Packet01Login(packet);	

		info.serverPlayerId = StCLogin.getVersion();

		if(!reconnect) {
			info.clientPlayerId = StCLogin.getVersion();
			try {
				if(clientSocket.pout.sendPacket(StCLogin) == null) {
					return "Client didn't accept login packet";
				}
			} catch (EOFException eof) {
				return "Client closed connection before accepting login";
			} catch (IOException ioe) {
				return "IO Error sending server login";
			}
		}

		return null;

	}

	static SecureRandom hashGenerator = new SecureRandom();

	static String getHashString() {
		long hashLong;
		synchronized( hashGenerator ) {
			hashLong = hashGenerator.nextLong();
		}

		return Long.toHexString(hashLong);
	}

	static boolean authenticate( String username , String hashString, PassthroughConnection ptc )  {

		try {
			String encodedUsername =  URLEncoder.encode(username, "UTF-8");
			String encodedHashString =  URLEncoder.encode(hashString, "UTF-8");
			String authURLString = new String( "http://www.minecraft.net/game/checkserver.jsp?user=" + encodedUsername + "&serverId=" + encodedHashString);
			if(!Globals.isQuiet()) {
				ptc.printLogMessage("Authing with " + authURLString);
			}
			URL minecraft = new URL(authURLString);
			URLConnection minecraftConnection = minecraft.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(minecraftConnection.getInputStream()));

			String reply = in.readLine();

			if( Globals.isInfo() ) {
				ptc.printLogMessage("Server Response: " + reply );
			}

			in.close();

			if( reply != null && reply.equals("YES")) {

				if(!Globals.isQuiet()) {
					ptc.printLogMessage("Auth successful");
				}
				return true;
			}
		} catch (MalformedURLException mue) {
			ptc.printLogMessage("Auth URL error");
		} catch (IOException ioe) {
			ptc.printLogMessage("Problem connecting to auth server");
		}

		return false;
	}

	static String sha1Hash( String inputString ) {

		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.reset();

			md.update(inputString.getBytes("utf-8"));

			BigInteger bigInt = new BigInteger( md.digest() );

			return bigInt.toString( 16 ) ;

		} catch (Exception ioe) {
			return "hash error";
		}

	}

}
