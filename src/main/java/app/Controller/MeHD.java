package app.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.Service.AuthSystem;

public class MeHD implements HttpHandler {
    AuthSystem authsystem;

    public MeHD(AuthSystem a) {
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
