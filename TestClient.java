import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class TestClient {
	public static TokenPair token;

	public static void main(String [] args) {
		if (args.length != 2) {
            System.err.println("Usage: " + TestClient.class.getSimpleName() + " <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

		Scanner in = new Scanner(System.in);
		try {
			SocketChannel channel = SocketChannel.open();
			channel.connect(new InetSocketAddress(host, port));
			channel.finishConnect();

			Socket socket = channel.socket();
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in2 = new ObjectInputStream(socket.getInputStream());

			while(true) {
				try {
					NetRequest request = new NetRequest();
					NetResponse response = null;
					System.out.print("> "); 
					String message = in.next();
					if (message.equals("close")) break;
					else if (message.equals("1")) {
						request.addField("type", "token-request");
						request.addField("password", "password");

						out.writeObject(request);
						response = (NetResponse) in2.readObject();

						token = response.getToken();
					} else if (message.equals("2")) {
						request.addField("type", "token-request");
						request.addField("password", "2");

						out.writeObject(request);
						response = (NetResponse) in2.readObject();
					} else if (message.equals("3")) {
						request.addField("type", "data-request");
						request.addField("data-type", "active-connections");
						System.out.println(token);
						request.setToken(token);

						out.writeObject(request);
						response = (NetResponse) in2.readObject();
					} 
					
					System.out.println(response);
				} catch (Exception e) {

				}
			}

			socket.close();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		} 
	}
}