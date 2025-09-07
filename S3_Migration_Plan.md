# プロフィール画像ストレージ S3移行計画書

## 目次
1. [移行概要](#移行概要)
2. [全体計画（フェーズ分け）](#全体計画フェーズ分け)
3. [技術的詳細手順](#技術的詳細手順)
4. [データ移行手順](#データ移行手順)
5. [リスク分析と対策](#リスク分析と対策)
6. [ロールバック手順](#ロールバック手順)
7. [テスト計画](#テスト計画)
8. [移行チェックリスト](#移行チェックリスト)

## 移行概要

### 現在の構成
- **ストレージ**: EC2ローカルファイルシステム（`/srv/sbm/api/uploads/profiles/`）
- **アクセス方式**: 静的ファイル配信
- **URL形式**: `https://api.sbm-app.com/uploads/profiles/{filename}`
- **設定**: `application-prod.yml`の`storage`セクションで管理

### 移行後の構成
- **ストレージ**: AWS S3バケット
- **アクセス方式**: S3署名付きURL または CloudFront経由
- **URL形式**: `https://{bucket}.s3.{region}.amazonaws.com/{filename}` または CloudFront URL
- **設定**: Spring Boot `@ConditionalOnProperty`による動的切り替え

### 移行の目的
- **スケーラビリティ**: EC2の容量制限からの解放
- **可用性**: S3の99.999999999%の耐久性
- **コスト最適化**: 使用量に応じた課金
- **バックアップ**: 自動的なデータレプリケーション

---

## 全体計画（フェーズ分け）

### Phase 1: 準備フェーズ（1-2日）
- [ ] AWS S3バケット作成・設定
- [ ] IAMロール・ポリシー設定
- [ ] S3サービス実装
- [ ] 設定ファイル更新

### Phase 2: 実装フェーズ（2-3日）
- [ ] S3対応ProfileImageService実装
- [ ] 条件分岐による切り替え機能実装
- [ ] 単体テスト・統合テスト実施
- [ ] ステージング環境でのテスト

### Phase 3: データ移行フェーズ（1日）
- [ ] 既存画像のS3移行スクリプト実行
- [ ] データ整合性確認
- [ ] 移行後テスト実施

### Phase 4: 本番リリースフェーズ（1日）
- [ ] 本番環境へのデプロイ
- [ ] S3モードへの切り替え
- [ ] 動作確認・監視
- [ ] 旧ファイルのクリーンアップ

---

## 技術的詳細手順

### 1. AWS S3環境構築

#### 1.1 S3バケット作成
```bash
# AWS CLI でバケット作成
aws s3 mb s3://sbm-app-profile-images --region ap-northeast-1

# バケットポリシー設定
aws s3api put-bucket-policy --bucket sbm-app-profile-images --policy '{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::sbm-app-profile-images/*"
    }
  ]
}'

# CORS設定
aws s3api put-bucket-cors --bucket sbm-app-profile-images --cors-configuration '{
  "CORSRules": [
    {
      "AllowedOrigins": ["https://sbm-app.com", "https://api.sbm-app.com"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
      "AllowedHeaders": ["*"],
      "MaxAgeSeconds": 3000
    }
  ]
}'
```

#### 1.2 IAMロール・ポリシー設定
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::sbm-app-profile-images/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::sbm-app-profile-images"
    }
  ]
}
```

### 2. 依存関係追加

#### 2.1 build.gradle更新
```gradle
dependencies {
    // 既存の依存関係...
    
    // AWS SDK
    implementation 'software.amazon.awssdk:s3:2.20.26'
    implementation 'software.amazon.awssdk:auth:2.20.26'
    implementation 'software.amazon.awssdk:regions:2.20.26'
}
```

### 3. 設定ファイル更新

#### 3.1 application-prod.yml更新
```yaml
storage:
  type: ${STORAGE_TYPE:local}  # local または s3
  upload-dir: ${STORAGE_DIR:uploads/profiles}  # ローカル用
  base-url: ${STORAGE_BASE_URL:https://api.sbm-app.com/uploads/profiles}  # ローカル用
  
  # S3設定
  s3:
    enabled: ${S3_ENABLED:false}
    bucket-name: ${S3_BUCKET_NAME:sbm-app-profile-images}
    region: ${S3_REGION:ap-northeast-1}
    base-url: ${S3_BASE_URL:https://sbm-app-profile-images.s3.ap-northeast-1.amazonaws.com}
    access-key-id: ${AWS_ACCESS_KEY_ID:}
    secret-access-key: ${AWS_SECRET_ACCESS_KEY:}
```

### 4. S3サービス実装

#### 4.1 S3Config.java作成
```java
package importApp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public S3Client s3Client(S3Properties s3Properties) {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        s3Properties.getAccessKeyId(),
                        s3Properties.getSecretAccessKey()
                    )
                ))
                .build();
    }
    
    @ConfigurationProperties(prefix = "storage.s3")
    @Configuration
    public static class S3Properties {
        private String bucketName;
        private String region;
        private String baseUrl;
        private String accessKeyId;
        private String secretAccessKey;
        
        // Getters and Setters
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public String getAccessKeyId() { return accessKeyId; }
        public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }
        
        public String getSecretAccessKey() { return secretAccessKey; }
        public void setSecretAccessKey(String secretAccessKey) { this.secretAccessKey = secretAccessKey; }
    }
}
```

#### 4.2 S3StorageService.java作成
```java
package importApp.service;

import importApp.config.S3Config.S3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);
    
    @Autowired
    private S3Client s3Client;
    
    @Autowired
    private S3Properties s3Properties;
    
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            
            String fileUrl = s3Properties.getBaseUrl() + "/" + key;
            logger.info("File uploaded to S3 successfully: {}", fileUrl);
            
            return fileUrl;
            
        } catch (Exception e) {
            logger.error("Failed to upload file to S3: {}", key, e);
            throw new RuntimeException("S3 upload failed", e);
        }
    }
    
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted from S3 successfully: {}", key);
            
        } catch (Exception e) {
            logger.error("Failed to delete file from S3: {}", key, e);
            throw new RuntimeException("S3 delete failed", e);
        }
    }
    
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error checking file existence in S3: {}", key, e);
            return false;
        }
    }
    
    public String extractKeyFromUrl(String url) {
        if (url != null && url.contains(s3Properties.getBaseUrl())) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return null;
    }
}
```

#### 4.3 ProfileImageService.java更新
```java
// 既存のProfileImageServiceにS3対応を追加

