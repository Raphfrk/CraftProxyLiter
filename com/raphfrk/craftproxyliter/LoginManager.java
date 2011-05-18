package com.raphfrk.craftproxyliter;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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

		Packet02Handshake CtSHandshake = new Packet02Handshake(packet);
		info.setUsername(CtSHandshake.getUsername());

		return null;

	}

	public static String bridgeLogin(LocalSocket clientSocket, LocalSocket serverSocket, ConnectionInfo info, PassthroughConnection ptc, boolean reconnect) {

		Packet packet = new Packet();

		Packet02Handshake CtSHandshake = new Packet02Handshake(info.getUsername());

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
		if(Globals.isAuth()) {
			hash = getHashString();
			StCHandshake = new Packet02Handshake(hash);
		}
		
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

		if(Globals.isAuth()) {
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

}
