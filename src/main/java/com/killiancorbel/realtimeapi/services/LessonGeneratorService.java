package com.killiancorbel.realtimeapi.services;

import com.killiancorbel.realtimeapi.models.*;
import com.killiancorbel.realtimeapi.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class LessonGeneratorService {

    private final Logger logger = LoggerFactory.getLogger(LessonGeneratorService.class);

    @Value("${app.openai-api-key}")
    private String openaiApiKey;

    @Value("${app.lesson-model:gpt-5-mini}")
    private String lessonModel;

    @Autowired
    private DailyLessonRepository dailyLessonRepository;
    @Autowired
    private UserLearningProfileRepository profileRepository;
    @Autowired
    private YukiRepository yukiRepository;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    private static final Map<String, String> LEVEL_NAMES = Map.of(
            "0", "A1 (beginner)", "1", "A2 (elementary)",
            "2", "B1 (intermediate)", "3", "B2 (upper-intermediate)"
    );

    private static final Map<String, String> LANGUAGE_NATIVE_NAMES = Map.ofEntries(
            Map.entry("French", "français"), Map.entry("Spanish", "español"),
            Map.entry("Russian", "русский"), Map.entry("Italian", "italiano"),
            Map.entry("German", "Deutsch"), Map.entry("Portuguese", "português"),
            Map.entry("Japanese", "日本語"), Map.entry("Korean", "한국어"),
            Map.entry("Chinese", "中文"), Map.entry("English", "English"),
            Map.entry("Arabic", "العربية"), Map.entry("Hindi", "हिन्दी"),
            Map.entry("Turkish", "Türkçe")
    );

    /**
     * Generate a personalized daily lesson for a user.
     */
    public DailyLesson generateLesson(User user, YukiData yukiData) {
        String language = yukiData.getLanguage();
        int level = yukiData.getLevel();
        UserLearningProfile profile = profileRepository.findByUser(user);

        String prompt = buildGenerationPrompt(language, level, profile);

        try {
            String response = callOpenAI(prompt);
            return parseLessonResponse(response, user, language, level);
        } catch (Exception e) {
            logger.error("Failed to generate lesson for user {}: {}", user.getUid(), e.getMessage());
            return null;
        }
    }

    private String buildGenerationPrompt(String language, int level, UserLearningProfile profile) {
        String nativeName = LANGUAGE_NATIVE_NAMES.getOrDefault(language, language);
        String levelName = LEVEL_NAMES.getOrDefault(String.valueOf(level), "A1 (beginner)");

        StringBuilder sb = new StringBuilder();
        sb.append("Generate a personalized daily speaking lesson in ").append(language);
        sb.append(" for a ").append(levelName).append(" student.\n\n");

        if (profile != null) {
            sb.append("Student profile:\n");
            sb.append("- Weak areas: ").append(profile.getWeakTopics()).append("\n");
            sb.append("- Strong areas: ").append(profile.getStrongTopics()).append("\n");
            sb.append("- Common mistakes: ").append(profile.getCommonMistakes()).append("\n");
            sb.append("- Vocabulary already learned: ").append(profile.getVocabularyMastered()).append("\n");
            sb.append("- Preferred topics: ").append(profile.getPreferredTopics()).append("\n");
            sb.append("- Learning speed: ").append(profile.getLearningSpeed()).append("\n");
            sb.append("- Total lessons done: ").append(profile.getTotalLessonsDone()).append("\n\n");
        }

        sb.append("CRITICAL: ALL lesson content (title, scenario, vocabulary front, conversation) ");
        sb.append("MUST be in ").append(nativeName).append(". ");
        sb.append("NEVER use English in the lesson content. ");
        sb.append("Only the vocabulary 'back' field should be in the student's native language (English).\n\n");

        sb.append("Generate a structured JSON lesson with EXACTLY this format:\n");
        sb.append("{\n");
        sb.append("  \"title\": \"lesson title in ").append(nativeName).append("\",\n");
        sb.append("  \"scenario\": \"immersive scenario description in ").append(nativeName).append("\",\n");
        sb.append("  \"objectives\": [\"objective 1 in ").append(nativeName).append("\", ...],\n");
        sb.append("  \"vocabulary\": [{\"front\": \"word in ").append(nativeName).append("\", \"back\": \"translation in English\"}, ...],\n");
        sb.append("  \"conversationScript\": [\n");
        sb.append("    {\"role\": \"yuki\", \"text\": \"Yuki's line in ").append(nativeName).append("\"},\n");
        sb.append("    {\"role\": \"user_prompt\", \"text\": \"prompt for the user to respond in ").append(nativeName).append("\"},\n");
        sb.append("    ...\n");
        sb.append("  ],\n");
        sb.append("  \"reviewQuestions\": [{\"question\": \"question in ").append(nativeName).append("\", \"answer\": \"answer in ").append(nativeName).append("\"}, ...],\n");
        sb.append("  \"difficulty\": 5,\n");
        sb.append("  \"xpReward\": 30\n");
        sb.append("}\n\n");

        sb.append("Requirements:\n");
        sb.append("- 6-8 vocabulary words the student hasn't learned yet\n");
        sb.append("- 6-8 conversation exchanges (alternating yuki/user_prompt)\n");
        sb.append("- 3 review questions\n");
        sb.append("- Focus on the student's weak areas while being engaging\n");
        sb.append("- The scenario should be practical and real-life (restaurant, travel, shopping, etc.)\n");
        sb.append("- Adapt difficulty to the student's level and learning speed\n");
        sb.append("- Respond ONLY with the JSON, no other text\n");

        return sb.toString();
    }

    private String callOpenAI(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", lessonModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a language lesson generator. Always respond with valid JSON only."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.8,
                "max_tokens", 4096
        );

        Map response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) throw new RuntimeException("Empty response from OpenAI");

        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private DailyLesson parseLessonResponse(String jsonResponse, User user, String language, int level) {
        // Clean potential markdown code blocks
        String clean = jsonResponse.trim();
        if (clean.startsWith("```")) {
            clean = clean.replaceFirst("```json\\s*", "").replaceFirst("```\\s*$", "");
        }

        DailyLesson lesson = new DailyLesson();
        lesson.setUser(user);
        lesson.setLanguage(language);
        lesson.setLevel(level);
        lesson.setLessonDate(LocalDate.now());

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> parsed = mapper.readValue(clean, Map.class);

            lesson.setTitle((String) parsed.getOrDefault("title", "Daily Lesson"));
            lesson.setScenario((String) parsed.getOrDefault("scenario", ""));
            lesson.setObjectives(mapper.writeValueAsString(parsed.getOrDefault("objectives", List.of())));
            lesson.setVocabulary(mapper.writeValueAsString(parsed.getOrDefault("vocabulary", List.of())));
            lesson.setConversationScript(mapper.writeValueAsString(parsed.getOrDefault("conversationScript", List.of())));
            lesson.setReviewQuestions(mapper.writeValueAsString(parsed.getOrDefault("reviewQuestions", List.of())));
            lesson.setDifficulty((int) parsed.getOrDefault("difficulty", 5));
            lesson.setXpReward((int) parsed.getOrDefault("xpReward", 30));
        } catch (Exception e) {
            logger.error("Failed to parse lesson JSON: {}", e.getMessage());
            lesson.setTitle("Daily Lesson");
            lesson.setScenario(clean);
            lesson.setObjectives("[]");
            lesson.setVocabulary("[]");
            lesson.setConversationScript("[]");
            lesson.setReviewQuestions("[]");
        }

        dailyLessonRepository.save(lesson);
        logger.info("Generated lesson '{}' for user {} in {}", lesson.getTitle(), user.getUid(), language);
        return lesson;
    }
}
