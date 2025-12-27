import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
    
public class SimpleHTTP {
    public static void main() throws IOException {
	InetSocketAddress addr = new InetSocketAddress(8000);
	HttpServer server = HttpServer.create(addr, 0);

	SimpleHandler FYhandler = new SimpleHandler("FUCKYOU");
	DBHandler DBhandler = new DBHandler("jdbc:mysql://8.162.3.110:3306/mysql?user=root&password=password");
	server.createContext("/", FYhandler);
	server.createContext("/db/", DBhandler);
	
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

    public static class DBHandler implements HttpHandler{
	public String responseText, dburl;
	public DBHandler(String dburl){
	    this.dburl = dburl;
	}
	
	@Override
	public void handle (HttpExchange exchange) throws IOException {
	    try{
		try (Connection conn = DriverManager.getConnection(dburl);
		     Statement stmt = conn.createStatement();
		     // 执行一个简单的查询，看看数据库版本
		     ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
		    if (rs.next()) {
			responseText = "Success! MySQL Version: " + rs.getString(1);
		    } else {
			responseText = "Connected, but no result returned.";
		    }
		}
	    } catch(SQLException e) {
		e.printStackTrace();
	    }
	    
	    byte[] responseBytes = responseText.getBytes();
	    exchange.sendResponseHeaders(200, responseBytes.length);
	    try(OutputStream os = exchange.getResponseBody()) {
		os.write(responseBytes);
	    }
	}
    }
}

