package com.killiancorbel.realtimeapi.services;

import com.killiancorbel.realtimeapi.models.Topic;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;

@Service
public class TopicService {
    public Topic loadTopics(String language) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            InputStream inputStream = new ClassPathResource("topics/topics_" + language + ".json").getInputStream();
            return objectMapper.readValue(inputStream, Topic.class);
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception properly in production
        }
        return null;
    }
}
