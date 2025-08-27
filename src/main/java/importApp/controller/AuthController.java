package importApp.controller;

import importApp.entity.UserEntity;
import importApp.mapper.UserMapper;
import importApp.model.ChangePasswordRequest;
import importApp.model.LoginRequest;
import importApp.model.LoginResponse;
import importApp.security.JwtService;
import importApp.service.UserService;
import importApp.service.OAuth2SessionService;
import java.util.Map;
import java.util.HashMap;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
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

    @Autowired
    private OAuth2SessionService sessionService;

    @Autowired
    private UserMapper userMapper;

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
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header format");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid Authorization header format");
            }
            String token = authHeader.replace("Bearer ", "");
            // JWTトークンからユーザーIDを抽出（検証はextractUserId内で例外として処理される）
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
        } catch (IllegalStateException e) {
            // OAuth ユーザーのパスワード変更試行
            logger.warn("Invalid password change attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (ExpiredJwtException e) {
            // トークンが期限切れの場合
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token has expired");
        } catch (SignatureException e) {
            // トークンの署名が不正な場合
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token signature");
        } catch (Exception e) {
            // その他の予期せぬエラー（データベースエラーなど）
            logger.error("An unexpected error occurred", e); // 詳細をログに出力
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @PostMapping("/auth/oauth2/session")
    public ResponseEntity<?> getOAuth2Session(@RequestParam String sessionId) {
        logger.info("OAuth2 session request received: sessionId={}", sessionId);

        try {
            OAuth2SessionService.SessionData sessionData = sessionService.getAndRemoveSession(sessionId);
            
            if (sessionData == null) {
                logger.warn("Invalid or expired session: {}", sessionId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired session");
            }

            return ResponseEntity.ok(new LoginResponse(sessionData.getJwt(), sessionData.getUserId()));
        } catch (Exception e) {
            logger.error("OAuth2 session processing error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Session processing failed");
        }
    }

    @DeleteMapping("/auth/withdraw")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String authHeader) {
        logger.info("User withdrawal request received");
        
        try {
            // Authorization ヘッダーの検証
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid Authorization header format");
            }
            String token = authHeader.replace("Bearer ", "");
            
            // JWTトークンからユーザーIDを抽出
            String userId = jwtService.extractUserId(token);
            
            // 退会処理を実行
            boolean isDeleted = userService.deleteUser(Long.valueOf(userId));
            
            if (isDeleted) {
                logger.info("User successfully withdrew: userId={}", userId);
                return ResponseEntity.ok("User account successfully deleted");
            } else {
                logger.warn("Withdrawal failed for userId={} - user may already be deleted", userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to delete user account");
            }
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token has expired");
        } catch (SignatureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token signature");
        } catch (Exception e) {
            logger.error("User withdrawal error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during withdrawal");
        }
    }

    @GetMapping("/auth/user")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid Authorization header format");
            }
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtService.extractUserId(token);
            
            UserEntity user = userMapper.findById(Long.valueOf(userId));
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("user_id", user.getUser_id());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("profileImageUrl", user.getProfileImageUrl()); // nullでもOK
            
            return ResponseEntity.ok(response);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has expired");
        } catch (SignatureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

}
