package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;

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
}
