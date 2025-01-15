-- Tabel user
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` INT AUTO_INCREMENT PRIMARY KEY,
                                      `username` VARCHAR(50) NOT NULL,
    `role` TINYINT(1) NOT NULL DEFAULT 0,
    `email` VARCHAR(100) NOT NULL,
    `password` VARCHAR(255) NOT NULL
    );

-- Tabel genre
CREATE TABLE IF NOT EXISTS `genre` (
                                       `id` INT AUTO_INCREMENT PRIMARY KEY,
                                       `name` VARCHAR(50) NOT NULL,
    `genre_image` MEDIUMBLOB
    );

-- Tabel game
CREATE TABLE IF NOT EXISTS `game` (
                                      `id` INT AUTO_INCREMENT PRIMARY KEY,
                                      `title` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `release_date` DATE,
    `developer` VARCHAR(100),
    `game_image` MEDIUMBLOB,
    `price` DOUBLE,
    `steam_link` VARCHAR(255)
    );

-- Tabel rating
CREATE TABLE IF NOT EXISTS `rating` (
                                        `id` INT AUTO_INCREMENT PRIMARY KEY,
                                        `user_id` INT NOT NULL,
                                        `game_id` INT NOT NULL,
                                        `rating_value` INT CHECK (`rating_value` >= 1 AND `rating_value` <= 5),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`game_id`) REFERENCES `game`(`id`)
    );

-- Tabel penghubung antara game dan genre
CREATE TABLE IF NOT EXISTS `game_genre` (
                                            `game_id` INT NOT NULL,
                                            `genre_id` INT NOT NULL,
                                            PRIMARY KEY (`game_id`, `genre_id`),
    FOREIGN KEY (`game_id`) REFERENCES `game`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`genre_id`) REFERENCES `genre`(`id`) ON DELETE CASCADE
    );

-- Tabel penghubung antara game dan genre
CREATE TABLE IF NOT EXISTS `tb_game_genre` (
                                               `id` INT AUTO_INCREMENT PRIMARY KEY,
                                               `game_id` INT NOT NULL,
                                               `genre_id` INT NOT NULL,
                                               FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE CASCADE
    );
