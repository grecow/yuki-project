package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table
public class DailyLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String language;
    private int level;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String scenario;            // Immersive context description

    @Column(columnDefinition = "TEXT")
    private String objectives;          // JSON array: ["Commander un plat", ...]

    @Column(columnDefinition = "TEXT")
    private String vocabulary;          // JSON array: [{front: "...", back: "..."}]

    @Column(columnDefinition = "TEXT")
    private String conversationScript;  // JSON array: [{role: "yuki"/"user_prompt", text: "..."}]

    @Column(columnDefinition = "TEXT")
    private String reviewQuestions;     // JSON array: [{question: "...", answer: "..."}]

    private int difficulty = 5;         // 1-10
    private int xpReward = 30;
    private boolean completed = false;
    private LocalDate lessonDate;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }

    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }

    public String getVocabulary() { return vocabulary; }
    public void setVocabulary(String vocabulary) { this.vocabulary = vocabulary; }

    public String getConversationScript() { return conversationScript; }
    public void setConversationScript(String conversationScript) { this.conversationScript = conversationScript; }

    public String getReviewQuestions() { return reviewQuestions; }
    public void setReviewQuestions(String reviewQuestions) { this.reviewQuestions = reviewQuestions; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
