package app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.DTO.ApiResponse;
import app.Service.AuthSystem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户注册、登录相关接口")
public class LoginController {
    private ApiResponse<Void> apiresponse;
    @Autowired
    private AuthSystem authSystem;

    private static class LoginRequest {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 session")
    public ResponseEntity<ApiResponse<Void>> login(
            @RequestBody LoginRequest request) {
        AuthSystem.SessionResult sessionresult =
                authSystem.getSession(request.username, request.password);
        String session = sessionresult.session;
        String err = sessionresult.err;
        if (err == null) {
            apiresponse = ApiResponse.success("登入成功.");
            ResponseCookie cookie = ResponseCookie.from("session", session)
                    .httpOnly(true).sameSite("Lax").path("/").build();
            return ResponseEntity.ok().header("Set-Cookie", cookie.toString())
                    .body(apiresponse);
        } else {
            apiresponse = ApiResponse.error(1003, err);
            return ResponseEntity.ok().body(apiresponse);
        }
    }
}
