-- V4: Add conversation history and flashcards tables

CREATE TABLE IF NOT EXISTS conversation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    language VARCHAR(50),
    duration_seconds INT DEFAULT 0,
    sentences_count INT DEFAULT 0,
    xp_gained INT DEFAULT 0,
    transcript TEXT,
    corrections TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS flashcard (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    language VARCHAR(50),
    front VARCHAR(500) NOT NULL,
    back VARCHAR(500) NOT NULL,
    repetitions INT DEFAULT 0,
    ease_factor DOUBLE DEFAULT 2.5,
    interval_days INT DEFAULT 1,
    next_review TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE INDEX idx_conv_user_date ON conversation_history(user_id, created_at DESC);
CREATE INDEX idx_flash_user_lang ON flashcard(user_id, language);
CREATE INDEX idx_flash_review ON flashcard(user_id, language, next_review);
