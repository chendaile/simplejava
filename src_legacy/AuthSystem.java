import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

/**
 * AuthSystem Business Codes: 0 : Success (成功) 400 : JSON Format Error (JSON格式错误/系统错误) 405 : Method
 * Not Allowed (方法不允许) 1001 : Register Error (注册错误) 1002 : Login Error (账号密码错误) 1003 : Login Error
 * (session错误)
 */
class AuthSystem {
	Map<String, String> pwdDB;
	Map<String, String> session2userDB = new ConcurrentHashMap<>();
	Map<String, String> user2sessionDB = new ConcurrentHashMap<>();
	static final String DBPATH = "pwdDB.dat";

	static class SessionResult {
		String session;
		String err;

		SessionResult(String s, String e) {
			this.session = s;
			this.err = e;
		}
	}
	static class LoginResult {
		String username;
		String err;

		LoginResult(String u, String e) {
			this.username = u;
			this.err = e;
		}
	}

	AuthSystem() {
		this.pwdDB = loadPwdDB();
	}

	@SuppressWarnings("unchecked")
	Map<String, String> loadPwdDB() {
		File dbfile = new File(DBPATH);
		if (!dbfile.exists()) {
			System.out.printf("未找到磁盘数据库%s,新建数据库.\n", DBPATH);
			return new ConcurrentHashMap<>();
		}
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DBPATH))) {
			System.out.printf("读取到磁盘数据库%s.\n", DBPATH);
			return (Map<String, String>) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return new ConcurrentHashMap<>();
		}
	}

	void savePwdDB() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DBPATH))) {
			oos.writeObject(pwdDB);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	LoginResult Login(String session) {
		if (session == null) {
			return new LoginResult(null, "未传入session.");
		} else if ("".equals(session)) {
			return new LoginResult(null, "session不应为空.");
		}

		String username = session2userDB.get(session);
		if (username == null) {
			return new LoginResult(null, "未有登入凭证.");
		} else {
			return new LoginResult(username, null);
		}
	}

	SessionResult getSession(String u, String p) {
		if (u == null) {
			return new SessionResult(null, "用户名未传入.");
		} else if ("".equals(u)) {
			return new SessionResult(null, "用户名应不为空.");
		} else if (p == null) {
			return new SessionResult(null, "密码未传入.");
		} else if ("".equals(p)) {
			return new SessionResult(null, "密码不应为空.");
		}

		String expected_pwd = pwdDB.get(u);
		if (expected_pwd == null) {
			return new SessionResult(null, "用户未注册.");
		} else if (!expected_pwd.equals(p)) {
			return new SessionResult(null, "密码错误.");
		} else if (user2sessionDB.containsKey(u)) {
			String oldSession = user2sessionDB.remove(u);
			if (oldSession != null) {
				session2userDB.remove(oldSession);
			}
		}
		String session = UUID.randomUUID().toString();
		user2sessionDB.put(u, session);
		session2userDB.put(session, u);
		return new SessionResult(session, null);
	}

	String Register(String u, String p) {
		if (pwdDB.containsKey(u)) {
			return "用户已存在.";
		} else if ("".equals(u)) {
			return "用户名应不为空";
		} else if (u == null) {
			return "未传入用户名";
		} else if ("".equals(p)) {
			return "密码应不为空";
		} else if (p == null) {
			return "未传入密码";
		}
		pwdDB.put(u, p);
		savePwdDB();
		return null;
	}

	static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
		// Default: business code = 0 (success) or statusCode (if error)
		int businessCode = (statusCode == 200) ? 0 : statusCode;
		sendJson(exchange, statusCode, businessCode, null, data);
	}

	static void sendJson(HttpExchange exchange, int statusCode, int businessCode, String message,
			Object data) throws IOException {
		ApiResponse response = new ApiResponse(businessCode, message, data);
		Gson gson = new Gson();
		String jsonResponse = gson.toJson(response);
		byte[] responseBytes = jsonResponse.getBytes("UTF-8");

		exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
		exchange.sendResponseHeaders(statusCode, responseBytes.length);

		try (OutputStream os = exchange.getResponseBody()) {
			os.write(responseBytes);
		}
	}

	static Map<String, String> parseCookie(String cookieHeader) {
		Map<String, String> map = new HashMap<>();
		if (cookieHeader == null) {
			return map;
		}
		String[] pairs = cookieHeader.split(";");
		for (String pair : pairs) {
			String[] keyval = pair.trim().split("=");
			if (keyval.length == 2) {
				map.put(keyval[0], keyval[1]);
			}
		}
		return map;
	}
}


