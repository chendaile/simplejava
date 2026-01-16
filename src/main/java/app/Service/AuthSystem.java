package app.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;

public class AuthSystem {
    Map<String, String> pwdDB;
    Map<String, String> session2userDB = new ConcurrentHashMap<>();
    Map<String, String> user2sessionDB = new ConcurrentHashMap<>();
    static final String DBPATH = "pwdDB.dat";

    public static class SessionResult {
        public String session;
        public String err;

        SessionResult(String s, String e) {
            this.session = s;
            this.err = e;
        }
    }

    public static class LoginResult {
        public String username;
        public String err;

        LoginResult(String u, String e) {
            this.username = u;
            this.err = e;
        }
    }

    public AuthSystem() {
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

    public LoginResult Login(String session) {
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

    public SessionResult getSession(String u, String p) {
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

    public String Register(String u, String p) {
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

    public static void sendJson(HttpExchange exchange, int statusCode, Object data)
            throws IOException {
        // Default: business code = 0 (success) or statusCode (if error)
        int businessCode = (statusCode == 200) ? 0 : statusCode;
        sendJson(exchange, statusCode, businessCode, null, data);
    }

    public static void sendJson(HttpExchange exchange, int statusCode, int businessCode,
            String message, Object data) throws IOException {
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

    public static Map<String, String> parseCookie(String cookieHeader) {
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
