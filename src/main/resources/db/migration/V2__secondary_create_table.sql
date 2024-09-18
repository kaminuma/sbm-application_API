-- create tasks table
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks` (
    `task_id` INT AUTO_INCREMENT PRIMARY KEY,
    `task_name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `due_date` DATE,
    `priority` VARCHAR(100),
    `status` VARCHAR(50),
    `project_id` INT,
    `user_id` INT,
    CONSTRAINT `fk_project`
        FOREIGN KEY (`project_id`) REFERENCES `projects`(`project_id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_user`
        FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
;