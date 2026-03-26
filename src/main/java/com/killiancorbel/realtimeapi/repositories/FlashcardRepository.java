package com.killiancorbel.realtimeapi.repositories;

import com.killiancorbel.realtimeapi.models.Flashcard;
import com.killiancorbel.realtimeapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {
    List<Flashcard> findByUserAndLanguage(User user, String language);
    List<Flashcard> findByUserAndLanguageAndNextReviewBefore(User user, String language, LocalDateTime before);
    long countByUserAndLanguage(User user, String language);
}