@Service
public class ProfileImageService {
    
    // 既存のフィールド...
    
    @Value("${storage.type:local}")
    private String storageType;
    
    @Autowired(required = false)
    private S3StorageService s3StorageService;
    
    public ProfileImageResponseDto uploadProfileImage(MultipartFile file, String userId) {
        try {
            // 既存のバリデーション処理...
            
            // ファイル名生成
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            
            String imageUrl;
            
            if ("s3".equals(storageType)) {
                // S3にアップロード
                if (s3StorageService == null) {
                    throw new RuntimeException("S3 service not available");
                }
                
                // 既存のカスタム画像があれば削除
                if (user.getProfileImageUrl() != null && !isGoogleImage(user.getProfileImageUrl())) {
                    String existingKey = s3StorageService.extractKeyFromUrl(user.getProfileImageUrl());
                    if (existingKey != null) {
                        s3StorageService.deleteFile(existingKey);
                    }
                }
                
                // 画像をリサイズしてS3にアップロード
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Thumbnails.of(file.getInputStream())
                    .size(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                    .keepAspectRatio(true)
                    .outputQuality(0.9)
                    .outputFormat(extension)
                    .toOutputStream(outputStream);
                
                byte[] imageBytes = outputStream.toByteArray();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                
                imageUrl = s3StorageService.uploadFile(
                    newFilename, 
                    inputStream, 
                    imageBytes.length, 
                    "image/" + extension
                );
                
            } else {
                // 既存のローカル保存処理
                if (user.getProfileImageUrl() != null && !isGoogleImage(user.getProfileImageUrl())) {
                    deleteStorageFile(user.getProfileImageUrl());
                }
                
                Path targetPath = Paths.get(uploadDir, newFilename);
                Thumbnails.of(file.getInputStream())
                    .size(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                    .keepAspectRatio(true)
                    .outputQuality(0.9)
                    .toFile(targetPath.toFile());
                
                imageUrl = baseUrl + "/" + newFilename;
            }
            
            // DBを更新
            userMapper.updateProfileImageUrl(Integer.parseInt(userId), imageUrl);
            
            logger.info("Profile image uploaded successfully for user {} using {} storage: {}", 
                       userId, storageType, imageUrl);
            
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
            
            // カスタム画像の場合は削除
            if (user.getProfileImageUrl() != null && !isGoogleImage(user.getProfileImageUrl())) {
                if ("s3".equals(storageType) && s3StorageService != null) {
                    String key = s3StorageService.extractKeyFromUrl(user.getProfileImageUrl());
                    if (key != null) {
                        s3StorageService.deleteFile(key);
                    }
                } else {
                    deleteStorageFile(user.getProfileImageUrl());
                }
            }
            
            // プロフィール画像URLをnullに設定
            userMapper.updateProfileImageUrl(Integer.parseInt(userId), null);
            
            logger.info("Profile image deleted for user {} using {} storage", userId, storageType);
            
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
    
    // 既存のgetProfileImageメソッドは変更不要
    
    // 必要なimport文を追加
    // import java.io.ByteArrayInputStream;
    // import java.io.ByteArrayOutputStream;
}
```

---

## データ移行手順

### 1. 移行前準備

#### 1.1 既存データのバックアップ
```bash
# EC2上で実行
sudo tar -czf /tmp/profile_images_backup_$(date +%Y%m%d).tar.gz /srv/sbm/api/uploads/profiles/

# S3にバックアップをアップロード
aws s3 cp /tmp/profile_images_backup_$(date +%Y%m%d).tar.gz s3://sbm-app-backups/
```

#### 1.2 データベースから画像URL情報取得
```sql
-- 移行対象の画像URL一覧取得
SELECT id, profile_image_url 
FROM users 
WHERE profile_image_url IS NOT NULL 
  AND profile_image_url NOT LIKE '%googleusercontent.com%' 
  AND profile_image_url NOT LIKE '%google.com%'
  AND profile_image_url LIKE '%uploads/profiles%';
```

### 2. 移行スクリプト作成・実行

#### 2.1 移行スクリプト（migrate_images_to_s3.py）
```python
#!/usr/bin/env python3
import os
import boto3
import mysql.connector
from urllib.parse import urlparse
import logging
from pathlib import Path

# ログ設定
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 設定
S3_BUCKET = 'sbm-app-profile-images'
S3_REGION = 'ap-northeast-1'
LOCAL_UPLOAD_DIR = '/srv/sbm/api/uploads/profiles'
S3_BASE_URL = f'https://{S3_BUCKET}.s3.{S3_REGION}.amazonaws.com'

# DB設定（環境変数から取得）
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', 'rootpassword'),
    'database': os.getenv('DB_NAME', 'importApp'),
    'charset': 'utf8mb4'
}

def main():
    # AWS S3クライアント初期化
    s3_client = boto3.client('s3', region_name=S3_REGION)
    
    # DB接続
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    
    try:
        # 移行対象データ取得
        query = """
        SELECT id, profile_image_url 
        FROM users 
        WHERE profile_image_url IS NOT NULL 
          AND profile_image_url NOT LIKE '%googleusercontent.com%' 
          AND profile_image_url NOT LIKE '%google.com%'
          AND profile_image_url LIKE '%uploads/profiles%'
        """
        cursor.execute(query)
        users = cursor.fetchall()
        
        logger.info(f"Found {len(users)} users with local images to migrate")
        
        success_count = 0
        error_count = 0
        
        for user in users:
            user_id = user['id']
            old_url = user['profile_image_url']
            
            try:
                # ローカルファイルパス取得
                filename = os.path.basename(urlparse(old_url).path)
                local_file_path = os.path.join(LOCAL_UPLOAD_DIR, filename)
                
                if not os.path.exists(local_file_path):
                    logger.warning(f"Local file not found for user {user_id}: {local_file_path}")
                    error_count += 1
                    continue
                
                # S3にアップロード
                content_type = get_content_type(filename)
                
                with open(local_file_path, 'rb') as file:
                    s3_client.upload_fileobj(
                        file,
                        S3_BUCKET,
                        filename,
                        ExtraArgs={
                            'ContentType': content_type,
                            'ACL': 'public-read'
                        }
                    )
                
                # 新しいURLを生成
                new_url = f"{S3_BASE_URL}/{filename}"
                
                # DB更新
                update_query = "UPDATE users SET profile_image_url = %s WHERE id = %s"
                cursor.execute(update_query, (new_url, user_id))
                
                logger.info(f"Migrated image for user {user_id}: {old_url} -> {new_url}")
                success_count += 1
                
            except Exception as e:
                logger.error(f"Failed to migrate image for user {user_id}: {e}")
                error_count += 1
        
        # トランザクションコミット
        conn.commit()
        
        logger.info(f"Migration completed. Success: {success_count}, Errors: {error_count}")
        
    except Exception as e:
        logger.error(f"Migration failed: {e}")
        conn.rollback()
        
    finally:
        cursor.close()
        conn.close()

def get_content_type(filename):
    ext = os.path.splitext(filename)[1].lower()
    content_types = {
        '.jpg': 'image/jpeg',
        '.jpeg': 'image/jpeg',
        '.png': 'image/png',
        '.webp': 'image/webp'
    }
    return content_types.get(ext, 'application/octet-stream')

if __name__ == "__main__":
    main()
```

#### 2.2 移行スクリプト実行
```bash
# 必要なPythonライブラリインストール
pip3 install boto3 mysql-connector-python

# 環境変数設定
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export DB_HOST=your_db_host
export DB_USER=your_db_user
export DB_PASSWORD=your_db_password
export DB_NAME=importApp

# 移行実行
python3 migrate_images_to_s3.py
```

### 3. 移行後検証

#### 3.1 データ整合性確認スクリプト
```bash
#!/bin/bash

# S3内のファイル数確認
echo "S3 bucket file count:"
aws s3 ls s3://sbm-app-profile-images/ --recursive | wc -l

# DB内のS3 URL数確認
mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD -D importApp -e "
SELECT COUNT(*) as s3_urls 
FROM users 
WHERE profile_image_url LIKE 'https://sbm-app-profile-images.s3.ap-northeast-1.amazonaws.com%'"

echo "Data migration verification completed"
```

---

## リスク分析と対策

### 高リスク項目

#### 1. データ消失リスク
**リスク**: 移行中にファイルが失われる可能性
**対策**:
- 移行前の完全バックアップ実施
- 段階的移行（一部ユーザーから開始）
- 移行後も既存ファイルを一定期間保持
- ロールバック手順の準備

#### 2. ダウンタイムリスク
**リスク**: 移行中にサービスが停止する可能性
**対策**:
- ゼロダウンタイム移行の実装
- Blue-Greenデプロイメント方式の採用
- リアルタイム切り替えによる瞬断のみ

#### 3. URL変更に伴う画像表示エラー
**リスク**: 既存の画像URLが無効になる
**対策**:
- データベース内URL一括更新
- CDN設定による旧URL→新URLリダイレクト
- 移行後の全面的な動作確認

### 中リスク項目

#### 4. パフォーマンス劣化
**リスク**: S3アクセスがローカルより遅い
**対策**:
- CloudFrontによるCDN配信の検討
- 適切なリージョン選択（ap-northeast-1）
- 画像サイズ最適化の維持

#### 5. AWS料金の増加
**リスク**: 想定以上のコストが発生
**対策**:
- 詳細なコスト見積もりと監視
- ライフサイクルポリシーによる古いファイルの自動削除
- 使用量アラートの設定

#### 6. セキュリティリスク
**リスク**: S3バケットの不適切な設定
**対策**:
- 最小権限の原則に基づくIAMロール設定
- バケットポリシーの適切な設定
- アクセスログの監視

### 低リスク項目

#### 7. 既存機能への影響
**リスク**: 関連機能が動作しなくなる
**対策**:
- 条件分岐による段階的切り替え
- 既存APIインターフェースの維持
- 徹底的な回帰テスト

---

## ロールバック手順

### 緊急時ロールバック（10分以内）

#### 1. 設定値による即座の切り替え
```bash
# 本番環境の環境変数を緊急変更
export STORAGE_TYPE=local

# アプリケーション再起動
sudo systemctl restart sbm-app
```

#### 2. ロードバランサーでの切り戻し
```bash
# 旧バージョンのインスタンスに切り替え
aws elbv2 modify-target-group --target-group-arn arn:aws:elasticloadbalancing:... \
  --targets Id=old-instance-id,Port=8080
```

### 完全ロールバック手順

#### 1. アプリケーションの切り戻し
```bash
# 旧バージョンのコードにロールバック
git checkout previous-stable-branch
./gradlew build
sudo systemctl restart sbm-app
```

#### 2. データベースの復旧
```sql
-- S3 URLをローカルURLに戻す（移行前のバックアップから復元）
-- バックアップテーブルを事前に作成していた場合
UPDATE users u 
JOIN users_backup b ON u.id = b.id 
SET u.profile_image_url = b.profile_image_url;
```

#### 3. ローカルファイルの復元
```bash
# バックアップから復元
sudo tar -xzf /tmp/profile_images_backup_YYYYMMDD.tar.gz -C /

# パーミッション修正
sudo chown -R sbm:sbm /srv/sbm/api/uploads/profiles/
sudo chmod -R 755 /srv/sbm/api/uploads/profiles/
```

### ロールバック判定基準
- **CPU使用率**: 90%以上が5分間継続
- **エラー率**: 5%以上
- **レスポンス時間**: 平均3秒以上
- **画像表示失敗率**: 1%以上

---

## テスト計画

### 1. 単体テスト

#### 1.1 S3StorageServiceTest
```java
@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {
    
    @Mock
    private S3Client s3Client;
    
    @Mock
    private S3Config.S3Properties s3Properties;
    
    @InjectMocks
    private S3StorageService s3StorageService;
    
    @Test
    void testUploadFile_Success() {
        // テスト実装
        when(s3Properties.getBucketName()).thenReturn("test-bucket");
        when(s3Properties.getBaseUrl()).thenReturn("https://test-bucket.s3.amazonaws.com");
        
        String result = s3StorageService.uploadFile("test.jpg", inputStream, 1024, "image/jpeg");
        
        assertThat(result).isEqualTo("https://test-bucket.s3.amazonaws.com/test.jpg");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
    
    @Test
    void testUploadFile_Failure() {
        // エラー時のテスト実装
    }
    
    @Test
    void testDeleteFile_Success() {
        // 削除成功のテスト実装
    }
    
    @Test
    void testFileExists_True() {
        // ファイル存在確認のテスト実装
    }
}
```

#### 1.2 ProfileImageServiceTest (S3対応版)
```java
@ExtendWith(MockitoExtension.class)
class ProfileImageServiceTest {
    
    @Mock
    private S3StorageService s3StorageService;
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private ProfileImageService profileImageService;
    
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ProfileImageService profileImageService() {
            ProfileImageService service = new ProfileImageService();
            ReflectionTestUtils.setField(service, "storageType", "s3");
            return service;
        }
    }
    
    @Test
    void testUploadProfileImage_S3_Success() {
        // S3アップロード成功のテスト
        when(userMapper.findById(anyLong())).thenReturn(createTestUser());
        when(s3StorageService.uploadFile(anyString(), any(), anyLong(), anyString()))
            .thenReturn("https://bucket.s3.amazonaws.com/test.jpg");
        
        ProfileImageResponseDto result = profileImageService.uploadProfileImage(createTestFile(), "1");
        
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProfileImageUrl()).contains("s3.amazonaws.com");
        verify(s3StorageService).uploadFile(anyString(), any(), anyLong(), anyString());
        verify(userMapper).updateProfileImageUrl(1, "https://bucket.s3.amazonaws.com/test.jpg");
    }
    
    @Test
    void testDeleteProfileImage_S3_Success() {
        // S3削除成功のテスト
    }
}
```

### 2. 統合テスト

#### 2.1 ProfileImageControllerIntegrationTest
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProfileImageControllerIntegrationTest {
    
    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
            .withServices(LocalStackContainer.Service.S3);
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private S3Client s3Client;
    
    @TestConfiguration
    static class TestS3Config {
        @Bean
        @Primary
        public S3Client testS3Client() {
            return S3Client.builder()
                    .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")))
                    .region(Region.US_EAST_1)
                    .build();
        }
    }
    
    @Test
    void testFullImageUploadFlow() {
        // 完全な画像アップロードフローのテスト
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource("src/test/resources/test-image.jpg"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + generateTestJwt());
        
        ResponseEntity<ProfileImageResponseDto> response = restTemplate.exchange(
                "/api/v1/users/profile-image",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                ProfileImageResponseDto.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getProfileImageUrl()).contains("s3");
        
        // S3に実際にファイルが保存されているか確認
        String key = extractKeyFromUrl(response.getBody().getProfileImageUrl());
        assertThat(s3Client.headObject(HeadObjectRequest.builder()
                .bucket("test-bucket")
                .key(key)
                .build())).isNotNull();
    }
}
```

### 3. パフォーマンステスト

#### 3.1 負荷テスト設定
```yaml
# load-test-config.yml
scenarios:
  - name: image_upload_test
    executor: ramping-vus
    stages:
      - duration: 2m
        target: 10
      - duration: 5m
        target: 50
      - duration: 2m
        target: 0
    env:
      BASE_URL: https://api.sbm-app.com
```

#### 3.2 k6負荷テストスクリプト
```javascript
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 10 },
    { duration: '5m', target: 50 },
    { duration: '2m', target: 0 },
  ],
};

export default function() {
  const url = 'https://api.sbm-app.com/api/v1/users/profile-image';
  
  const payload = {
    file: http.file(open('./test-image.jpg', 'b'), 'test-image.jpg', 'image/jpeg'),
  };
  
  const params = {
    headers: {
      'Authorization': 'Bearer ' + __ENV.JWT_TOKEN,
    },
  };
  
  let response = http.post(url, payload, params);
  
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 3000ms': (r) => r.timings.duration < 3000,
    'success field is true': (r) => r.json('success') === true,
  });
}
```

### 4. E2Eテスト

#### 4.1 Selenium WebDriver テスト
```java
@ExtendWith(SeleniumExtension.class)
class ProfileImageE2ETest {
    
    @Test
    void testImageUploadAndDisplayFlow(ChromeDriver driver) {
        // ログイン
        driver.get("https://sbm-app.com/login");
        login(driver, TEST_EMAIL, TEST_PASSWORD);
        
        // プロフィールページに移動
        driver.get("https://sbm-app.com/profile");
        
        // 画像アップロード
        WebElement fileInput = driver.findElement(By.id("profile-image-input"));
        fileInput.sendKeys("/path/to/test-image.jpg");
        
        WebElement uploadButton = driver.findElement(By.id("upload-button"));
        uploadButton.click();
        
        // アップロード完了まで待機
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("upload-success")));
        
        // 画像が表示されているか確認
        WebElement profileImage = driver.findElement(By.id("profile-image"));
        String imageUrl = profileImage.getAttribute("src");
        
        assertThat(imageUrl).contains("s3.ap-northeast-1.amazonaws.com");
        
        // 実際に画像が読み込めるか確認
        HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
        connection.setRequestMethod("GET");
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getContentType()).startsWith("image/");
    }
}
```

---

## 移行チェックリスト

### Phase 1: 準備フェーズ

#### AWS環境構築
- [ ] S3バケット作成（sbm-app-profile-images）
- [ ] バケットポリシー設定（public-read）
- [ ] CORS設定
- [ ] IAMユーザー/ロール作成
- [ ] アクセスキー生成・保存
- [ ] バケット暗号化設定（オプション）
- [ ] ライフサイクルポリシー設定（オプション）

#### コード実装
- [ ] build.gradle依存関係追加
- [ ] S3Config.java実装
- [ ] S3StorageService.java実装
- [ ] ProfileImageService.java更新
- [ ] application-prod.yml設定追加
- [ ] 単体テスト作成・実行
- [ ] 統合テスト作成・実行

#### インフラ準備
- [ ] 本番環境への環境変数設定
- [ ] ステージング環境構築
- [ ] 監視・アラート設定
- [ ] ログ設定更新

### Phase 2: 実装フェーズ

#### ステージング環境テスト
- [ ] S3アップロード機能テスト
- [ ] 画像削除機能テスト
- [ ] 画像表示確認
- [ ] パフォーマンステスト
- [ ] エラーハンドリング確認
- [ ] セキュリティテスト

#### 本番環境準備
- [ ] デプロイパッケージ作成
- [ ] Blue-Greenデプロイ準備
- [ ] ロールバック手順確認
- [ ] 監視ダッシュボード準備

### Phase 3: データ移行フェーズ

#### 移行前作業
- [ ] 既存データバックアップ
- [ ] 移行対象データ確認
- [ ] 移行スクリプト検証
- [ ] 移行時間見積もり

#### 移行実行
- [ ] メンテナンス告知
- [ ] 移行スクリプト実行
- [ ] データ整合性確認
- [ ] 画像表示確認
- [ ] エラーログ確認

#### 移行後確認
- [ ] 全ユーザーの画像表示確認
- [ ] 新規アップロードテスト
- [ ] 削除機能テスト
- [ ] パフォーマンス確認

### Phase 4: 本番リリースフェーズ

#### デプロイ実行
- [ ] Blue-Greenデプロイ実行
- [ ] ヘルスチェック確認
- [ ] 環境変数切り替え
- [ ] 動作確認

#### 切り替え後監視
- [ ] リアルタイム監視（1時間）
- [ ] エラー率監視
- [ ] レスポンス時間監視
- [ ] AWS料金監視

#### クリーンアップ
- [ ] 旧ローカルファイル削除（1週間後）
- [ ] 不要な設定項目削除
- [ ] 一時的なバックアップファイル削除
- [ ] ドキュメント更新

### 継続監視項目

#### 日次監視
- [ ] S3使用量・料金
- [ ] エラーログ
- [ ] 画像アップロード成功率
- [ ] レスポンス時間

#### 週次監視
- [ ] パフォーマンス傾向分析
- [ ] セキュリティログ確認
- [ ] バックアップ状況確認

#### 月次監視
- [ ] コスト分析・最適化検討
- [ ] ライフサイクルポリシー見直し
- [ ] セキュリティ監査

---

## 緊急時連絡先・エスカレーション

### 技術的問題
1. **Level 1**: 開発チーム（即時対応）
2. **Level 2**: インフラチーム（30分以内）
3. **Level 3**: AWS サポート（1時間以内）

### ビジネス影響
1. **軽微**: 開発リーダーに報告
2. **重大**: プロダクトマネージャーに報告
3. **緊急**: 全ステークホルダーに緊急連絡

---

## 付録

### A. 有用なAWS CLIコマンド集
```bash
# S3使用量確認
aws s3api list-objects-v2 --bucket sbm-app-profile-images --query 'Contents[].Size' --output text | awk '{s+=$1} END {print s/1024/1024 " MB"}'

# 大きなファイルの確認
aws s3api list-objects-v2 --bucket sbm-app-profile-images --query 'reverse(sort_by(Contents, &Size))[:10].[Key,Size]' --output table

# アクセスログの確認
aws logs filter-log-events --log-group-name /aws/s3/sbm-app-profile-images --start-time $(date -d '1 hour ago' +%s)000
```

### B. トラブルシューティング

#### よくある問題と解決法

**問題**: S3アップロードが403エラー
**解決**: IAMポリシーとバケットポリシーの確認

**問題**: 画像が表示されない
**解決**: CORSとContent-Typeの確認

**問題**: アップロードが遅い
**解決**: リージョンとネットワーク環境の確認

### C. パフォーマンス最適化Tips
- 画像リサイズの並列処理
- S3 Transfer Accelerationの活用
- CloudFrontの導入検討
- マルチパートアップロードの活用

---

## 変更履歴

| 日付 | バージョン | 変更内容 | 作成者 |
|------|------------|----------|--------|
| 2024-XX-XX | 1.0 | 初版作成 | 開発チーム |

---

**注意事項**: 
- 本計画書は実際の環境に合わせて調整してください
- セキュリティに関わる情報は適切に管理してください
- 移行前には必ず十分なテストを実施してください