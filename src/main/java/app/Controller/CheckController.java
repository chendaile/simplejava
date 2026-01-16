package app.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class CheckController {

    public static class CheckResponse {
        public String content;

        public CheckResponse(String content) {
            this.content = content;
        }
    }

    @GetMapping("/check")
    public CheckResponse check(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        System.out.printf("[Check] detect method: %s, uri: %s\n", method, uri);

        return new CheckResponse("Hello World From OFT.");
    }
}
