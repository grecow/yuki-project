package com.killiancorbel.realtimeapi.services;

import com.killiancorbel.realtimeapi.models.YukiData;
import com.killiancorbel.realtimeapi.models.Topic;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PromptService {
    @Autowired
    private TopicService topicService;

    private static final Map<String, String> LANGUAGE_CODES = Map.ofEntries(
            Map.entry("French", "fr"),
            Map.entry("Spanish", "es"),
            Map.entry("Russian", "ru"),
            Map.entry("Italian", "it"),
            Map.entry("German", "de"),
            Map.entry("Portuguese", "po"),
            Map.entry("Japanese", "jp"),
            Map.entry("Korean", "kr"),
            Map.entry("Chinese", "ch"),
            Map.entry("English", "en")
    );

    private static final Map<String, String> TEACHER_INTRO = Map.ofEntries(
            Map.entry("French", "Vous êtes un professeur de français très adaptable et engageant."),
            Map.entry("Spanish", "Eres un profesor de español altamente adaptable y dinámico."),
            Map.entry("Russian", "Вы очень адаптивный и увлекательный учитель русского языка."),
            Map.entry("Italian", "Sei un insegnante di italiano altamente adattabile e coinvolgente."),
            Map.entry("German", "Sie sind ein hochgradig anpassungsfähiger und engagierter Deutschlehrer."),
            Map.entry("Portuguese", "Você é um professor de português altamente adaptável e envolvente."),
            Map.entry("Japanese", "あなたは非常に適応力があり、魅力的な日本語の先生です。"),
            Map.entry("Korean", "당신은 매우 적응력 있고 매력적인 한국어 선생님입니다."),
            Map.entry("Chinese", "你是一位非常灵活和有吸引力的中文老师。"),
            Map.entry("English", "You are a highly adaptable and engaging English teacher.")
    );

    public String getPrompt(YukiData yukiData) {
        String language = yukiData.getLanguage();
        String code = LANGUAGE_CODES.getOrDefault(language, "en");
        Topic topicData = topicService.loadTopics(code);
        List<String> topics = levelFromYukiData(yukiData.getLevel(), topicData);
        String topic = topics.get(ThreadLocalRandom.current().nextInt(topics.size()));

        return buildPrompt(yukiData, language, topic);
    }

    public String getPlaygroundPrompt(YukiData yukiData) {
        String language = yukiData.getLanguage();
        String intro = TEACHER_INTRO.getOrDefault(language, TEACHER_INTRO.get("English"));
        return intro + " " + getPlaygroundRules(language);
    }

    private String buildPrompt(YukiData yukiData, String language, String topic) {
        StringBuilder prompt = new StringBuilder();

        // Role
        String intro = TEACHER_INTRO.getOrDefault(language, TEACHER_INTRO.get("English"));
        prompt.append(intro).append(" ");

        // Level-specific instructions
        prompt.append(getLevelInstructions(yukiData.getLevel(), language));

        // Tone
        prompt.append("Your personality is friendly and dynamic. ");
        prompt.append("Keep your responses concise, under 30 tokens. ");

        // Topic introduction
        prompt.append("Start with: 'Hi! I'm Yuki!' ");
        prompt.append("Immediately follow up with a question about this topic: ").append(topic).append(". ");

        // Corrections
        if (yukiData.isToCorrect()) {
            prompt.append(getCorrectionInstructions(yukiData.getLevel()));
        }

        // Goal
        prompt.append("Your goal is to make the conversation engaging and natural while ensuring effective speaking practice. ");
        prompt.append("Your responses should always be under 30 tokens. Never elaborate too much. ");
        prompt.append("The main objective is for the student to speak as much as possible. ");

        // Inline correction format (NEW)
        prompt.append("When correcting, use this format: [CORRECTION: wrong phrase → correct phrase]. ");
        prompt.append("This helps the student see exactly what to fix. ");

        return prompt.toString();
    }

    private String getLevelInstructions(int level, String language) {
        return switch (level) {
            case 0 -> "Your student is at A1 level (beginner). " +
                    "Use simple sentences and A1-level vocabulary. " +
                    "Only correct major mistakes. " +
                    "Keep answers under 30 tokens. Let the student speak as much as possible. " +
                    "Do not give examples when asking questions. " +
                    "Ask only one question at a time. ";
            case 1 -> "Your student is at A2 level (elementary). " +
                    "Use simple sentences and A2-level vocabulary. " +
                    "Keep answers under 30 tokens. " +
                    "Ask only one question at a time. ";
            case 2 -> "Your student is at B1 level (intermediate). " +
                    "Use B1-level words and phrases. " +
                    "Choose engaging and creative topics. " +
                    "Keep answers under 30 tokens. " +
                    "Ask only one question at a time. ";
            default -> "Your student is at B2 level (upper-intermediate). " +
                    "Use natural B2-level conversation. " +
                    "Choose creative and stimulating topics. " +
                    "Keep answers under 30 tokens. " +
                    "Ask only one question at a time. ";
        };
    }

    private String getCorrectionInstructions(int level) {
        if (level == 0) {
            return "Correct only major mistakes in a supportive way. " +
                    "For beginners, prioritize practice over corrections. " +
                    "Use format: [CORRECTION: mistake → fix]. ";
        }
        return "Correct mistakes concisely: 'Great! We usually say [correct version].' " +
                "Use format: [CORRECTION: mistake → fix]. " +
                "Make corrections motivating, always under 30 tokens. ";
    }

    private String getPlaygroundRules(String language) {
        return "Keep your responses concise, under 30 tokens. " +
                "Your goal is to make the conversation engaging and natural. " +
                "Never elaborate too much. The main objective is for the student to speak as much as possible. " +
                "When correcting, use format: [CORRECTION: wrong → correct]. ";
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
