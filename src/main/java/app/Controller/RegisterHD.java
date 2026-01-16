package app.Controller;

import java.io.IOException;
import java.io.InputStream;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.Service.AuthSystem;

public class RegisterHD implements HttpHandler {
    AuthSystem authsystem;

    public RegisterHD(AuthSystem a) {
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
