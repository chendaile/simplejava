package app;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import app.Controller.CheckHD;
import app.Controller.LoginHD;
import app.Controller.RegisterHD;
import app.Controller.MeHD;
import app.Controller.CallDeepseekHD;
import app.Service.AuthSystem;

public class MechHubApplication {
	public static void main(String[] args) throws IOException {
		InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8000);
		HttpServer server = HttpServer.create(addr, 10);
		AuthSystem authsystem = new AuthSystem();
		// api handlers
		server.createContext("/api/check", new CheckHD());
		server.createContext("/api/auth/login", new LoginHD(authsystem));
		server.createContext("/api/auth/register", new RegisterHD(authsystem));
		server.createContext("/api/auth/me", new MeHD(authsystem));
		server.createContext("/api/calldeepseek", new CallDeepseekHD());

		server.start();
		System.out.printf("Server is running on %s\n", addr.toString());
	}
}


