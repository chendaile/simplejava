package app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.DTO.ApiResponse;
import app.Service.AuthSystem;

@RestController
@RequestMapping("/api/auth")
public class MeController {

    @Autowired
    AuthSystem authsystem;

    // DTO for User Info
    public static class UserInfo {
        public String username;

        public UserInfo(String username) {
            this.username = username;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getMe(@CookieValue(
            name = "session_token", required = false) String tokenFromCookie) {
        System.out.printf("[Me] Received Cookie Token: %s\n", tokenFromCookie);

        if (tokenFromCookie == null) {
            // Original code sent 401 with code 1003.
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(1003, "未登录"));
        }

        AuthSystem.LoginResult result = authsystem.Login(tokenFromCookie);

        if (result.username != null) {
            UserInfo data = new UserInfo(result.username);

            System.out.printf("[Me] Session verified for user: %s\n",
                    result.username);
            return ResponseEntity.ok(ApiResponse.success("Session有效", data));
        } else {
            // Original code sent 401 with code 1003 for error too.
            System.out.printf("[Me] Session invalid: %s\n", result.err);
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(1003, result.err));
        }
    }
}