class ApiResponse {
	int code;
	String message;
	Object data;

	ApiResponse(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
}


class RegisterHD implements HttpHandler {
	AuthSystem authsystem;

	RegisterHD(AuthSystem a) {
		this.authsystem = a;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();

		if (!method.equals("POST")) {
			AuthSystem.sendJson(exchange, 405, 405, "方法不支持", null);
			return;
		}

		InputStream is = exchange.getRequestBody();
		String body = new String(is.readAllBytes());
		try {
			JsonObject body_parsed = JsonParser.parseString(body).getAsJsonObject();

			String username = body_parsed.get("username").getAsString();
			String password = body_parsed.get("password").getAsString();
			String error = authsystem.Register(username, password);

			if (error == null) {
				AuthSystem.sendJson(exchange, 200, 0, "注册成功", null);
				System.out.printf("[Register] Successfully register, username: %s\n", username);
			} else {
				// Business Logic Error: 200 OK, Code 1001
				AuthSystem.sendJson(exchange, 200, 1001, error, null);
				System.out.printf("[Register] Error: %s\n", error);
			}
		} catch (JsonParseException e) {
			// System Error: 400 Bad Request, Code 400
			AuthSystem.sendJson(exchange, 400, 400, "Json格式错误", null);
			System.out.printf("[Register] Error: %s\n", e.getMessage());
		}
	}
}


class LoginHD implements HttpHandler {
	AuthSystem authsystem;

	LoginHD(AuthSystem a) {
		this.authsystem = a;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();

		if (!method.equals("POST")) {
			AuthSystem.sendJson(exchange, 405, 405, "方法不支持", null);
			return;
		}

		InputStream is = exchange.getRequestBody();
		String body = new String(is.readAllBytes());
		try {
			JsonObject body_parsed = JsonParser.parseString(body).getAsJsonObject();

			if (body_parsed.has("username") && body_parsed.has("password")) {
				String username = body_parsed.get("username").getAsString();
				String password = body_parsed.get("password").getAsString();
				AuthSystem.SessionResult sessionresult = authsystem.getSession(username, password);

				if (sessionresult.err == null) {
					Map<String, String> data = new HashMap<>();
					data.put("token", sessionresult.session);

					// Set-Cookie header BEFORE sending response
					String cookieVal =
							String.format("session_token=%s; Path=/; HttpOnly; SameSite=Lax",
									sessionresult.session);
					exchange.getResponseHeaders().add("Set-Cookie", cookieVal);
					System.out.printf("[Login] Setting cookie: %s\n", cookieVal);

					AuthSystem.sendJson(exchange, 200, 0, "登录成功", data);
					System.out.printf("[Login] Successfully login, session: %s\n",
							sessionresult.session);
				} else {
					// Business Logic Error: 200 OK, Code 1002
					AuthSystem.sendJson(exchange, 200, 1002, sessionresult.err, null);
					System.out.printf("[Login] Error: %s\n", sessionresult.err);
				}
			} else {
				AuthSystem.sendJson(exchange, 400, 400, "Json格式错误: 缺少用户名或密码", null);
				System.out.printf("[Login] Error: Missing username or password\n");
			}
		} catch (Exception e) {
			AuthSystem.sendJson(exchange, 400, 400, "系统错误/Json格式错误", null);
			System.out.printf("[Login] Error: %s\n", e.getMessage());
			e.printStackTrace();
		}
	}
}


class MeHD implements HttpHandler {
	AuthSystem authsystem;

	MeHD(AuthSystem a) {
		this.authsystem = a;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();

		if (!method.equals("GET")) {
			AuthSystem.sendJson(exchange, 405, 405, "方法不支持", null);
			return;
		}

		String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
		System.out.printf("[Me] Received Cookie Header: %s\n", cookieHeader);
		Map<String, String> cookies = AuthSystem.parseCookie(cookieHeader);
		String tokenFromCookie = cookies.get("session_token");

		if (tokenFromCookie == null) {
			AuthSystem.sendJson(exchange, 401, 1003, "未登录", null);
			return;
		}

		AuthSystem.LoginResult result = authsystem.Login(tokenFromCookie);

		if (result.username != null) {
			Map<String, String> data = new HashMap<>();
			data.put("username", result.username);
			AuthSystem.sendJson(exchange, 200, 0, "Session有效", data);
			System.out.printf("[Me] Session verified for user: %s\n", result.username);
		} else {
			AuthSystem.sendJson(exchange, 401, 1003, result.err, null);
			System.out.printf("[Me] Session invalid: %s\n", result.err);
		}
	}
}
