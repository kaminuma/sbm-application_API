-- AI API利用制限テーブル作成（将来の拡張を考慮した汎用名）
CREATE TABLE ai_api_usage_limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'gemini',
    date DATE NOT NULL,
    daily_count INT NOT NULL DEFAULT 0,
    monthly_count INT NOT NULL DEFAULT 0,  -- 将来の月次制限用（現在は参照のみ）
    last_reset_date DATE,                   -- 将来の月次リセット管理用
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_provider_date (user_id, provider, date),
    INDEX idx_user_month (user_id, last_reset_date),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- usersテーブルにAI利用制限カラム追加
ALTER TABLE users ADD COLUMN ai_daily_limit INT NOT NULL DEFAULT 5;
ALTER TABLE users ADD COLUMN ai_monthly_limit INT NOT NULL DEFAULT 50;  -- 将来の月次制限用（現在は未使用）