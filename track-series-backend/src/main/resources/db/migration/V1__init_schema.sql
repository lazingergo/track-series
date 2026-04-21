CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS series (
    id BIGINT NOT NULL,
    title VARCHAR(255),
    summary TEXT,
    image_url VARCHAR(255),
    status VARCHAR(255),
    premiered DATE,
    ended DATE,
    PRIMARY KEY (id)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS episode (
    id BIGINT NOT NULL,
    title VARCHAR(255),
    season_number INT,
    episode_number INT,
    airdate DATE,
    summary TEXT,
    series_id BIGINT,
    watchable TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_episode_series_id (series_id),
    CONSTRAINT fk_episode_series FOREIGN KEY (series_id) REFERENCES series(id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS genre (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_genre_name (name)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS series_genre (
    series_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (series_id, genre_id),
    KEY idx_series_genre_genre_id (genre_id),
    CONSTRAINT fk_series_genre_series FOREIGN KEY (series_id) REFERENCES series(id) ON DELETE CASCADE,
    CONSTRAINT fk_series_genre_genre FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS tracked_series (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    series_id BIGINT NOT NULL,
    status VARCHAR(64) NOT NULL,
    rating INT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tracked_series_user_series (user_id, series_id),
    KEY idx_tracked_series_series_id (series_id),
    CONSTRAINT fk_tracked_series_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_tracked_series_series FOREIGN KEY (series_id) REFERENCES series(id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS watched_episodes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    episode_id BIGINT NOT NULL,
    watched_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_watched_episodes_user_episode (user_id, episode_id),
    KEY idx_watched_episodes_episode_id (episode_id),
    CONSTRAINT fk_watched_episodes_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_watched_episodes_episode FOREIGN KEY (episode_id) REFERENCES episode(id) ON DELETE CASCADE
) ENGINE = InnoDB;