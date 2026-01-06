import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class App{
    public static void main(String[] args) throws IOException{
	InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8000); 
	HttpServer server = HttpServer.create(addr, 10);
	AuthSystem authsystem = new AuthSystem();
	server.createContext("/", new HelloHD());
	server.createContext("/favicon.ico", new IconHD());
	server.createContext("/auth/login", new LoginHD(authsystem));
	server.createContext("/auth/register", new RegisterHD(authsystem));
	
	server.start();
	System.out.printf("Server is running on %s\n", addr.toString());
    }
}

public class HelloHD implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
	File index_file = new File("www/index.html");
	byte[] index_bytes = Files.readAllBytes(index_file.toPath());
	exchange.sendResponseHeaders(200, 0);

	String method = exchange.getRequestMethod();
	String uri = exchange.getRequestURI().getPath();
	try (OutputStream os = exchange.getResponseBody()) {
	    os.write(index_bytes);
	    System.out.printf("[Helloworld] detect method: %s, uri: %s\n", method, uri);   
	}
    }   
}

public class IconHD implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
	File icon_file = new File("www/icon.png");
	if (icon_file.exists()) {
	    byte[] icon_bytes = Files.readAllBytes(icon_file.toPath());
	    exchange.sendResponseHeaders(200, 0);

	    String method = exchange.getRequestMethod();
	    String uri = exchange.getRequestURI().getPath();
	    try (OutputStream os = exchange.getResponseBody()) {
		os.write(icon_bytes);
		System.out.printf("[Icon] detect method: %s, uri: %s\n", method, uri);   
	    }
	} else {
	    exchange.sendResponseHeaders(404, 0);
	    System.out.printf("[Icon] Icon not exist.\n");
	}
    }
}
