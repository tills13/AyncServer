import java.io.IOException;

public class TestServer {
	public static void main(String [] args) {
		try {
			HTTPServer server = new HTTPServer();
			server.run();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
	}
}