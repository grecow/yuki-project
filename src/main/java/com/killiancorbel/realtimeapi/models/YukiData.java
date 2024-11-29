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
}
