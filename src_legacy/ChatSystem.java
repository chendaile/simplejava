import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class CallDeepseekHD implements HttpHandler {
	static final String DeepseekAPIPath = "https://api.deepseek.com/chat/completions";
	static final String DeepseekAPIKey = "sk-d136935b79a048989462c744798ca1f9";

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		HttpResponse<String> response;
		String method = exchange.getRequestMethod();
		if ("POST".equals(method)) {
			InputStream is = exchange.getRequestBody();
			String requestBody = new String(is.readAllBytes());

			HttpClient client = HttpClient.newHttpClient();
			String json = "{" + "\"model\": \"deepseek-chat\"," + "\"messages\": ["
					+ "  {\"role\": \"system\", \"content\": \"You are a helpful assistant.answer in chinese.\"},"
					+ "  {\"role\": \"user\", \"content\": \"" + requestBody + "\"}" + "]" + "}";

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(DeepseekAPIPath))
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + DeepseekAPIKey) //
					.POST(HttpRequest.BodyPublishers.ofString(json)).build();
			System.out.printf("[CallDeepseek] send request: %s,\n body: %s\n", request.toString(),
					json);

			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofString());
			} catch (InterruptedException e) {
				e.printStackTrace();
				exchange.sendResponseHeaders(501, -1);
				return;
			}

			JsonObject responseParsed = JsonParser.parseString(response.body()).getAsJsonObject();
			String content = responseParsed.get("choices").getAsJsonArray().get(0).getAsJsonObject()
					.get("message").getAsJsonObject().get("content").getAsString();
			System.out.printf("[CallDeepseek] get response body: %s\n", content);
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();
			os.write(content.getBytes());
			os.close();
		} else {
			exchange.sendResponseHeaders(405, -1);
		}
	}

}
