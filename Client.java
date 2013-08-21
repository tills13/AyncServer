import java.nio.channels.SocketChannel;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;

public class Client extends Thread {
	private SocketChannel channel;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private boolean active;
	private HTTPServer server;

	public Client(HTTPServer server, SocketChannel channel) throws IOException {
		this.channel = channel;
		this.out = new ObjectOutputStream(channel.socket().getOutputStream());
		this.in = new ObjectInputStream(channel.socket().getInputStream());
		this.server = server;
	}

	public void run() {
		this.active = true;
		listen();
	}

	private void listen() {
		String disconnectMessage = "";
		while (this.active) {
			try {
				NetRequest request = (NetRequest) in.readObject();
				write(handleRequest(request));				
			} catch (IOException e) {
				disconnectMessage = "CHANNEL_DISCONNECTED";
				this.active = false;
			} catch (ClassNotFoundException e) {
				server.log("channel read error: " + this.channel);
				disconnectMessage = "CHANNEL_READ_ERROR";
				this.active = false;
			} 

			if (!this.active) {
				try {
					server.channelDisconnected(channel, disconnectMessage);
				} catch (IOException e) {
					server.log("error: " + this.channel + " - " + e.getMessage());
				}
			}
		}
	}

	public NetResponse handleRequest(NetRequest request) {
		NetResponse response = new NetResponse();
		switch(request.getType()) {
			case 1:
				//request an authtoken
				if (request.getField("password").equals("password")) {
					response.addField("type", "token-request-success");
                    TokenPair token = server.generateTokenPair(getConsumerName());
                    if (token == null) return generateFailure("could not generate token pair");
                    response.setToken(token);
                    server.log("generated token for: " + this.channel);
				} else {
					return generateFailure("incorrect password");
				}

				break;
			case 2: 
				if (server.authenticate(getConsumerName(), request.getToken())) {
					//if (request.getField("data-type").equals("active-connections")) response.addField("active-connections", server.connected.size() + "");
					response.addField("active-connections", server.connected.size() + "");
				} else {
					server.log("invalid token: " + this.channel + " " + request.getField("type"));
					return generateFailure("invalid token");
				}
				break;
			case 3:
				System.out.println("case 3");
				break;
			case 4: 
				System.out.println("case 4");
				break;
			case 5: 

		}

		return response;
	}

	public NetResponse generateFailure(String reason) {
		NetResponse response = new NetResponse();
		response.addField("fail", reason);

		return response;
	}

	public Socket getSocket() {
		return this.getChannel().socket();
	}

	public SocketChannel getChannel() {
		return this.channel;
	}

	public String getConsumerName() {
		try {
			return ((InetSocketAddress)getChannel().getRemoteAddress()).getHostName();
		} catch (IOException e) {

		}

		return "";
	}

	public boolean getActive() {
		return this.active;	
	}

	public boolean write(Object object) {
		try {
			this.out.writeObject(object);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}