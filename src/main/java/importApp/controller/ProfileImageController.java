package importApp.controller;

import importApp.dto.ProfileImageResponseDto;
import importApp.security.JwtService;
import importApp.service.ProfileImageService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class ProfileImageController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileImageController.class);
    
    @Autowired
    private ProfileImageService profileImageService;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/profile-image")
    public ResponseEntity<ProfileImageResponseDto> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Profile image upload request received");
        
        // JWTトークンからユーザーIDを抽出
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(
                ProfileImageResponseDto.builder()
                    .success(false)
                    .message("認証エラー")
                    .build()
            );
        }
        
        if (file == null || file.isEmpty()) {
            logger.warn("Empty file received for user {}", userId);
            return ResponseEntity.badRequest().body(
                ProfileImageResponseDto.builder()
                    .success(false)
                    .message("ファイルが空です")
                    .build()
            );
        }
        
        // プロフィール画像をアップロード
        ProfileImageResponseDto response = profileImageService.uploadProfileImage(file, userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/profile-image")
    public ResponseEntity<ProfileImageResponseDto> deleteProfileImage(
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Profile image delete request received");
        
        // JWTトークンからユーザーIDを抽出
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(
                ProfileImageResponseDto.builder()
                    .success(false)
                    .message("認証エラー")
                    .build()
            );
        }
        
        // プロフィール画像を削除
        ProfileImageResponseDto response = profileImageService.deleteProfileImage(userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/profile-image")
    public ResponseEntity<ProfileImageResponseDto> getProfileImage(
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Profile image get request received");
        
        // JWTトークンからユーザーIDを抽出
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(
                ProfileImageResponseDto.builder()
                    .success(false)
                    .message("認証エラー")
                    .build()
            );
        }
        
        // プロフィール画像情報を取得
        ProfileImageResponseDto response = profileImageService.getProfileImage(userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    private String extractUserIdFromToken(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header format");
                return null;
            }
            
            String token = authHeader.substring(7);
            return jwtService.extractUserId(token);
            
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired");
            return null;
        } catch (SignatureException e) {
            logger.warn("Invalid JWT token signature");
            return null;
        } catch (Exception e) {
            logger.warn("JWT token validation error: {}", e.getMessage());
            return null;
        }
    }
}