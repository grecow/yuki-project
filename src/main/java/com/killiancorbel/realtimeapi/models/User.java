package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Table
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique=true)
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }
    public String getPassword() {
        return password;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setPassword(String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encryptedPassword = bCryptPasswordEncoder.encode(password);
        this.password = encryptedPassword;
    }

    public void setPlainPassword(String password) {
        this.password = password;
    }
}
