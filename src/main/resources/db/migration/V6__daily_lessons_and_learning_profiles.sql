-- V6: Daily lessons and user learning profiles

CREATE TABLE IF NOT EXISTS user_learning_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    weak_topics TEXT DEFAULT '[]',
    strong_topics TEXT DEFAULT '[]',
    common_mistakes TEXT DEFAULT '[]',
    vocabulary_mastered TEXT DEFAULT '[]',
    preferred_topics TEXT DEFAULT '[]',
    total_lessons_done INT DEFAULT 0,
    avg_session_minutes INT DEFAULT 0,
    learning_speed VARCHAR(20) DEFAULT 'normal',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS daily_lesson (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    language VARCHAR(50),
    level INT DEFAULT 0,
    title VARCHAR(500),
    scenario TEXT,
    objectives TEXT,
    vocabulary TEXT,
    conversation_script TEXT,
    review_questions TEXT,
    difficulty INT DEFAULT 5,
    xp_reward INT DEFAULT 30,
    completed BOOLEAN DEFAULT FALSE,
    lesson_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE INDEX idx_daily_lesson_user_date ON daily_lesson(user_id, lesson_date);
CREATE INDEX idx_daily_lesson_completed ON daily_lesson(user_id, completed);
