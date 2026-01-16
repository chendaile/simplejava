package app.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.Service.AuthSystem;

public class LoginHD implements HttpHandler {
    AuthSystem authsystem;

    public LoginHD(AuthSystem a) {
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
