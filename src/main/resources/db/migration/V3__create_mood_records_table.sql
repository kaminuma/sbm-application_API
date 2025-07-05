-- create mood_records table
DROP TABLE IF EXISTS `mood_records`;
CREATE TABLE `mood_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `date` DATE NOT NULL,
    `mood` INT NOT NULL CHECK (`mood` >= 1 AND `mood` <= 5),
    `note` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` BOOLEAN DEFAULT FALSE,
    UNIQUE KEY `unique_user_date` (`user_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- create index for better performance
CREATE INDEX `idx_mood_records_user_date` ON `mood_records`(`user_id`, `date`); 