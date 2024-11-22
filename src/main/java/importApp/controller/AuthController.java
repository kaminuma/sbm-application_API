package importApp.controller;

import importApp.entity.UserEntity;
import importApp.model.LoginRequest;
import importApp.model.LoginResponse;
import importApp.security.JwtService;
import importApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserEntity user) {
        userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // ユーザー名とパスワードでユーザーを認証
        UserEntity user = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());

        // 認証に成功した場合
        if (user != null) {
            // JWTトークンを生成
            String userId = String.valueOf(user.getUser_id());
            String token = jwtService.generateToken(userId);

            // LoginResponseを作成し、トークンとユーザーIDを返す
            return ResponseEntity.ok(new LoginResponse(token, userId));
        } else {
            // 認証に失敗した場合
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, null)); // エラーメッセージは含めず
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean isDeleted = userService.deleteUser(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

