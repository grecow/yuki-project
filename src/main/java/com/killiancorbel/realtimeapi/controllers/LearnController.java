package com.killiancorbel.realtimeapi.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.killiancorbel.realtimeapi.models.*;
import com.killiancorbel.realtimeapi.repositories.*;
import com.killiancorbel.realtimeapi.services.LessonGeneratorService;
import com.killiancorbel.realtimeapi.services.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/learn")
public class LearnController {

    private final Logger logger = LoggerFactory.getLogger(LearnController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YukiRepository yukiRepository;
    @Autowired
    private ConversationHistoryRepository conversationHistoryRepository;
    @Autowired
    private FlashcardRepository flashcardRepository;
    @Autowired
    private DailyLessonRepository dailyLessonRepository;
    @Autowired
    private LessonGeneratorService lessonGeneratorService;
    @Autowired
    private UserProfileService userProfileService;

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

    // ========== DAILY LESSONS ==========

    @GetMapping("/daily-lessons")
    @ResponseBody
    public List<DailyLesson> getDailyLessons(@RequestHeader("Authorization") String auth) {
        User user = getAuthenticatedUser(auth);
        List<DailyLesson> lessons = dailyLessonRepository.findByUserAndLessonDate(user, LocalDate.now());
        // If no lesson for today, generate one on-demand
        if (lessons.isEmpty()) {
            YukiData yd = yukiRepository.findByUser(user);
            if (yd != null && yd.getLanguage() != null) {
                DailyLesson lesson = lessonGeneratorService.generateLesson(user, yd);
                if (lesson != null) lessons = List.of(lesson);
            }
        }
        return lessons;
    }

    @PostMapping("/daily-lesson/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeDailyLesson(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> body) {
        User user = getAuthenticatedUser(auth);
        DailyLesson lesson = dailyLessonRepository.findById(id)
                .orElseThrow(() -> new AccessDeniedException("Lesson not found"));
        lesson.setCompleted(true);
        dailyLessonRepository.save(lesson);

        // Update XP
        YukiData yd = yukiRepository.findByUser(user);
        if (yd != null) {
            yd.addXp(lesson.getXpReward());
            yd.setTotalConversations(yd.getTotalConversations() + 1);
            yd.setDailyConversationsUsed(yd.getDailyConversationsUsed() + 1);
            if (!yd.isDoneToday()) {
                yd.setStreak(yd.getStreak() + 1);
                if (yd.getStreak() > yd.getMaxStreak()) yd.setMaxStreak(yd.getStreak());
            }
            yd.setDoneToday(true);
            yukiRepository.save(yd);
        }

        // Update learning profile
        String corrections = body != null ? (String) body.getOrDefault("corrections", "") : "";
        int duration = body != null ? (int) body.getOrDefault("durationMinutes", 5) : 5;
        userProfileService.updateAfterConversation(user, "", corrections, duration);

        return ResponseEntity.ok(Map.of(
                "xpGained", lesson.getXpReward(),
                "totalXp", yd != null ? yd.getXp() : 0,
                "level", yd != null ? yd.getCalculatedLevel() : 1
        ));
    }

    // ========== CONVERSATION HISTORY ==========

    @PostMapping("/conversation/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveConversation(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Object> body) {
        User user = getAuthenticatedUser(auth);
        YukiData yd = yukiRepository.findByUser(user);

        ConversationHistory ch = new ConversationHistory();
        ch.setUser(user);
        ch.setLanguage(yd != null ? yd.getLanguage() : "");
        ch.setDurationSeconds((int) body.getOrDefault("durationSeconds", 0));
        ch.setSentencesCount((int) body.getOrDefault("sentencesCount", 0));
        ch.setXpGained((int) body.getOrDefault("xpGained", 0));
        ch.setTranscript((String) body.getOrDefault("transcript", ""));
        ch.setCorrections((String) body.getOrDefault("corrections", ""));
        conversationHistoryRepository.save(ch);

        return ResponseEntity.ok(Map.of("id", ch.getId()));
    }

    @GetMapping("/conversations")
    @ResponseBody
    public List<ConversationHistory> getConversations(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = getAuthenticatedUser(auth);
        return conversationHistoryRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size));
    }

    // ========== FLASHCARDS ==========

    @PostMapping("/flashcard")
    @ResponseBody
    public ResponseEntity<Flashcard> createFlashcard(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, String> body) {
        User user = getAuthenticatedUser(auth);
        YukiData yd = yukiRepository.findByUser(user);

        Flashcard fc = new Flashcard();
        fc.setUser(user);
        fc.setLanguage(yd != null ? yd.getLanguage() : body.getOrDefault("language", ""));
        fc.setFront(body.get("front"));
        fc.setBack(body.get("back"));
        fc.setNextReview(LocalDateTime.now());
        flashcardRepository.save(fc);

        return ResponseEntity.ok(fc);
    }

    @GetMapping("/flashcards")
    @ResponseBody
    public List<Flashcard> getFlashcards(@RequestHeader("Authorization") String auth) {
        User user = getAuthenticatedUser(auth);
        YukiData yd = yukiRepository.findByUser(user);
        String language = yd != null ? yd.getLanguage() : "";
        return flashcardRepository.findByUserAndLanguage(user, language);
    }

    @GetMapping("/flashcards/due")
    @ResponseBody
    public List<Flashcard> getDueFlashcards(@RequestHeader("Authorization") String auth) {
        User user = getAuthenticatedUser(auth);
        YukiData yd = yukiRepository.findByUser(user);
        String language = yd != null ? yd.getLanguage() : "";
        return flashcardRepository.findByUserAndLanguageAndNextReviewBefore(user, language, LocalDateTime.now());
    }

    @PostMapping("/flashcard/{id}/review")
    @ResponseBody
    public ResponseEntity<Flashcard> reviewFlashcard(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        getAuthenticatedUser(auth);
        Flashcard fc = flashcardRepository.findById(id)
                .orElseThrow(() -> new AccessDeniedException("Flashcard not found"));
        int quality = body.getOrDefault("quality", 3);
        fc.review(Math.max(0, Math.min(5, quality)));
        flashcardRepository.save(fc);
        return ResponseEntity.ok(fc);
    }

    @DeleteMapping("/flashcard/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteFlashcard(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer id) {
        getAuthenticatedUser(auth);
        flashcardRepository.deleteById(id);
        return ResponseEntity.ok("ok");
    }
}
