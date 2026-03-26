package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table
public class YukiData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private User user;
    private int tokens;
    private int level;
    private int goal = 0;
    private String language;
    private boolean toCorrect;
    private boolean premium = false;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonDone> lessonsDone;
    @ManyToMany
    private List<Achievement> achievements;
    private int streak = 0;
    private int maxStreak = 0;
    private int timeStudied = 0;
    private int sentences = 0;
    private int vocabulary = 0;
    private boolean doneToday = false;
    private boolean notifications = false;
    private int xp = 0;
    private boolean discoveryDone = false;
    private java.time.LocalDateTime trialStartDate;
    private java.time.LocalDateTime trialEndDate;
    private int dailyConversationsUsed = 0;
    private int totalConversations = 0;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public int getTokens() {
        return tokens;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public int getLevel() {
        return level;
    }


    public String getLanguage() {
        return language;
    }

    public void setLevel(int level) {
        this.level = level;
    }


    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isToCorrect() {
        return toCorrect;
    }

    public void setToCorrect(boolean toCorrect) {
        this.toCorrect = toCorrect;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public List<LessonDone> getLessonsDone() {
        return lessonsDone;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public void setLessonsDone(List<LessonDone> lessonsDone) {
        this.lessonsDone = lessonsDone;
    }

    public void addAchievement(Achievement achievement) {
        this.achievements.add(achievement);
    }

    public void addLessonsDone(LessonDone e) {
        this.lessonsDone.add(e);
    }

    public int getSentences() {
        return sentences;
    }

    public int getStreak() {
        return streak;
    }

    public int getTimeStudied() {
        return timeStudied;
    }

    public int getVocabulary() {
        return vocabulary;
    }

    public void setSentences(int sentences) {
        this.sentences = sentences;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void setTimeStudied(int timeStudied) {
        this.timeStudied = timeStudied;
    }

    public void setVocabulary(int vocabulary) {
        this.vocabulary = vocabulary;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public boolean isDoneToday() {
        return doneToday;
    }

    public void setDoneToday(boolean doneToday) {
        this.doneToday = doneToday;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void addXp(int amount) {
        this.xp += amount;
    }

    public boolean isDiscoveryDone() { return discoveryDone; }
    public void setDiscoveryDone(boolean discoveryDone) { this.discoveryDone = discoveryDone; }

    public java.time.LocalDateTime getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(java.time.LocalDateTime trialStartDate) { this.trialStartDate = trialStartDate; }

    public java.time.LocalDateTime getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(java.time.LocalDateTime trialEndDate) { this.trialEndDate = trialEndDate; }

    public int getDailyConversationsUsed() { return dailyConversationsUsed; }
    public void setDailyConversationsUsed(int dailyConversationsUsed) { this.dailyConversationsUsed = dailyConversationsUsed; }

    public int getTotalConversations() { return totalConversations; }
    public void setTotalConversations(int totalConversations) { this.totalConversations = totalConversations; }

    public boolean isInTrial() {
        if (trialStartDate == null || trialEndDate == null) return false;
        var now = java.time.LocalDateTime.now();
        return now.isAfter(trialStartDate) && now.isBefore(trialEndDate);
    }

    public int getTrialDaysLeft() {
        if (trialEndDate == null) return 0;
        long days = java.time.Duration.between(java.time.LocalDateTime.now(), trialEndDate).toDays();
        return Math.max(0, (int) days);
    }

    /**
     * Can the user start a conversation right now?
     * Premium/trial = unlimited. Free = 1/day.
     */
    public boolean canStartConversation() {
        if (premium || isInTrial()) return true;
        return dailyConversationsUsed < 1;
    }

    /**
     * Calculate user level based on XP.
     * Each level requires progressively more XP.
     * Level 1: 0 XP, Level 2: 100 XP, Level 3: 300 XP, etc.
     */
    public int getCalculatedLevel() {
        if (xp < 100) return 1;
        if (xp < 300) return 2;
        if (xp < 600) return 3;
        if (xp < 1000) return 4;
        if (xp < 1500) return 5;
        if (xp < 2100) return 6;
        if (xp < 2800) return 7;
        if (xp < 3600) return 8;
        if (xp < 4500) return 9;
        return 10 + (xp - 4500) / 1000;
    }

    /**
     * XP needed to reach the next level.
     */
    public int getXpForNextLevel() {
        int[] thresholds = {100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500};
        for (int t : thresholds) {
            if (xp < t) return t;
        }
        int currentLevel = getCalculatedLevel();
        return 4500 + (currentLevel - 9) * 1000;
    }
}
