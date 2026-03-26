package com.killiancorbel.realtimeapi.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
public class UserConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String consentType;      // "analytics", "marketing", "notifications", "data_processing"
    private boolean granted;
    private String consentVersion;   // e.g. "v1.2" — ties to your privacy policy version
    private String source;           // "onboarding", "settings", "banner"
    private LocalDateTime grantedAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserConsent() {}

    public UserConsent(User user, String consentType, boolean granted, String version, String source) {
        this.user = user;
        this.consentType = consentType;
        this.granted = granted;
        this.consentVersion = version;
        this.source = source;
        this.grantedAt = granted ? LocalDateTime.now() : null;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }
    public boolean isGranted() { return granted; }
    public void setGranted(boolean granted) { this.granted = granted; }
    public String getConsentVersion() { return consentVersion; }
    public void setConsentVersion(String consentVersion) { this.consentVersion = consentVersion; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
