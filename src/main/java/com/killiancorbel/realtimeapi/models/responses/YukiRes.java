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
    private int xp;
    private int userLevel;
    private int xpForNextLevel;
    private int goal;
    private boolean discoveryDone;
    private boolean inTrial;
    private int trialDaysLeft;
    private boolean canStartConversation;
    private boolean correctionsEnabled;
    private int totalConversations;

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

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public int getXpForNextLevel() {
        return xpForNextLevel;
    }

    public void setXpForNextLevel(int xpForNextLevel) {
        this.xpForNextLevel = xpForNextLevel;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public boolean isDiscoveryDone() { return discoveryDone; }
    public void setDiscoveryDone(boolean discoveryDone) { this.discoveryDone = discoveryDone; }

    public boolean isInTrial() { return inTrial; }
    public void setInTrial(boolean inTrial) { this.inTrial = inTrial; }

    public int getTrialDaysLeft() { return trialDaysLeft; }
    public void setTrialDaysLeft(int trialDaysLeft) { this.trialDaysLeft = trialDaysLeft; }

    public boolean isCanStartConversation() { return canStartConversation; }
    public void setCanStartConversation(boolean canStartConversation) { this.canStartConversation = canStartConversation; }

    public boolean isCorrectionsEnabled() { return correctionsEnabled; }
    public void setCorrectionsEnabled(boolean correctionsEnabled) { this.correctionsEnabled = correctionsEnabled; }

    public int getTotalConversations() { return totalConversations; }
    public void setTotalConversations(int totalConversations) { this.totalConversations = totalConversations; }
}
