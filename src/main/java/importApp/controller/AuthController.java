package importApp.controller;

import importApp.entity.UserEntity;
import importApp.model.ChangePasswordRequest;
import importApp.model.LoginRequest;
import importApp.model.LoginResponse;
import importApp.security.JwtService;
import importApp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody UserEntity user) {
        logger.info("Register request received");

        try {
            userService.registerUser(user);
            logger.info("User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (Exception e) {
            logger.error("User registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User registration failed");
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login request received");

        try {
            UserEntity user = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());

            if (user != null) {
                String userId = String.valueOf(user.getUser_id());
                String token = jwtService.generateToken(userId);
                logger.info("Login successful for user ID: {}", userId);
                return ResponseEntity.ok(new LoginResponse(token, userId));
            } else {
                logger.warn("Login failed: Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(null, null));
            }
        } catch (Exception e) {
            logger.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed due to server error");
        }
    }

    @DeleteMapping("/auth/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Delete user request received for ID: {}", id);

        try {
            boolean isDeleted = userService.deleteUser(id);
            if (isDeleted) {
                logger.info("User deleted successfully: ID={}", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("User not found for deletion: ID={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error deleting user ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/auth/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, 
                                           @RequestHeader("Authorization") String authHeader) {
        logger.info("Change password request received");

        try {
            // JWTトークンからユーザーIDを取得
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtService.extractUserId(token);
            
            // パスワード変更処理
            boolean isChanged = userService.changePassword(Long.valueOf(userId), 
                                                         request.getCurrentPassword(), 
                                                         request.getNewPassword());
            
            if (isChanged) {
                logger.info("Password changed successfully for user ID: {}", userId);
                return ResponseEntity.ok("Password changed successfully");
            } else {
                logger.warn("Password change failed: Invalid current password for user ID: {}", userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Current password is incorrect");
            }
        } catch (Exception e) {
            logger.error("Password change error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Password change failed due to server error");
        }
    }
}
