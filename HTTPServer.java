import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;

public class HTTPServer {
	ServerSocket server;
	ServerSocketChannel serverChannel;
	List<Client> connected = new ArrayList<Client>();

	//defaults
	public static final int DEFAULT_PORT = 8000;
	public static final int MAX_CONNECTIONS = 4;
	private static final Map<String, TokenPair> TOKEN_CACHE = new HashMap<String, TokenPair>();

	public HTTPServer() throws IOException {
		this(DEFAULT_PORT);
	}

	public HTTPServer(int port) throws IOException {
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.bind(new InetSocketAddress(port));
		this.server = serverChannel.socket();

		System.out.println("Server started at " + serverChannel.getLocalAddress());
	}

	public void run() {
		try {
			while (true) channelConnected(serverChannel.accept());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void channelConnected(SocketChannel channel) throws IOException {
		if (connected.size() == MAX_CONNECTIONS) channelDisconnected(channel, "MAX_CONNECTIONS");
		else {
			Client client = new Client(this, channel);
			notifyConnect(channel);
			connected.add(client);

			client.start();
		}
	}

	public void channelDisconnected(SocketChannel channel) throws IOException {
		channelDisconnected(channel, "CHANNEL_DISCONNECTED");
	}

	public void channelDisconnected(SocketChannel channel, String reason) throws IOException {
		notifyDisconnect(channel, reason);
		connected.remove(channel);

		channel.close();
	}

	public void notifyConnect(SocketChannel channel) {
		try {
			log(((InetSocketAddress)channel.getRemoteAddress()).getHostName() + " connected: [" + channel.getRemoteAddress() + ", " + ((InetSocketAddress)channel.getRemoteAddress()).getPort() + "]");
		} catch (IOException e) {

		} 
	}

	public void notifyDisconnect(SocketChannel channel, String reason) {
		try {
			String message = ((InetSocketAddress)channel.getRemoteAddress()).getHostName() + " disconnected: [" + channel.getRemoteAddress() + ", " + ((InetSocketAddress)channel.getRemoteAddress()).getPort() + "]";
			if (reason != "") message += " (REASON: " + reason + ")";
			log(message);
		} catch (IOException e) {

		} 
	}

	public TokenPair generateTokenPair(String consumer_name) {
		if (TOKEN_CACHE.containsKey(consumer_name)) return TOKEN_CACHE.get(consumer_name);
		String token = DigestUtils.md5Hex(consumer_name + System.nanoTime());
		String secret = DigestUtils.md5Hex(consumer_name + System.nanoTime() + token);
		
		TokenPair tokenPair = new TokenPair(token, secret);
		this.TOKEN_CACHE.put(consumer_name, tokenPair);
		return tokenPair;
	}

	public boolean authenticate(String consumer_name, TokenPair authToken) {
		if (TOKEN_CACHE.containsKey(consumer_name)) {
			if (TOKEN_CACHE.get(consumer_name).equals(authToken)) return true;
		}

		return false;
	}

	public void log(String message) {
		System.out.println("LOG: " + message);
	}
}