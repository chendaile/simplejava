package app.Controller;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CheckHD implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response_text = "{\"content\": \"Hello World From OFT.\"}";
        byte[] bytes = response_text.getBytes();
        exchange.sendResponseHeaders(200, 0);

        String method = exchange.getRequestMethod();
        String uri = exchange.getRequestURI().getPath();
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
            System.out.printf("[Check] detect method: %s, uri: %s\n", method, uri);
        }
    }
}
