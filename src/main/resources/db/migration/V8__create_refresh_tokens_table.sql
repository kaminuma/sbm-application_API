-- リフレッシュトークン管理テーブル（マルチデバイス対応）
CREATE TABLE `refresh_tokens` (
    `refresh_token_id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `token` VARCHAR(255) NOT NULL UNIQUE,
    `expires_at` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `device_info` VARCHAR(255) DEFAULT NULL COMMENT 'デバイス情報（User-Agent等）',
    `device_type` VARCHAR(50) DEFAULT 'UNKNOWN' COMMENT 'デバイスタイプ（WEB/MOBILE/TABLET）',
    `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'ログイン時のIPアドレス',
    `last_used_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最終使用日時',
    INDEX idx_user_id (`user_id`),
    INDEX idx_token (`token`),
    INDEX idx_expires_at (`expires_at`),
    INDEX idx_user_device (`user_id`, `device_type`),
    INDEX idx_last_used (`last_used_at`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='リフレッシュトークン管理（マルチデバイス対応）';