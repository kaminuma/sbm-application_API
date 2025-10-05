package importApp.controller;

import importApp.entity.UserEntity;
import importApp.exception.AccountLockedException;
import importApp.exception.BadCredentialsException;
import importApp.mapper.UserMapper;
import importApp.model.ChangePasswordRequest;
import importApp.model.LoginRequest;
import importApp.model.LoginResponse;
import importApp.model.RefreshTokenRequest;
import importApp.security.JwtService;
import importApp.service.UserService;
import importApp.service.OAuth2SessionService;
import importApp.service.RefreshTokenService;
import importApp.entity.RefreshTokenEntity;
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

    @Autowired
    private RefreshTokenService refreshTokenService;

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
        logger.info("Login request received for user: {}", loginRequest.getUsername());

        try {
            UserEntity user = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());

            if (user != null) {
                String userId = String.valueOf(user.getUser_id());
                String token = jwtService.generateToken(userId);
                RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user.getUser_id());
                logger.info("Login successful for user ID: {}", userId);
                return ResponseEntity.ok(new LoginResponse(token, refreshToken.getToken(), userId));
            } else {
                logger.warn("Login failed: Invalid username or password");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "ユーザー名またはパスワードが正しくありません。");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (AccountLockedException e) {
            logger.warn("Login attempt for locked account: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "アカウントがロックされています。しばらく時間をおいてから再度お試しください。");
            errorResponse.put("errorType", "ACCOUNT_LOCKED");
            return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse); // 423 Locked
        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("errorType", "BAD_CREDENTIALS");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            logger.error("Login error", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "ログインに失敗しました。しばらくしてからもう一度お試しください。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
                    .body("パスワードの変更ができませんでした。入力内容を確認してください。");
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

            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(Long.valueOf(sessionData.getUserId()));
            return ResponseEntity.ok(new LoginResponse(sessionData.getJwt(), refreshToken.getToken(), sessionData.getUserId()));
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

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        logger.info("Refresh token request received");

        try {
            RefreshTokenEntity newRefreshToken = refreshTokenService.rotateRefreshToken(request.getRefreshToken());

            if (newRefreshToken == null) {
                logger.warn("Invalid or expired refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired refresh token");
            }

            String newAccessToken = jwtService.generateToken(String.valueOf(newRefreshToken.getUser_id()));
            logger.info("Token refreshed successfully for user ID: {}", newRefreshToken.getUser_id());

            return ResponseEntity.ok(new LoginResponse(newAccessToken, newRefreshToken.getToken(), String.valueOf(newRefreshToken.getUser_id())));
        } catch (Exception e) {
            logger.error("Token refresh error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Token refresh failed");
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        logger.info("Logout request received");

        try {
            refreshTokenService.deleteRefreshToken(request.getRefreshToken());
            logger.info("User logged out successfully");
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            logger.error("Logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed");
        }
    }

}
