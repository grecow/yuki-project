package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
public class PrivacyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String type;            // "export", "deletion", "rectification", "access"
    private String status;          // "pending", "processing", "completed", "rejected"
    private String details;
    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime fulfilledAt;
    private LocalDateTime deadline;  // RGPD: 30 days max

    public PrivacyRequest() {}

    public PrivacyRequest(User user, String type) {
        this.user = user;
        this.type = type;
        this.status = "pending";
        this.requestedAt = LocalDateTime.now();
        this.deadline = LocalDateTime.now().plusDays(30);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getFulfilledAt() { return fulfilledAt; }
    public void setFulfilledAt(LocalDateTime fulfilledAt) { this.fulfilledAt = fulfilledAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}
