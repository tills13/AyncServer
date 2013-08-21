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

public class AsyncServer {
	ServerSocket server;
	ServerSocketChannel serverChannel;
	List<Channel> connected = new ArrayList<Channel>();

	//defaults
	public static final int DEFAULT_PORT = 8000;
	public static final int MAX_CONNECTIONS = 4;
	private static final Map<String, TokenPair> TOKEN_CACHE = new HashMap<String, TokenPair>();

	public AsyncServer() throws IOException {
		this(DEFAULT_PORT);
	}

	public AsyncServer(int port) throws IOException {
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.bind(new InetSocketAddress(port));
		this.server = serverChannel.socket();

		System.out.println("Server listening on " + serverChannel.getLocalAddress());
	}

	public void run() {
		try {
			while (true) channelConnected(serverChannel.accept());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void channelConnected(SocketChannel channel) throws IOException {
		Channel client = new Channel(this, channel);
		if (connected.size() == MAX_CONNECTIONS) channelDisconnected(client, "MAX_CONNECTIONS");
		else {
			notifyConnect(client);
			connected.add(client);

			client.start();
		}
	}

	public void channelDisconnected(Channel channel) throws IOException {
		channelDisconnected(channel, "CHANNEL_DISCONNECTED");
	}

	public void channelDisconnected(Channel channel, String reason) throws IOException {
		notifyDisconnect(channel, reason);
		connected.remove(channel);

		channel.getChannel().close();
	}

	public void notifyConnect(Channel channel) {
		log(channel.getConsumerName() + " connected: " + channel.getLongName());
	}

	public void notifyDisconnect(Channel channel, String reason) {
		log((reason == "") ? channel.getConsumerName() + " disconnected: " + channel.getLongName() : channel.getConsumerName() + " disconnected: " + channel.getLongName() + " (REASON: " + reason + ")");
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