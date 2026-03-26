package com.killiancorbel.realtimeapi.services;

import com.killiancorbel.realtimeapi.models.AuditLog;
import com.killiancorbel.realtimeapi.repositories.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Async
    public void log(String userId, String action, String category, String details) {
        AuditLog log = new AuditLog(userId, action, category, details);
        auditLogRepository.save(log);
    }

    // Auth events
    public void logLogin(String userId) { log(userId, "login", "auth", null); }
    public void logLogout(String userId) { log(userId, "logout", "auth", null); }
    public void logRegister(String userId, String email) { log(userId, "register", "auth", "email: " + email); }

    // Product events
    public void logLessonStarted(String userId, String lessonTitle) { log(userId, "lesson_started", "product", lessonTitle); }
    public void logLessonCompleted(String userId, String lessonTitle, int xp) { log(userId, "lesson_completed", "product", lessonTitle + " +" + xp + "XP"); }
    public void logConversationStarted(String userId) { log(userId, "conversation_started", "product", null); }
    public void logConversationEnded(String userId, int durationSec) { log(userId, "conversation_ended", "product", durationSec + "s"); }
    public void logFlashcardReviewed(String userId, int quality) { log(userId, "flashcard_reviewed", "product", "quality: " + quality); }
    public void logStreakUpdated(String userId, int streak) { log(userId, "streak_updated", "product", "streak: " + streak); }
    public void logLevelUp(String userId, int newLevel) { log(userId, "level_up", "product", "level: " + newLevel); }

    // Privacy events
    public void logDataExportRequested(String userId) { log(userId, "data_export_requested", "privacy", null); }
    public void logDataExportCompleted(String userId) { log(userId, "data_export_completed", "privacy", null); }
    public void logDeletionRequested(String userId) { log(userId, "deletion_requested", "privacy", null); }
    public void logDeletionCompleted(String userId) { log(userId, "deletion_completed", "privacy", null); }
    public void logConsentUpdated(String userId, String type, boolean granted) { log(userId, "consent_updated", "privacy", type + ": " + granted); }

    // Billing events
    public void logTrialStarted(String userId) { log(userId, "trial_started", "billing", null); }
    public void logSubscriptionActivated(String userId, String plan) { log(userId, "subscription_activated", "billing", plan); }
    public void logSubscriptionCanceled(String userId) { log(userId, "subscription_canceled", "billing", null); }
    public void logPaymentFailed(String userId) { log(userId, "payment_failed", "billing", null); }

    // Admin events
    public void logAdminAccess(String adminId, String action, String targetUserId) { log(adminId, "admin_" + action, "admin", "target: " + targetUserId); }
}
