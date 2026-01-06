import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class AuthSystem {
    Map<String, String> pwdDB = new HashMap<>();
    Map<String, String> sessionDB = new HashMap<>();
    public static class LoginResult {
	String session;
	String err;
	public LoginResult(String s,String e) {
	    this.session = s;
	    this.err = e;
	}
    }
    
    public LoginResult getSession(String u, String p) {
	String expected_pwd = pwdDB.get(u);

	if (u == null){
	    return new LoginResult(null, "用户名未传入.");
	} else if ("".equals(u)){
	    return new LoginResult(null, "用户名应不为空.");
	} else if (expected_pwd == null){
	    return new LoginResult(null, "用户未注册.");
	} else if (p == null){
	    return new LoginResult(null, "密码未传入.");
	} else if ("".equals(p)){
	    return new LoginResult(null, "密码不应为空.");
	} else if (!expected_pwd.equals(p)){
	    return new LoginResult(null, "密码错误.");
	}
	    
	String session = UUID.randomUUID().toString();
	sessionDB.put(session, u);
	return new LoginResult(session, null);
    }
    
    public String Register(String u, String p){
	if (pwdDB.containsKey(u)){
	    return "用户已存在.";
	} else if ("".equals(u)){
	    return "用户名应不为空";
	} else if (u == null){
	    return "未传入用户名";
	} else if ("".equals(p)){
	    return "密码应不为空";
	} else if (p == null){
	    return "未传入密码";
	} 
	pwdDB.put(u, p);
	return null;
    }
}

class RegisterHD implements HttpHandler {
    AuthSystem authsystem;
    public RegisterHD(AuthSystem a){
	this.authsystem = a;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
	String method = exchange.getRequestMethod();
	URI uri = exchange.getRequestURI();
	    
	if (method.equals("POST")) {
	    exchange.sendResponseHeaders(200, 0);
	    InputStream is = exchange.getRequestBody();
	    String body = new String(is.readAllBytes());
	    Map<String, String> body_parsed = Jsonparser.parseJson(body);
		
	    String username = body_parsed.get("username");
	    String password = body_parsed.get("password");
	    String error = authsystem.Register(username, password);
	    try (OutputStream os = exchange.getResponseBody()) {
		if (error == null){
		    os.write("Successfully register".getBytes());
		    System.out.printf("[Register] Successfully register, username: %s, pwd: %s\n", username, password);
		} else {
		    byte[] err_bytes = error.getBytes();
		    os.write(err_bytes);
		    System.out.printf("[Register] Error: %s\n", error);
		}
	    }
	} else {
	    exchange.sendResponseHeaders(405, -1);
	}
    }
}

class LoginHD implements HttpHandler {
    AuthSystem authsystem;
    public LoginHD(AuthSystem a){
	this.authsystem = a;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
	String method = exchange.getRequestMethod();
	URI uri = exchange.getRequestURI();
	    
	if (method.equals("POST")) {
	    exchange.sendResponseHeaders(200, 0);
	    InputStream is = exchange.getRequestBody();
	    String body = new String(is.readAllBytes());
	    Map<String, String> body_parsed = Jsonparser.parseJson(body);
		
	    String username = body_parsed.get("username");
	    String password = body_parsed.get("password");
	    AuthSystem.LoginResult loginresult = authsystem.getSession(username, password);
	    try (OutputStream os = exchange.getResponseBody()) {
		if (loginresult.err == null){
		    byte[] session_bytes = loginresult.session.getBytes();
		    os.write(session_bytes);
		    System.out.printf("[Login] Successfully login, session: %s\n", loginresult.session);
		} else {
		    byte[] err_bytes = loginresult.err.getBytes();
		    os.write(err_bytes);
		    System.out.printf("[Login] Error: %s\n", loginresult.err);
		}
	    }
	} else {
	    exchange.sendResponseHeaders(405, -1);
	}
    }
}
	
