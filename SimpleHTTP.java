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

	SimpleHandler handler01 = new SimpleHandler("nihao");
	SimpleHandler handler02 = new SimpleHandler("FUCKYOU");
	server.createContext("/nm/", handler02);
	server.createContext("/", handler01);

	server.setExecutor(null);
	server.start();

	System.out.println("Server is running!");
    }

    public static class SimpleHandler implements HttpHandler {
	public String responseText;

	public SimpleHandler(String responseText) {
	    this.responseText = responseText;
	}
	
	@Override
	public void handle (HttpExchange exchange) throws IOException{
	    byte[] responseBytes = responseText.getBytes();
	    exchange.sendResponseHeaders(200, responseBytes.length);
	    try(OutputStream os = exchange.getResponseBody()) {
		os.write(responseBytes);
	    }
	}
    }
}

