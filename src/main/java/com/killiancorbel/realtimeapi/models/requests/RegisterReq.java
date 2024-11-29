package com.killiancorbel.realtimeapi.models.requests;

public class RegisterReq {
    private String email;
    private String password;


    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

