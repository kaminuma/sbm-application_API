-- OAuth関連カラムの追加
ALTER TABLE users 
ADD COLUMN provider VARCHAR(20) DEFAULT 'LOCAL' COMMENT '認証プロバイダー（LOCAL/GOOGLE）',
ADD COLUMN google_id VARCHAR(255) NULL COMMENT 'GoogleユーザーID',
ADD COLUMN profile_image_url TEXT NULL COMMENT 'プロフィール画像URL',
ADD COLUMN is_email_verified BOOLEAN DEFAULT FALSE COMMENT 'メール認証済みフラグ';

-- パスワードをnull許可に変更（Googleユーザー用）
ALTER TABLE users 
MODIFY COLUMN password VARCHAR(255) NULL;

-- google_id専用の生成列を追加（NULLはそのまま、値がある場合はユニーク対象）
ALTER TABLE users
ADD COLUMN google_id_indexable VARCHAR(255) GENERATED ALWAYS AS (
    CASE WHEN google_id IS NULL THEN NULL ELSE google_id END
) STORED;

-- インデックス追加
CREATE UNIQUE INDEX idx_users_google_id ON users(google_id_indexable);
CREATE INDEX idx_users_provider ON users(provider);

-- 既存データ対応
UPDATE users
SET provider = 'LOCAL'
WHERE provider IS NULL;
