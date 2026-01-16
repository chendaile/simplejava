package app.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.DTO.ApiResponse;
import app.Service.AuthSystem;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    @Autowired
    AuthSystem authsystem;

    public static class RegisterRequest {
        public String username;
        public String password;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody RegisterRequest request) {
        String username = request.username;
        String password = request.password;

        String error = authsystem.Register(username, password);

        if (error == null) {
            System.out.printf(
                    "[Register] Successfully register, username: %s\n",
                    username);
            return ResponseEntity.ok(ApiResponse.success("注册成功"));
        } else {
            // Business Logic Error: 200 OK, Code 1001
            System.out.printf("[Register] Error: %s\n", error);
            return ResponseEntity.ok(ApiResponse.error(1001, error));
        }
    }
}
