package app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.DTO.ApiResponse;
import app.Service.AuthSystem;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    AuthSystem authsystem;

    // DTO for Login Request
    public static class LoginRequest {
        public String username;
        public String password;
    }

    // DTO for Login Response Data
    public static class LoginData {
        public String token;

        public LoginData(String token) {
            this.token = token;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginData>> login(
            @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.username;
        String password = loginRequest.password;

        if (username == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "缺少用户名或密码"));
        }

        AuthSystem.SessionResult sessionresult =
                authsystem.getSession(username, password);

        if (sessionresult.err == null && sessionresult.session != null) {
            LoginData data = new LoginData(sessionresult.session);

            ResponseCookie springCookie =
                    ResponseCookie.from("session_token", sessionresult.session)
                            .httpOnly(true).sameSite("Lax").path("/").build();

            System.out.printf("[Login] Successfully login, session: %s\n",
                    sessionresult.session);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                    .body(ApiResponse.success("登录成功", data));
        } else {
            // Business Logic Error: 200 OK, Code 1002
            System.out.printf("[Login] Error: %s\n", sessionresult.err);
            return ResponseEntity
                    .ok(ApiResponse.error(1002, sessionresult.err));
        }
    }
}
