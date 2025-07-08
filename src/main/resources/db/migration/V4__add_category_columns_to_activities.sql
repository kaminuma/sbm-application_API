-- activitiesテーブルにカテゴリ情報を追加
ALTER TABLE activities
  ADD COLUMN category VARCHAR(32) NOT NULL DEFAULT '',
  ADD COLUMN category_sub VARCHAR(64) NOT NULL DEFAULT ''; 