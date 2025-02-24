package com.killiancorbel.realtimeapi.models.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.killiancorbel.realtimeapi.models.Achievement;
import com.killiancorbel.realtimeapi.models.LessonDone;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class YukiRes {
    private int tokens;
    private String prompt;
    private String email;
    private String fullName;
    private boolean premium;
    private int streak;
    private int maxStreak;
    private int timeStudied;
    private int sentences;
    private int vocabulary;
    private String language;
    private boolean doneToday;
    private List<Achievement> achievements;
    private List<LessonDone> lessonsDone;
    private boolean notifications;

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public int getTimeStudied() {
        return timeStudied;
    }

    public int getVocabulary() {
        return vocabulary;
    }

    public int getSentences() {
        return sentences;
    }

    public int getStreak() {
        return streak;
    }

    public void setDoneToday(boolean doneToday) {
        this.doneToday = doneToday;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    public void setTimeStudied(int timeStudied) {
        this.timeStudied = timeStudied;
    }

    public void setVocabulary(int vocabulary) {
        this.vocabulary = vocabulary;
    }

    public void setSentences(int sentences) {
        this.sentences = sentences;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public boolean isDoneToday() {
        return doneToday;
    }

    public List<LessonDone> getLessonsDone() {
        return lessonsDone;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setLessonsDone(List<LessonDone> lessonsDone) {
        this.lessonsDone = lessonsDone;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }
}
