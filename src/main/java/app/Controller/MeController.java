package app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.Service.AuthSystem;
import app.Service.AuthSystem.LoginResult;
import app.DTO.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户注册、登录相关接口")
public class MeController {
    private ApiResponse<LoginResponseData> apiresponse;
    @Autowired
    private AuthSystem authSystem;

    private static class LoginResponseData {
        public String username;

        private LoginResponseData(String username) {
            this.username = username;
        }
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息",
            description = "通过 session cookie 获取当前登录用户的信息")
    public ResponseEntity<ApiResponse<LoginResponseData>> me(
            @CookieValue(name = "session", required = false) String session) {
        LoginResult loginresult = authSystem.Login(session);
        String username = loginresult.username;
        String err = loginresult.err;
        if (err == null) {
            LoginResponseData data = new LoginResponseData(username);
            apiresponse = ApiResponse.success("登入成功.", data);
        } else {
            apiresponse = ApiResponse.error(1001, err);
        }
        return ResponseEntity.ok().body(apiresponse);
    }
}
