package com.killiancorbel.realtimeapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.killiancorbel.realtimeapi.models.*;
import com.killiancorbel.realtimeapi.repositories.*;
import com.killiancorbel.realtimeapi.services.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping(path = "/privacy")
public class PrivacyController {

    private final Logger logger = LoggerFactory.getLogger(PrivacyController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private YukiRepository yukiRepository;
    @Autowired private ConversationHistoryRepository conversationHistoryRepository;
    @Autowired private FlashcardRepository flashcardRepository;
    @Autowired private DailyLessonRepository dailyLessonRepository;
    @Autowired private UserConsentRepository userConsentRepository;
    @Autowired private PrivacyRequestRepository privacyRequestRepository;
    @Autowired private AuditService auditService;

    private User getAuthenticatedUser(String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            User user = userRepository.findByUid(decodedToken.getUid());
            if (user == null) throw new AccessDeniedException("User not found");
            return user;
        } catch (Exception e) {
            throw new AccessDeniedException("Not authorized");
        }
    }

    // ==================== DATA EXPORT (RGPD Art. 20) ====================

    @PostMapping("/export")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportData(@RequestHeader("Authorization") String auth) {
        User user = getAuthenticatedUser(auth);
        auditService.logDataExportRequested(user.getUid());

        // Create privacy request
        PrivacyRequest req = new PrivacyRequest(user, "export");
        req.setStatus("completed");
        req.setFulfilledAt(LocalDateTime.now());
        privacyRequestRepository.save(req);

        // Collect all user data
        YukiData yd = yukiRepository.findByUser(user);
        Map<String, Object> export = new LinkedHashMap<>();

        // Account
        export.put("account", Map.of(
            "email", user.getEmail() != null ? user.getEmail() : "",
            "fullName", user.getFullName() != null ? user.getFullName() : "",
            "uid", user.getUid()
        ));

        // Learning data
        if (yd != null) {
            export.put("learning", Map.ofEntries(
                Map.entry("language", yd.getLanguage() != null ? yd.getLanguage() : ""),
                Map.entry("level", yd.getLevel()),
                Map.entry("xp", yd.getXp()),
                Map.entry("streak", yd.getStreak()),
                Map.entry("maxStreak", yd.getMaxStreak()),
                Map.entry("timeStudied", yd.getTimeStudied()),
                Map.entry("sentences", yd.getSentences()),
                Map.entry("vocabulary", yd.getVocabulary()),
                Map.entry("totalConversations", yd.getTotalConversations()),
                Map.entry("premium", yd.isPremium())
            ));
        }

        // Conversations
        var conversations = conversationHistoryRepository.findByUserOrderByCreatedAtDesc(
            user, org.springframework.data.domain.PageRequest.of(0, 1000));
        export.put("conversations", conversations.stream().map(c -> Map.of(
            "date", c.getCreatedAt().toString(),
            "language", c.getLanguage() != null ? c.getLanguage() : "",
            "duration", c.getDurationSeconds(),
            "transcript", c.getTranscript() != null ? c.getTranscript() : ""
        )).toList());

        // Flashcards
        var flashcards = flashcardRepository.findByUserAndLanguage(user, yd != null ? yd.getLanguage() : "");
        export.put("flashcards", flashcards.stream().map(f -> Map.of(
            "front", f.getFront(),
            "back", f.getBack(),
            "repetitions", f.getRepetitions(),
            "createdAt", f.getCreatedAt().toString()
        )).toList());

        // Consents
        var consents = userConsentRepository.findByUser(user);
        export.put("consents", consents.stream().map(c -> Map.of(
            "type", c.getConsentType(),
            "granted", c.isGranted(),
            "version", c.getConsentVersion() != null ? c.getConsentVersion() : "",
            "date", c.getCreatedAt().toString()
        )).toList());

        // Privacy requests history
        var requests = privacyRequestRepository.findByUser(user);
        export.put("privacyRequests", requests.stream().map(r -> Map.of(
            "type", r.getType(),
            "status", r.getStatus(),
            "requestedAt", r.getRequestedAt().toString()
        )).toList());

        auditService.logDataExportCompleted(user.getUid());
        return ResponseEntity.ok(export);
    }

    // ==================== ACCOUNT DELETION (RGPD Art. 17) ====================

    @PostMapping("/delete-account")
    @ResponseBody
    public ResponseEntity<Map<String, String>> requestDeletion(@RequestHeader("Authorization") String auth) {
        User user = getAuthenticatedUser(auth);
        auditService.logDeletionRequested(user.getUid());

        PrivacyRequest req = new PrivacyRequest(user, "deletion");
        privacyRequestRepository.save(req);

        // Execute deletion
        try {
            YukiData yd = yukiRepository.findByUser(user);
            if (yd != null) yukiRepository.delete(yd);

            // Delete conversations, flashcards, lessons
            // (cascade should handle most, but explicit is safer)
            var conversations = conversationHistoryRepository.findByUserOrderByCreatedAtDesc(
                user, org.springframework.data.domain.PageRequest.of(0, 10000));
            conversationHistoryRepository.deleteAll(conversations);

            var flashcards = flashcardRepository.findByUserAndLanguage(user, "");
            // Delete all flashcards for this user regardless of language
            flashcardRepository.deleteAll(flashcards);

            var consents = userConsentRepository.findByUser(user);
            userConsentRepository.deleteAll(consents);

            // Mark request as completed before deleting user
            req.setStatus("completed");
            req.setFulfilledAt(LocalDateTime.now());
            privacyRequestRepository.save(req);

            userRepository.delete(user);

            auditService.logDeletionCompleted(user.getUid());
            return ResponseEntity.ok(Map.of("status", "deleted"));
        } catch (Exception e) {
            logger.error("Deletion failed for user {}: {}", user.getUid(), e.getMessage());
            req.setStatus("failed");
            req.setDetails(e.getMessage());
            privacyRequestRepository.save(req);
            return ResponseEntity.internalServerError().body(Map.of("status", "failed", "error", e.getMessage()));
        }
    }

    // ==================== CONSENT MANAGEMENT ====================

    @PostMapping("/consent")
    @ResponseBody
    public ResponseEntity<String> updateConsent(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Object> body) {
        User user = getAuthenticatedUser(auth);
        String type = (String) body.get("type");
        boolean granted = (boolean) body.getOrDefault("granted", false);
        String version = (String) body.getOrDefault("version", "v1.0");
        String source = (String) body.getOrDefault("source", "settings");

        UserConsent existing = userConsentRepository.findByUserAndConsentType(user, type);
        if (existing != null) {
            existing.setGranted(granted);
            existing.setConsentVersion(version);
            existing.setSource(source);
            if (granted) {
                existing.setGrantedAt(LocalDateTime.now());
                existing.setRevokedAt(null);
            } else {
                existing.setRevokedAt(LocalDateTime.now());
            }
            userConsentRepository.save(existing);
        } else {
            UserConsent consent = new UserConsent(user, type, granted, version, source);
            userConsentRepository.save(consent);
        }

        auditService.logConsentUpdated(user.getUid(), type, granted);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/consents")
    @ResponseBody
    public List<UserConsent> getConsents(@RequestHeader("Authorization") String auth) {
        User user = getAuthenticatedUser(auth);
        return userConsentRepository.findByUser(user);
    }

    @GetMapping("/requests")
    @ResponseBody
    public List<PrivacyRequest> getPrivacyRequests(@RequestHeader("Authorization") String auth) {
        User user = getAuthenticatedUser(auth);
        return privacyRequestRepository.findByUser(user);
    }
}
