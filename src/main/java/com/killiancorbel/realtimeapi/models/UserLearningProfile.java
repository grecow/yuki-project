package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
public class UserLearningProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @Column(columnDefinition = "TEXT")
    private String weakTopics = "[]";           // JSON array of weak topic strings

    @Column(columnDefinition = "TEXT")
    private String strongTopics = "[]";         // JSON array of strong topic strings

    @Column(columnDefinition = "TEXT")
    private String commonMistakes = "[]";       // JSON array of mistake strings

    @Column(columnDefinition = "TEXT")
    private String vocabularyMastered = "[]";   // JSON array of words already learned

    @Column(columnDefinition = "TEXT")
    private String preferredTopics = "[]";      // JSON array of preferred topics

    private int totalLessonsDone = 0;
    private int avgSessionMinutes = 0;
    private String learningSpeed = "normal";    // slow, normal, fast

    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getWeakTopics() { return weakTopics; }
    public void setWeakTopics(String weakTopics) { this.weakTopics = weakTopics; }

    public String getStrongTopics() { return strongTopics; }
    public void setStrongTopics(String strongTopics) { this.strongTopics = strongTopics; }

    public String getCommonMistakes() { return commonMistakes; }
    public void setCommonMistakes(String commonMistakes) { this.commonMistakes = commonMistakes; }

    public String getVocabularyMastered() { return vocabularyMastered; }
    public void setVocabularyMastered(String vocabularyMastered) { this.vocabularyMastered = vocabularyMastered; }

    public String getPreferredTopics() { return preferredTopics; }
    public void setPreferredTopics(String preferredTopics) { this.preferredTopics = preferredTopics; }

    public int getTotalLessonsDone() { return totalLessonsDone; }
    public void setTotalLessonsDone(int totalLessonsDone) { this.totalLessonsDone = totalLessonsDone; }

    public int getAvgSessionMinutes() { return avgSessionMinutes; }
    public void setAvgSessionMinutes(int avgSessionMinutes) { this.avgSessionMinutes = avgSessionMinutes; }

    public String getLearningSpeed() { return learningSpeed; }
    public void setLearningSpeed(String learningSpeed) { this.learningSpeed = learningSpeed; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
