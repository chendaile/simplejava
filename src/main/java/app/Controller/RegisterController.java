package app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
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
public class RegisterController {
    private ApiResponse<Void> apiresponse;
    @Autowired
    private AuthSystem authSystem;

    private static class RegisterRequest {
        public String username;
        public String password;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody RegisterRequest request) {
        String err = authSystem.Register(request.username, request.password);
        if (err == null) {
            apiresponse = ApiResponse.success("注册成功.");
        } else {
            apiresponse = ApiResponse.error(1002, err);
        }
        return ResponseEntity.ok().body(apiresponse);
    }
}
