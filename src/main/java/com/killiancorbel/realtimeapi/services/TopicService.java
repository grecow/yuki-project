package com.killiancorbel.realtimeapi.services;

import com.killiancorbel.realtimeapi.models.Topic;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TopicService {
    private final Logger logger = LoggerFactory.getLogger(TopicService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Topic> cache = new ConcurrentHashMap<>();

    private static final String[] SUPPORTED_LANGUAGES = {"en", "fr", "es", "ru", "it", "de", "po", "jp", "kr", "ch"};

    @PostConstruct
    public void preloadTopics() {
        for (String lang : SUPPORTED_LANGUAGES) {
            try {
                InputStream is = new ClassPathResource("topics/topics_" + lang + ".json").getInputStream();
                Topic topic = objectMapper.readValue(is, Topic.class);
                cache.put(lang, topic);
                logger.info("Cached topics for language: {}", lang);
            } catch (IOException e) {
                logger.warn("Could not load topics for language {}: {}", lang, e.getMessage());
            }
        }
        logger.info("Topic cache loaded: {} languages", cache.size());
    }

    public Topic loadTopics(String language) {
        return cache.get(language);
    }
}
