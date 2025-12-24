import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleHTTP {
    public static void main() throws IOException {
	InetSocketAddress addr = new InetSocketAddress(8000);
	HttpServer server = HttpServer.create(addr, 0);

	HttpHandler handler = new HttpHandler() {
		@Override
	        public void handle (HttpExchange exchange) throws IOException{
		    String response = "Hello World";
		    exchange.sendResponseHeaders(200, response.length());
		    try(OutputStream os = exchange.getResponseBody()) {
			os.write(response.getBytes());
		    }
		}
	};
	server.createContext("/", handler);

	server.setExecutor(null);
	server.start();

	System.out.println("Server is running!");
    }
}

