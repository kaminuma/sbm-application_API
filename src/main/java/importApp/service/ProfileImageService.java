package importApp.service;

import importApp.dto.ProfileImageResponseDto;
import importApp.entity.UserEntity;
import importApp.mapper.UserMapper;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileImageService.class);
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg",
        "image/png",
        "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_IMAGE_SIZE = 1024; // 最大画像サイズ (1024x1024)
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${storage.upload-dir:uploads/profiles}")
    private String uploadDir;
    
    @Value("${storage.base-url:http://localhost:8080/uploads/profiles}")
    private String baseUrl;
    
    private final Tika tika = new Tika();
    
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }
        } catch (IOException e) {
            logger.error("Failed to create upload directory", e);
        }
    }
    
    public ProfileImageResponseDto uploadProfileImage(MultipartFile file, String userId) {
        try {
            // ファイルサイズチェック
            if (file.getSize() > MAX_FILE_SIZE) {
                return ProfileImageResponseDto.builder()
                    .success(false)
                    .message("ファイルサイズが5MBを超えています")
                    .build();
            }
            
            // ファイル形式チェック
            String contentType = tika.detect(file.getInputStream());
            if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
                return ProfileImageResponseDto.builder()
                    .success(false)
                    .message("対応していないファイル形式です。JPEG、PNG、WebPのみ対応しています")
                    .build();
            }
            
            // ユーザー情報取得
            UserEntity user = userMapper.findById(Long.parseLong(userId));
            if (user == null) {
                return ProfileImageResponseDto.builder()
                    .success(false)
                    .message("ユーザーが見つかりません")
                    .build();
            }
            
            // 既存のカスタム画像があれば削除
            if (user.getProfileImageUrl() != null && !isGoogleImage(user.getProfileImageUrl())) {
                deleteStorageFile(user.getProfileImageUrl());
            }
            
            // ファイル名生成
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path targetPath = Paths.get(uploadDir, newFilename);
            
            // 画像をリサイズして保存
            Thumbnails.of(file.getInputStream())
                .size(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                .keepAspectRatio(true)
                .outputQuality(0.9)
                .toFile(targetPath.toFile());
            
            // URL生成
            String imageUrl = baseUrl + "/" + newFilename;
            
            // DBを更新
            userMapper.updateProfileImageUrl(Long.parseLong(userId), imageUrl);
            
            logger.info("Profile image uploaded successfully for user {}: {}", userId, imageUrl);
            
            return ProfileImageResponseDto.builder()
                .success(true)
                .profileImageUrl(imageUrl)
                .isGoogleImage(false)
                .canDelete(true)
                .message("プロフィール画像をアップロードしました")
                .build();
                
        } catch (Exception e) {
            logger.error("Failed to upload profile image for user {}", userId, e);
            return ProfileImageResponseDto.builder()
                .success(false)
                .message("画像のアップロードに失敗しました: " + e.getMessage())
                .build();
        }
    }
    
    public ProfileImageResponseDto deleteProfileImage(String userId) {
        try {
            // ユーザー情報取得
            UserEntity user = userMapper.findById(Long.parseLong(userId));
            if (user == null) {
                return ProfileImageResponseDto.builder()
                    .success(false)
                    .message("ユーザーが見つかりません")
                    .build();
            }
            
            // カスタム画像の場合はストレージから削除
            if (user.getProfileImageUrl() != null && !isGoogleImage(user.getProfileImageUrl())) {
                deleteStorageFile(user.getProfileImageUrl());
            }
            
            // プロフィール画像URLをnullに設定
            userMapper.updateProfileImageUrl(Long.parseLong(userId), null);
            
            logger.info("Profile image deleted for user {}", userId);
            
            return ProfileImageResponseDto.builder()
                .success(true)
                .profileImageUrl(null)
                .isGoogleImage(false)
                .canDelete(false)
                .message("プロフィール画像を削除しました")
                .build();
                
        } catch (Exception e) {
            logger.error("Failed to delete profile image for user {}", userId, e);
            return ProfileImageResponseDto.builder()
                .success(false)
                .message("画像の削除に失敗しました: " + e.getMessage())
                .build();
        }
    }
    
    public ProfileImageResponseDto getProfileImage(String userId) {
        try {
            UserEntity user = userMapper.findById(Long.parseLong(userId));
            if (user == null) {
                return ProfileImageResponseDto.builder()
                    .success(false)
                    .message("ユーザーが見つかりません")
                    .build();
            }
            
            String imageUrl = user.getProfileImageUrl();
            boolean isGoogle = isGoogleImage(imageUrl);
            
            return ProfileImageResponseDto.builder()
                .success(true)
                .profileImageUrl(imageUrl)
                .isGoogleImage(isGoogle)
                .canDelete(imageUrl != null)
                .build();
                
        } catch (Exception e) {
            logger.error("Failed to get profile image for user {}", userId, e);
            return ProfileImageResponseDto.builder()
                .success(false)
                .message("プロフィール画像の取得に失敗しました")
                .build();
        }
    }
    
    private boolean isGoogleImage(String imageUrl) {
        return imageUrl != null && 
               (imageUrl.contains("googleusercontent.com") || 
                imageUrl.contains("google.com"));
    }
    
    private void deleteStorageFile(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.contains(baseUrl)) {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir, filename);
                Files.deleteIfExists(filePath);
                logger.info("Deleted storage file: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to delete storage file: {}", imageUrl, e);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}