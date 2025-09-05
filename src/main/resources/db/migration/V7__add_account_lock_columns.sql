-- アカウントロック機能のためのカラム追加
ALTER TABLE users 
ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0 COMMENT 'ログイン失敗回数',
ADD COLUMN account_locked_until TIMESTAMP NULL DEFAULT NULL COMMENT 'アカウントロック解除時刻';