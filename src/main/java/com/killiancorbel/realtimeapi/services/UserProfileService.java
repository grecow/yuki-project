package com.killiancorbel.realtimeapi.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.killiancorbel.realtimeapi.models.User;
import com.killiancorbel.realtimeapi.models.UserLearningProfile;
import com.killiancorbel.realtimeapi.repositories.UserLearningProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserProfileService {

    private final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Pattern CORRECTION_PATTERN = Pattern.compile("\\[CORRECTION:\\s*(.+?)\\s*→\\s*(.+?)\\]");

    @Autowired
    private UserLearningProfileRepository profileRepository;

    /**
     * Get or create a learning profile for a user.
     */
    public UserLearningProfile getOrCreateProfile(User user) {
        UserLearningProfile profile = profileRepository.findByUser(user);
        if (profile == null) {
            profile = new UserLearningProfile();
            profile.setUser(user);
            profileRepository.save(profile);
        }
        return profile;
    }

    /**
     * Update profile after a conversation.
     * Extracts corrections, identifies weak areas, adds vocabulary.
     */
    public void updateAfterConversation(User user, String transcript, String corrections, int durationMinutes) {
        UserLearningProfile profile = getOrCreateProfile(user);

        try {
            // Extract mistakes from corrections
            List<String> mistakes = parseJsonArray(profile.getCommonMistakes());
            if (corrections != null && !corrections.isEmpty()) {
                Matcher matcher = CORRECTION_PATTERN.matcher(corrections);
                while (matcher.find()) {
                    String mistake = matcher.group(1).trim();
                    if (!mistakes.contains(mistake) && mistakes.size() < 50) {
                        mistakes.add(mistake);
                    }
                }
                // Keep only last 30 mistakes
                if (mistakes.size() > 30) {
                    mistakes = new ArrayList<>(mistakes.subList(mistakes.size() - 30, mistakes.size()));
                }
                profile.setCommonMistakes(mapper.writeValueAsString(mistakes));
            }

            // Update session stats
            profile.setTotalLessonsDone(profile.getTotalLessonsDone() + 1);
            int currentAvg = profile.getAvgSessionMinutes();
            int total = profile.getTotalLessonsDone();
            profile.setAvgSessionMinutes(((currentAvg * (total - 1)) + durationMinutes) / total);

            // Update learning speed based on mistake rate
            if (corrections == null || corrections.isEmpty()) {
                profile.setLearningSpeed("fast");
            } else {
                long correctionCount = CORRECTION_PATTERN.matcher(corrections).results().count();
                if (correctionCount <= 1) {
                    profile.setLearningSpeed("fast");
                } else if (correctionCount <= 3) {
                    profile.setLearningSpeed("normal");
                } else {
                    profile.setLearningSpeed("slow");
                }
            }

            profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
        } catch (Exception e) {
            logger.error("Failed to update learning profile: {}", e.getMessage());
        }
    }

    /**
     * Add mastered vocabulary words to the profile.
     */
    public void addVocabulary(User user, List<String> words) {
        UserLearningProfile profile = getOrCreateProfile(user);
        try {
            List<String> vocab = parseJsonArray(profile.getVocabularyMastered());
            for (String word : words) {
                if (!vocab.contains(word)) {
                    vocab.add(word);
                }
            }
            // Keep last 500 words
            if (vocab.size() > 500) {
                vocab = new ArrayList<>(vocab.subList(vocab.size() - 500, vocab.size()));
            }
            profile.setVocabularyMastered(mapper.writeValueAsString(vocab));
            profileRepository.save(profile);
        } catch (Exception e) {
            logger.error("Failed to add vocabulary: {}", e.getMessage());
        }
    }

    private List<String> parseJsonArray(String json) {
        try {
            if (json == null || json.isEmpty()) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
