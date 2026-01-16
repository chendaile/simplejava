package app.Controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class DeepseekController {

    static final String DeepseekAPIPath =
            "https://api.deepseek.com/chat/completions";
    static final String DeepseekAPIKey = "sk-d136935b79a048989462c744798ca1f9";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/calldeepseek")
    public ResponseEntity<String> callDeepseek(
            @RequestBody String requestBody) {
        // requestBody here is the raw text content from user, e.g. "你好"
        // effectively simple string if body is raw text.
        // But if client sends JSON, we might want to accept Object or String.
        // The original code: String requestBody = new
        // String(is.readAllBytes());
        // And then constructed JSON: "{\"role\": \"user\", \"content\": \"" +
        // requestBody + "\"}"
        // This implies requestBody is just the content string.
        // Warning: if requestBody contains quotes it might break the JSON
        // construction.
        // I should probably use Jackson to construct the request JSON to be
        // safe.

        HttpClient client = HttpClient.newHttpClient();

        try {
            // Construct JSON securely
            Map<String, Object> systemMsg = Map.of("role", "system", "content",
                    "You are a helpful assistant.answer in chinese.");
            Map<String, Object> userMsg =
                    Map.of("role", "user", "content", requestBody);
            Map<String, Object> payload = Map.of("model", "deepseek-chat",
                    "messages", new Object[] {systemMsg, userMsg});
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DeepseekAPIPath))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + DeepseekAPIKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            System.out.printf("[CallDeepseek] send request: %s,\n body: %s\n",
                    request.toString(), jsonPayload);

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode rootNode = objectMapper.readTree(response.body());
            String content = "";
            if (rootNode.has("choices") && rootNode.get("choices").size() > 0) {
                content = rootNode.get("choices").get(0).get("message")
                        .get("content").asText();
            } else {
                content = "Error or empty response: " + response.body();
            }

            System.out.printf("[CallDeepseek] get response body: %s\n",
                    content);
            return ResponseEntity.ok(content);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(501).build();
        }
    }
}
