package com.killiancorbel.realtimeapi.models.responses;

public class LoginRes {
    private String email;
    private String token;

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }
}