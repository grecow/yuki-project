package com.killiancorbel.realtimeapi.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.killiancorbel.realtimeapi.models.Topic;
import com.killiancorbel.realtimeapi.models.YukiData;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PromptService {
    private final Logger logger = LoggerFactory.getLogger(PromptService.class);

    @Autowired
    private TopicService topicService;

    private final Map<String, Map<String, Object>> promptTemplates = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadPrompts() {
        try {
            InputStream is = new ClassPathResource("prompts/prompts.json").getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> raw = mapper.readValue(is, Map.class);
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                promptTemplates.put(entry.getKey(), (Map<String, Object>) entry.getValue());
            }
            logger.info("Loaded prompt templates for {} languages", promptTemplates.size());
        } catch (Exception e) {
            logger.error("Failed to load prompts.json: {}", e.getMessage());
        }
    }

    public String getPrompt(YukiData yukiData) {
        String language = yukiData.getLanguage();
        Map<String, Object> template = promptTemplates.getOrDefault(language, promptTemplates.get("English"));
        if (template == null) {
            return "You are Yuki, a language teacher. Keep responses under 30 tokens.";
        }

        String code = (String) template.get("code");
        Topic topicData = topicService.loadTopics(code);
        List<String> topics = levelFromYukiData(yukiData.getLevel(), topicData);
        String topic = topics.get(ThreadLocalRandom.current().nextInt(topics.size()));

        return buildPrompt(template, yukiData, topic);
    }

    public String getPlaygroundPrompt(YukiData yukiData) {
        Map<String, Object> template = promptTemplates.getOrDefault(yukiData.getLanguage(), promptTemplates.get("English"));
        if (template == null) return "";
        return (String) template.getOrDefault("playground", "");
    }

    private String buildPrompt(Map<String, Object> template, YukiData yukiData, String topic) {
        StringBuilder sb = new StringBuilder();

        // Teacher intro (in target language)
        sb.append(template.get("teacher_intro")).append(" ");

        // Level instructions (in target language)
        Map<String, String> levels = (Map<String, String>) template.get("levels");
        String levelKey = String.valueOf(yukiData.getLevel());
        sb.append(levels.getOrDefault(levelKey, levels.getOrDefault("0", ""))).append(" ");

        // Greeting + topic
        sb.append(template.get("greeting")).append(topic).append(". ");

        // Corrections
        if (yukiData.isToCorrect()) {
            sb.append(template.get("correction")).append(" ");
        }

        // Goal
        sb.append(template.get("goal")).append(" ");

        // Inline correction format
        sb.append("When correcting, use format: [CORRECTION: wrong → correct]. ");

        return sb.toString();
    }

    private List<String> levelFromYukiData(int level, Topic topic) {
        if (topic == null) return List.of("daily life");
        return switch (level) {
            case 0 -> topic.getBeginner() != null ? topic.getBeginner() : List.of("daily life");
            case 1 -> topic.getIntermediate() != null ? topic.getIntermediate() : List.of("hobbies");
            case 2 -> topic.getAdvanced() != null ? topic.getAdvanced() : List.of("culture");
            default -> topic.getExpert() != null ? topic.getExpert() : List.of("philosophy");
        };
    }
}
