package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
public class Flashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String language;
    private String front;       // Word/phrase in target language
    private String back;        // Translation in user's language

    // Spaced repetition fields
    private int repetitions = 0;
    private double easeFactor = 2.5;
    private int intervalDays = 1;
    private LocalDateTime nextReview;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }

    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }

    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    public double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public LocalDateTime getNextReview() { return nextReview; }
    public void setNextReview(LocalDateTime nextReview) { this.nextReview = nextReview; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * SM-2 spaced repetition algorithm.
     * quality: 0-5 (0=total blackout, 5=perfect)
     */
    public void review(int quality) {
        if (quality < 3) {
            repetitions = 0;
            intervalDays = 1;
        } else {
            if (repetitions == 0) {
                intervalDays = 1;
            } else if (repetitions == 1) {
                intervalDays = 6;
            } else {
                intervalDays = (int) Math.round(intervalDays * easeFactor);
            }
            repetitions++;
        }
        easeFactor = Math.max(1.3, easeFactor + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        nextReview = LocalDateTime.now().plusDays(intervalDays);
    }
}
