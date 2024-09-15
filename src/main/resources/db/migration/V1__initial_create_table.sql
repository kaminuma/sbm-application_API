-- create user table
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;

-- create projects table
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects` (
    `project_id` INT AUTO_INCREMENT PRIMARY KEY,
    `project_name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `user_id` INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;