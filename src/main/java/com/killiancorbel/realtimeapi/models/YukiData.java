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
    @OneToMany
    private List<LessonDone> lessonsDone;
    @ManyToMany
    private List<Achievement> achievements;
    private int streak = 0;
    private int maxStreak = 0;
    private int timeStudied = 0;
    private int sentences = 0;
    private int vocabulary = 0;
    private boolean doneToday = false;
    private boolean notifications;

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
}
