-- insert initial user
INSERT INTO users (username, password, email) VALUES
('user', 'password', 'user@example.com');

-- Insert initial test data into project table
INSERT INTO projects (project_id, project_name, description, user_id) VALUES
('1000', 'PC業務', 'test', '1'),
('1001', 'プレゼン資料作成', 'test', '1'),
('1002', 'プログラミング', 'test', '1');