import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class App {
	static void main(String[] args) throws IOException {
		InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8000);
		HttpServer server = HttpServer.create(addr, 10);
		AuthSystem authsystem = new AuthSystem();
		// api handlers
		server.createContext("/api/check", new CheckHD());
		server.createContext("/api/auth/login", new LoginHD(authsystem));
		server.createContext("/api/auth/register", new RegisterHD(authsystem));
		server.createContext("/api/me", new MeHD(authsystem));
		server.createContext("/api/calldeepseek", new CallDeepseekHD());

		server.start();
		System.out.printf("Server is running on %s\n", addr.toString());
	}
}


class CheckHD implements HttpHandler {
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String response_text = "{\"content\": \"Hello World From OFT.\"}";
		byte[] bytes = response_text.getBytes();
		exchange.sendResponseHeaders(200, 0);

		String method = exchange.getRequestMethod();
		String uri = exchange.getRequestURI().getPath();
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
			System.out.printf("[Check] detect method: %s, uri: %s\n", method, uri);
		}
	}
}

